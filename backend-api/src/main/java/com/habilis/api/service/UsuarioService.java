package com.habilis.api.service;

import com.habilis.api.entity.Usuario;
import com.habilis.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para la gestión de usuarios
 * Contiene toda la lógica de negocio relacionada con autenticación y usuarios
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TwoFactorService twoFactorService;

    public UsuarioService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            TwoFactorService twoFactorService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.twoFactorService = twoFactorService;
    }

    /**
     * Registrar un nuevo usuario
     * 
     * @param nombreCompleto    Nombre y apellidos
     * @param movil             Teléfono móvil
     * @param correoElectronico Email
     * @param contrasena        Contraseña sin encriptar
     * @return Usuario creado
     */
    public Usuario registrarUsuario(String nombreCompleto, String movil, String correoElectronico, String contrasena) {
        // Verificar que el email no esté registrado
        if (usuarioRepository.existsByCorreoElectronico(correoElectronico)) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setMovil(movil);
        usuario.setCorreoElectronico(correoElectronico);
        usuario.setContrasena(passwordEncoder.encode(contrasena)); // Encriptar contraseña
        usuario.setTipoUsuario("USER"); // Por defecto es usuario normal
        usuario.setCuentaActiva(false); // Requiere confirmación de email

        // Generar token de confirmación
        String tokenConfirmacion = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(tokenConfirmacion); // Usamos el mismo campo temporalmente

        // Guardar en BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Enviar email de confirmación
        emailService.enviarEmailConfirmacion(correoElectronico, nombreCompleto, tokenConfirmacion);

        return usuarioGuardado;
    }

    /**
     * Confirmar cuenta mediante token
     * 
     * @param token Token de confirmación enviado por email
     * @return Usuario activado
     */
    public Usuario confirmarEmail(String token) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(token);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Token de confirmación inválido");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setCuentaActiva(true);
        usuario.setTokenRecuperacion(null); // Limpiar token

        return usuarioRepository.save(usuario);
    }

    /**
     * Iniciar sesión - Paso 1: Validar credenciales
     * 
     * @param correoElectronico Email del usuario
     * @param contrasena        Contraseña sin encriptar
     * @return Usuario si las credenciales son correctas
     */
    public Usuario login(String correoElectronico, String contrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronico(correoElectronico);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que la cuenta esté activa
        if (!usuario.getCuentaActiva()) {
            throw new RuntimeException("La cuenta no está activada. Revisa tu correo electrónico.");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return usuario;
    }

    /**
     * Verificar código 2FA - Paso 2 del login
     * 
     * @param usuarioId ID del usuario
     * @param codigo    Código de 6 dígitos del authenticator
     * @return true si el código es correcto
     */
    public boolean verificar2FA(Long usuarioId, int codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar límite de intentos
        if (usuario.getTwoFactorAttempts() >= 3) {
            LocalDateTime ultimoIntento = usuario.getLastTwoFactorAttempt();
            if (ultimoIntento != null && ultimoIntento.plusMinutes(5).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Demasiados intentos fallidos. Espera 5 minutos.");
            } else {
                // Resetear intentos después de 5 minutos
                usuario.setTwoFactorAttempts(0);
            }
        }

        // Verificar código TOTP
        boolean codigoValido = twoFactorService.verificarCodigoConVentana(
                usuario.getTwoFactorSecret(),
                codigo,
                5 // Ventana de 5 intervalos (±150 segundos) para mejor tolerancia
        );

        if (codigoValido) {
            // Resetear intentos si el código es correcto
            usuario.setTwoFactorAttempts(0);
            usuario.setLastTwoFactorAttempt(null);
            usuarioRepository.save(usuario);
            return true;
        } else {
            // Incrementar intentos fallidos
            usuario.setTwoFactorAttempts(usuario.getTwoFactorAttempts() + 1);
            usuario.setLastTwoFactorAttempt(LocalDateTime.now());
            usuarioRepository.save(usuario);
            return false;
        }
    }

    /**
     * Habilitar 2FA para un usuario - Generar QR
     * 
     * @param usuarioId ID del usuario
     * @return Código QR en Base64
     */
    public String habilitar2FA(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar secreto TOTP
        String secreto = twoFactorService.generarSecreto();

        // Guardar temporalmente (sin activar aún)
        usuario.setTwoFactorSecret(secreto);
        usuarioRepository.save(usuario);

        // Generar código QR
        return twoFactorService.generarCodigoQR(usuario.getCorreoElectronico(), secreto);
    }

    /**
     * Verificar activación de 2FA - El usuario debe ingresar un código para
     * confirmar
     * 
     * @param usuarioId ID del usuario
     * @param codigo    Código de 6 dígitos generado por la app authenticator
     * @return true si se activa correctamente
     */
    public boolean verificarActivacion2FA(Long usuarioId, int codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getTwoFactorSecret() == null) {
            throw new RuntimeException("Primero debes solicitar la habilitación de 2FA");
        }

        // Verificar que el código sea correcto
        boolean codigoValido = twoFactorService.verificarCodigo(usuario.getTwoFactorSecret(), codigo);

        if (codigoValido) {
            // Activar 2FA
            usuario.setTwoFactorEnabled(true);
            usuarioRepository.save(usuario);

            // Enviar email de notificación
            emailService.enviarNotificacion2FAActivado(
                    usuario.getCorreoElectronico(),
                    usuario.getNombreCompleto());

            return true;
        } else {
            // Código incorrecto - eliminar secreto temporal
            usuario.setTwoFactorSecret(null);
            usuarioRepository.save(usuario);
            return false;
        }
    }

    /**
     * Deshabilitar 2FA
     * 
     * @param usuarioId  ID del usuario
     * @param contrasena Contraseña del usuario (para confirmar)
     * @return true si se desactiva correctamente
     */
    public boolean deshabilitar2FA(Long usuarioId, String contrasena) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar contraseña por seguridad
        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Desactivar 2FA
        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);
        usuario.setTwoFactorAttempts(0);
        usuario.setLastTwoFactorAttempt(null);
        usuarioRepository.save(usuario);

        // Enviar email de notificación
        emailService.enviarNotificacion2FADesactivado(
                usuario.getCorreoElectronico(),
                usuario.getNombreCompleto());

        return true;
    }

    /**
     * Solicitar recuperación de contraseña
     * 
     * @param correoElectronico Email del usuario
     */
    public void solicitarRecuperacionPassword(String correoElectronico) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronico(correoElectronico);

        if (usuarioOpt.isEmpty()) {
            // Por seguridad, no revelar si el email existe o no
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // Generar token de recuperación
        String tokenRecuperacion = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(tokenRecuperacion);
        usuarioRepository.save(usuario);

        // Enviar email
        emailService.enviarEmailRecuperacion(
                correoElectronico,
                usuario.getNombreCompleto(),
                tokenRecuperacion);
    }

    /**
     * Restablecer contraseña usando token
     * 
     * @param token           Token de recuperación
     * @param nuevaContrasena Nueva contraseña sin encriptar
     */
    public void restablecerPassword(String token, String nuevaContrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(token);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Token de recuperación inválido o expirado");
        }

        Usuario usuario = usuarioOpt.get();

        // Actualizar contraseña
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuario.setTokenRecuperacion(null); // Limpiar token
        usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuario por ID
     * 
     * @param id ID del usuario
     * @return Usuario
     */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Buscar usuario por email
     * 
     * @param correoElectronico Email del usuario
     * @return Usuario
     */
    public Optional<Usuario> buscarPorEmail(String correoElectronico) {
        return usuarioRepository.findByCorreoElectronico(correoElectronico);
    }

    /**
     * Reenviar email de confirmación (genera nuevo token)
     * 
     * @param correoElectronico Email del usuario
     */
    public void reenviarEmailConfirmacion(String correoElectronico) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronico(correoElectronico);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("No existe una cuenta con ese correo electrónico");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que la cuenta no esté ya activada
        if (usuario.getCuentaActiva()) {
            throw new RuntimeException("Esta cuenta ya está activada. Puedes iniciar sesión directamente.");
        }

        // Generar nuevo token de confirmación
        String nuevoToken = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(nuevoToken);
        usuarioRepository.save(usuario);

        // Enviar nuevo email
        emailService.enviarEmailConfirmacion(
                correoElectronico,
                usuario.getNombreCompleto(),
                nuevoToken);
    }
}
