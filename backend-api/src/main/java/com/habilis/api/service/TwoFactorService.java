package com.habilis.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para manejar autenticación de dos factores (2FA) con TOTP
 * Compatible con Google Authenticator, Authy, Microsoft Authenticator, etc.
 */
@Service
public class TwoFactorService {

    private final GoogleAuthenticator googleAuthenticator;

    public TwoFactorService() {
        // Configuración con ventana de tiempo ampliada para mejor compatibilidad
        GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder configBuilder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();

        GoogleAuthenticatorConfig config = configBuilder
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                .setWindowSize(5) // ±150 segundos de tolerancia
                .setCodeDigits(6)
                .build();

        this.googleAuthenticator = new GoogleAuthenticator(config);

        System.out.println("✅ TwoFactorService inicializado - ventana: ±150 segundos");
    }

    /**
     * Generar un nuevo secreto BASE32 para TOTP
     * 
     * @return Secreto en formato BASE32
     */
    public String generarSecreto() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secreto = key.getKey();
        System.out.println("🔑 Secreto generado - longitud: " + secreto.length());
        return secreto;
    }

    /**
     * Generar código QR en formato Base64 (imagen PNG)
     * 
     * @param email   Email del usuario
     * @param secreto Secreto BASE32
     * @return Imagen QR en Base64
     */
    public String generarCodigoQR(String email, String secreto) {
        try {
            String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                    "HABILIS",
                    email,
                    new GoogleAuthenticatorKey.Builder(secreto).build());

            System.out.println("📱 QR generado para: " + email);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    otpAuthURL,
                    BarcodeFormat.QR_CODE,
                    300,
                    300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error al generar código QR: " + e.getMessage());
        }
    }

    /**
     * Verificar código TOTP de 6 dígitos
     * 
     * @param secreto Secreto BASE32 del usuario
     * @param codigo  Código de 6 dígitos ingresado por el usuario
     * @return true si el código es válido, false si no
     */
    public boolean verificarCodigo(String secreto, int codigo) {
        long ventanaActual = System.currentTimeMillis() / 30000;
        boolean resultado = googleAuthenticator.authorize(secreto, codigo);

        System.out.println("🔐 Verificación 2FA:");
        System.out.println("    Código: " + codigo);
        System.out.println("    Ventana: " + ventanaActual);
        System.out.println("    Resultado: " + (resultado ? "✅ VÁLIDO" : "❌ INVÁLIDO"));

        if (!resultado) {
            // Generar el código correcto para debugging
            int codigoCorrecto = googleAuthenticator.getTotpPassword(secreto);
            System.out.println("    Código esperado: " + codigoCorrecto);
        }

        return resultado;
    }

    /**
     * Verificar código TOTP con ventana de tiempo ampliada
     */
    public boolean verificarCodigoConVentana(String secreto, int codigo, int ventana) {
        long ventanaActual = System.currentTimeMillis() / 30000;
        boolean resultado = googleAuthenticator.authorize(secreto, codigo, ventana);

        System.out.println("🔐 Verificación 2FA (ventana " + ventana + "):");
        System.out.println("    Código: " + codigo);
        System.out.println("    Ventana: " + ventanaActual);
        System.out.println("    Tolerancia: ±" + (ventana * 30) + "s");
        System.out.println("    Resultado: " + (resultado ? "✅ VÁLIDO" : "❌ INVÁLIDO"));

        return resultado;
    }
}
