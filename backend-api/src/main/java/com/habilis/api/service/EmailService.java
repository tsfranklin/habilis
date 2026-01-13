package com.habilis.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de correos electrónicos
 * Usado para confirmación de cuenta, recuperación de contraseña y
 * notificaciones 2FA
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@habilis.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Enviar email de confirmación de cuenta
     * 
     * @param destinatario   Email del usuario
     * @param nombreCompleto Nombre del usuario
     * @param token          Token de confirmación
     */
    public void enviarEmailConfirmacion(String destinatario, String nombreCompleto, String token) {
        String asunto = "Confirma tu cuenta en HÁBILIS";
        String mensaje = String.format(
                "Hola %s,\n\n" +
                        "Gracias por registrarte en HÁBILIS.\n\n" +
                        "Para activar tu cuenta, usa el siguiente token de confirmación:\n\n" +
                        "Token: %s\n\n" +
                        "O visita: http://localhost/confirmar-email?token=%s\n\n" +
                        "Si no solicitaste esta cuenta, ignora este mensaje.\n\n" +
                        "Saludos,\n" +
                        "El equipo de HÁBILIS",
                nombreCompleto, token, token);

        enviarEmail(destinatario, asunto, mensaje);
    }

    /**
     * Enviar email de recuperación de contraseña
     * 
     * @param destinatario   Email del usuario
     * @param nombreCompleto Nombre del usuario
     * @param token          Token de recuperación
     */
    public void enviarEmailRecuperacion(String destinatario, String nombreCompleto, String token) {
        String asunto = "Recuperación de contraseña - HÁBILIS";
        String mensaje = String.format(
                "Hola %s,\n\n" +
                        "Recibimos una solicitud para restablecer tu contraseña.\n\n" +
                        "Usa el siguiente token para restablecer tu contraseña:\n\n" +
                        "Token: %s\n\n" +
                        "O visita: http://localhost/restablecer-password?token=%s\n\n" +
                        "Este token expirará en 1 hora.\n\n" +
                        "Si no solicitaste esto, ignora este mensaje y tu contraseña permanecerá sin cambios.\n\n" +
                        "Saludos,\n" +
                        "El equipo de HÁBILIS",
                nombreCompleto, token, token);

        enviarEmail(destinatario, asunto, mensaje);
    }

    /**
     * Enviar notificación de activación de 2FA
     * 
     * @param destinatario   Email del usuario
     * @param nombreCompleto Nombre del usuario
     */
    public void enviarNotificacion2FAActivado(String destinatario, String nombreCompleto) {
        String asunto = "Autenticación de Dos Factores Activada - HÁBILIS";
        String mensaje = String.format(
                "Hola %s,\n\n" +
                        "La autenticación de dos factores (2FA) ha sido activada en tu cuenta.\n\n" +
                        "Ahora necesitarás ingresar un código de 6 dígitos desde tu aplicación de autenticación " +
                        "(Google Authenticator, Authy, etc.) cada vez que inicies sesión.\n\n" +
                        "Si no realizaste esta acción, contacta con soporte inmediatamente.\n\n" +
                        "Saludos,\n" +
                        "El equipo de HÁBILIS",
                nombreCompleto);

        enviarEmail(destinatario, asunto, mensaje);
    }

    /**
     * Enviar notificación de desactivación de 2FA
     * 
     * @param destinatario   Email del usuario
     * @param nombreCompleto Nombre del usuario
     */
    public void enviarNotificacion2FADesactivado(String destinatario, String nombreCompleto) {
        String asunto = "Autenticación de Dos Factores Desactivada - HÁBILIS";
        String mensaje = String.format(
                "Hola %s,\n\n" +
                        "La autenticación de dos factores (2FA) ha sido desactivada en tu cuenta.\n\n" +
                        "Tu cuenta ahora usa solo contraseña para iniciar sesión.\n\n" +
                        "Si no realizaste esta acción, contacta con soporte inmediatamente.\n\n" +
                        "Saludos,\n" +
                        "El equipo de HÁBILIS",
                nombreCompleto);

        enviarEmail(destinatario, asunto, mensaje);
    }

    /**
     * Método genérico para enviar emails
     * 
     * @param destinatario Email destino
     * @param asunto       Asunto del email
     * @param mensaje      Cuerpo del mensaje
     */
    private void enviarEmail(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(destinatario);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);

            mailSender.send(mailMessage);

            System.out.println("✅ Email enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email a " + destinatario + ": " + e.getMessage());
            // En producción, registraríamos esto en el sistema de logs
        }
    }
}
