package com.habilis.api.service;

import com.habilis.api.entity.Factura;
import com.habilis.api.entity.Pedido;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
                        "O haz clic en este enlace: http://localhost/confirm-email.html?token=%s\n\n" +
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
                        "Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                        "%s\n\n" +
                        "Este enlace expirará en 1 hora.\n\n" +
                        "Si no solicitaste esto, ignora este mensaje y tu contraseña permanecerá sin cambios.\n\n" +
                        "Saludos,\n" +
                        "El equipo de HÁBILIS",
                nombreCompleto, "http://localhost/reset-password.html?token=" + token);

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
     * Enviar email de confirmación de pedido con factura PDF adjunta
     * 
     * @param destinatario Email del usuario
     * @param pedido       Pedido creado
     * @param factura      Factura generada
     * @param pdfFactura   Bytes del PDF de la factura
     */
    public void enviarConfirmacionPedido(String destinatario, Pedido pedido, Factura factura, byte[] pdfFactura) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("✅ Confirmación de Pedido #" + pedido.getId() + " - HÁBILIS");

            // Generar contenido HTML del email
            String htmlContent = generarHtmlConfirmacionPedido(pedido, factura);
            helper.setText(htmlContent, true);

            // Adjuntar PDF de la factura
            helper.addAttachment("factura_" + factura.getCodigoFactura() + ".pdf",
                    new ByteArrayResource(pdfFactura));

            mailSender.send(message);

            System.out.println("✅ Email de confirmación enviado a: " + destinatario);
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email de confirmación a " + destinatario + ": " + e.getMessage());
            // No lanzar excepción para no bloquear la creación del pedido
        }
    }

    /**
     * Generar HTML para email de confirmación de pedido
     */
    private String generarHtmlConfirmacionPedido(Pedido pedido, Factura factura) {
        return """
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #FF6B35; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; }
                        .order-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #FF6B35; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                        .button { background-color: #FF6B35; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 ¡Pedido Confirmado!</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            <p>¡Gracias por tu pedido en HÁBILIS! Tu pago ha sido procesado exitosamente.</p>

                            <div class="order-details">
                                <h3>Detalles del Pedido</h3>
                                <p><strong>Número de Pedido:</strong> #%d</p>
                                <p><strong>Código de Factura:</strong> %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Total:</strong> €%.2f</p>
                            </div>

                            <p>Adjunto encontrarás tu factura en formato PDF.</p>
                            <p>Puedes descargar tu factura en cualquier momento desde tu panel de usuario.</p>

                            <a href="http://localhost/user-dashboard.html" class="button">Ver Mis Pedidos</a>

                            <p style="margin-top: 30px;">Si tienes alguna pregunta, no dudes en contactarnos.</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 HÁBILIS - Kits educativos mensuales</p>
                            <p>Este es un email automático, por favor no respondas a este mensaje.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        pedido.getUsuario().getNombreCompleto(),
                        pedido.getId(),
                        factura.getCodigoFactura(),
                        factura.getFechaEmision().toString(),
                        pedido.getTotalPedido());
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
