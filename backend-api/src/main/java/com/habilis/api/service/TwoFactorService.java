package com.habilis.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Servicio para manejar autenticación de dos factores (2FA) con TOTP
 * Compatible con Google Authenticator, Authy, Microsoft Authenticator, etc.
 */
@Service
public class TwoFactorService {

    private final GoogleAuthenticator googleAuthenticator;

    public TwoFactorService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    /**
     * Generar un nuevo secreto BASE32 para TOTP
     * 
     * @return Secreto en formato BASE32
     */
    public String generarSecreto() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
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
            // Generar URL otpauth://
            String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                    "HABILIS", // Issuer (nombre de la app)
                    email, // Account name
                    new GoogleAuthenticatorKey.Builder(secreto).build());

            // Generar código QR como imagen
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    otpAuthURL,
                    BarcodeFormat.QR_CODE,
                    300, // Ancho
                    300 // Alto
            );

            // Convertir a imagen PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // Convertir a Base64
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
        return googleAuthenticator.authorize(secreto, codigo);
    }

    /**
     * Verificar código TOTP con ventana de tiempo ampliada
     * Útil para dar margen de error en caso de desincronización de relojes
     * 
     * @param secreto Secreto BASE32 del usuario
     * @param codigo  Código de 6 dígitos
     * @param ventana Número de intervalos de 30 segundos a verificar (por defecto:
     *                1)
     * @return true si el código es válido, false si no
     */
    public boolean verificarCodigoConVentana(String secreto, int codigo, int ventana) {
        return googleAuthenticator.authorize(secreto, codigo, ventana);
    }
}
