package com.habilis.api.controller;

import com.habilis.api.dto.AuthResponse;
import com.habilis.api.dto.LoginRequest;
import com.habilis.api.dto.RegistroRequest;
import com.habilis.api.entity.Usuario;
import com.habilis.api.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para autenticación y gestión de usuarios
 * Incluye registro, login, 2FA, recuperación de contraseña, etc.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * POST /api/auth/register
     * Registrar un nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        try {
            Usuario usuario = usuarioService.registrarUsuario(
                    request.getNombreCompleto(),
                    request.getMovil(),
                    request.getCorreoElectronico(),
                    request.getContrasena());

            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Usuario registrado exitosamente. Revisa tu correo para activar tu cuenta.",
                    usuario.getId(),
                    usuario.getTipoUsuario()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * POST /api/auth/confirmar-email
     * GET /api/auth/confirmar-email (para compatibilidad con enlaces de email)
     * Activar cuenta mediante token enviado por correo
     */
    @PostMapping("/confirmar-email")
    public ResponseEntity<AuthResponse> confirmarEmailPost(@RequestParam String token) {
        return confirmarEmail(token);
    }

    @GetMapping("/confirmar-email")
    public ResponseEntity<AuthResponse> confirmarEmailGet(@RequestParam String token) {
        return confirmarEmail(token);
    }

    private ResponseEntity<AuthResponse> confirmarEmail(String token) {
        try {
            Usuario usuario = usuarioService.confirmarEmail(token);
            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Cuenta activada exitosamente. Ya puedes iniciar sesión.",
                    usuario.getId(),
                    usuario.getTipoUsuario()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login
     * Iniciar sesión - Paso 1: Validar credenciales
     * Si el usuario tiene 2FA habilitado, retorna requires2FA=true
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            System.out.println("=== LOGIN - DEBUG ===");
            System.out.println("Email: " + request.getCorreoElectronico());
            System.out.println("Session ID antes de login: " + session.getId());

            Usuario usuario = usuarioService.login(request.getCorreoElectronico(), request.getContrasena());

            // Verificar si tiene 2FA habilitado
            if (usuario.getTwoFactorEnabled()) {
                // NO crear sesión aún - esperar código 2FA
                System.out.println("⚠️ Usuario tiene 2FA habilitado, esperando código");
                AuthResponse response = new AuthResponse(true, "Se requiere autenticación de dos factores");
                response.setUserId(usuario.getId());
                response.setRequires2FA(true);
                return ResponseEntity.ok(response);
            } else {
                // Crear sesión HTTP
                session.setAttribute("userId", usuario.getId());
                session.setAttribute("userRole", usuario.getTipoUsuario());

                System.out.println("✅ Sesión creada:");
                System.out.println("  - Session ID: " + session.getId());
                System.out.println("  - User ID: " + session.getAttribute("userId"));
                System.out.println("  - User Role: " + session.getAttribute("userRole"));
                System.out.println("  - Max Inactive Interval: " + session.getMaxInactiveInterval() + " segundos");
                System.out.println("=== FIN LOGIN ===");

                return ResponseEntity.ok(new AuthResponse(
                        true,
                        "Login exitoso",
                        usuario.getId(),
                        usuario.getTipoUsuario()));
            }
        } catch (RuntimeException e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * GET /api/auth/check-email
     * Verificar si un email existe en la base de datos (endpoint público)
     * Usado por el quiz para decidir si redirigir a login o registro
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = usuarioService.existeEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("email", email);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al verificar email"));
        }
    }

    /**
     * POST /api/auth/verificar-2fa
     * Login - Paso 2: Verificar código TOTP
     */
    @PostMapping("/verificar-2fa")
    public ResponseEntity<AuthResponse> verificar2FA(
            @RequestParam Long usuarioId,
            @RequestParam int codigo,
            HttpSession session) {
        try {
            boolean codigoValido = usuarioService.verificar2FA(usuarioId, codigo);

            if (codigoValido) {
                // Código correcto - crear sesión
                Usuario usuario = usuarioService.buscarPorId(usuarioId);
                session.setAttribute("userId", usuario.getId());
                session.setAttribute("userEmail", usuario.getCorreoElectronico());
                session.setAttribute("userRole", usuario.getTipoUsuario());

                return ResponseEntity.ok(new AuthResponse(
                        true,
                        "Login exitoso con 2FA",
                        usuario.getId(),
                        usuario.getTipoUsuario()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new AuthResponse(false, "Código 2FA incorrecto. Intenta nuevamente."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * GET /api/auth/me
     * Obtener información del usuario actual de la sesión
     */
    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No hay sesión activa"));
        }

        try {
            Usuario usuario = usuarioService.buscarPorId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nombreCompleto", usuario.getNombreCompleto());
            response.put("correoElectronico", usuario.getCorreoElectronico());
            response.put("movil", usuario.getMovil());
            response.put("tipoUsuario", usuario.getTipoUsuario());
            response.put("cuentaActiva", usuario.getCuentaActiva());
            response.put("twoFactorEnabled", usuario.getTwoFactorEnabled());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/logout
     * Cerrar sesión
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new AuthResponse(true, "Sesión cerrada exitosamente"));
    }

    /**
     * POST /api/auth/recuperar-password
     * Solicitar recuperación de contraseña (envía email)
     */
    @PostMapping("/recuperar-password")
    public ResponseEntity<AuthResponse> solicitarRecuperacion(@RequestParam String correoElectronico) {
        usuarioService.solicitarRecuperacionPassword(correoElectronico);
        return ResponseEntity.ok(new AuthResponse(
                true,
                "Si el correo existe, recibirás instrucciones para restablecer tu contraseña"));
    }

    /**
     * POST /api/auth/restablecer-password
     * Restablecer contraseña usando token
     */
    @PostMapping("/restablecer-password")
    public ResponseEntity<AuthResponse> restablecerPassword(
            @RequestParam String token,
            @RequestParam String nuevaContrasena) {
        try {
            usuarioService.restablecerPassword(token, nuevaContrasena);
            return ResponseEntity.ok(new AuthResponse(true, "Contraseña actualizada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * POST /api/auth/2fa/habilitar
     * Generar código QR para activar 2FA
     * Requiere sesión activa
     */
    @PostMapping("/2fa/habilitar")
    public ResponseEntity<?> habilitar2FA(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        try {
            String qrCodeBase64 = usuarioService.habilitar2FA(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Escanea el código QR con tu aplicación de autenticación");
            response.put("qrCode", "data:image/png;base64," + qrCodeBase64);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/2fa/verificar-activacion
     * Confirmar activación de 2FA ingresando un código
     * Requiere sesión activa
     */
    @PostMapping("/2fa/verificar-activacion")
    public ResponseEntity<AuthResponse> verificarActivacion2FA(
            @RequestParam int codigo,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse(false, "Debes iniciar sesión primero"));
        }

        try {
            boolean activado = usuarioService.verificarActivacion2FA(userId, codigo);

            if (activado) {
                return ResponseEntity.ok(new AuthResponse(
                        true,
                        "Autenticación de dos factores activada exitosamente"));
            } else {
                return ResponseEntity.badRequest().body(new AuthResponse(
                        false,
                        "Código incorrecto. Vuelve a solicitar la activación de 2FA."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * POST /api/auth/2fa/deshabilitar
     * Desactivar 2FA (requiere contraseña para confirmar)
     * Requiere sesión activa
     */
    @PostMapping("/2fa/deshabilitar")
    public ResponseEntity<AuthResponse> deshabilitar2FA(
            @RequestParam String contrasena,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AuthResponse(false, "Debes iniciar sesión primero"));
        }

        try {
            boolean desactivado = usuarioService.deshabilitar2FA(userId, contrasena);

            if (desactivado) {
                return ResponseEntity.ok(new AuthResponse(
                        true,
                        "Autenticación de dos factores desactivada"));
            } else {
                return ResponseEntity.badRequest().body(new AuthResponse(
                        false,
                        "Error al desactivar 2FA"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }

    /**
     * POST /api/auth/reenviar-confirmacion
     * Reenviar email de confirmación (genera nuevo token)
     */
    @PostMapping("/reenviar-confirmacion")
    public ResponseEntity<AuthResponse> reenviarConfirmacion(@RequestParam String email) {
        try {
            usuarioService.reenviarEmailConfirmacion(email);
            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Nuevo código de confirmación enviado. Revisa tu correo."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
        }
    }
}
