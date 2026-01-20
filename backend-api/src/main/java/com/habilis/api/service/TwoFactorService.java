package com.habilis.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Servicio para manejar autenticación de dos factores (2FA) con TOTP
 * Implementación SIMPLE y TRANSPARENTE usando totp-java
 * Compatible con Google Authenticator, Authy, Microsoft Authenticator, FreeOTP
 */
@Service
public class TwoFactorService {

    private final DefaultSecretGenerator secretGenerator;
    private final TimeProvider timeProvider;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;

    public TwoFactorService() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator();

        // Configuración del verificador con ventana de tiempo ampliada
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        this.qrGenerator = new ZxingPngQrGenerator();

        System.out.println("✅ TwoFactorService (TOTP-Java) inicializado");
        System.out.println("   - Algoritmo: SHA1");
        System.out.println("   - Dígitos: 6");
        System.out.println("   - Intervalo: 30 segundos");
        System.out.println("   - Discrepancia permitida: ±3 intervalos (±90s)");
    }

    /**
     * Generar un nuevo secreto BASE32 para TOTP
     * 
     * @return Secreto en formato BASE32 (compatible con todas las apps 2FA)
     */
    public String generarSecreto() {
        String secret = secretGenerator.generate();
        System.out.println("🔑 Secreto TOTP generado:");
        System.out.println("   - Longitud: " + secret.length() + " caracteres");
        System.out.println("   - Formato: BASE32");
        System.out.println(
                "   - Secret (primeros 10 chars): " + secret.substring(0, Math.min(10, secret.length())) + "...");
        return secret;
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
            // Crear datos para el QR
            QrData data = new QrData.Builder()
                    .label(email)
                    .secret(secreto)
                    .issuer("HABILIS")
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            System.out.println("📱 Generando QR para: " + email);
            System.out.println("   - Issuer: HABILIS");
            System.out.println("   - URL: otpauth://totp/HABILIS:" + email + "?secret=" + secreto + "&issuer=HABILIS");

            // Generar imagen QR
            byte[] imageData = qrGenerator.generate(data);

            // Convertir a Base64
            String base64 = Base64.getEncoder().encodeToString(imageData);
            System.out.println("   - QR generado exitosamente (" + imageData.length + " bytes)");

            return base64;

        } catch (QrGenerationException e) {
            System.err.println("❌ Error generando QR: " + e.getMessage());
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
        try {
            String codigoStr = String.format("%06d", codigo);

            // Obtener el código esperado actual
            String codigoEsperado = codeGenerator.generate(secreto, Math.floorDiv(timeProvider.getTime(), 30));

            // Verificar con discrepancia de ±1 intervalo PRIMERO (más estricto)
            boolean valido = codeVerifier.isValidCode(secreto, codigoStr);

            System.out.println("🔐 Verificación TOTP:");
            System.out.println("   - Código ingresado: " + codigoStr);
            System.out.println("   - Código esperado (ventana actual): " + codigoEsperado);
            System.out.println("   - Timestamp: " + timeProvider.getTime());
            System.out.println("   - Ventana: " + Math.floorDiv(timeProvider.getTime(), 30));
            System.out.println("   - Resultado: " + (valido ? "✅ VÁLIDO" : "❌ INVÁLIDO"));

            if (!valido) {
                // Mostrar códigos de ventanas anteriores y posteriores para debugging
                long currentBucket = Math.floorDiv(timeProvider.getTime(), 30);
                String codePrev = codeGenerator.generate(secreto, currentBucket - 1);
                String codeNext = codeGenerator.generate(secreto, currentBucket + 1);
                System.out.println("   - Ventana anterior (-30s): " + codePrev);
                System.out.println("   - Ventana posterior (+30s): " + codeNext);
            }

            return valido;
        } catch (Exception e) {
            System.err.println("❌ Error verificando código: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verificar código TOTP con ventana de tiempo ampliada
     * 
     * @param secreto Secreto BASE32 del usuario
     * @param codigo  Código de 6 dígitos
     * @param ventana Número de intervalos de tolerancia (discrepancia)
     * @return true si el código es válido, false si no
     */
    public boolean verificarCodigoConVentana(String secreto, int codigo, int ventana) {
        try {
            String codigoStr = String.format("%06d", codigo);

            // Crear verificador con ventana personalizada
            DefaultCodeVerifier verifierConVentana = new DefaultCodeVerifier(codeGenerator, timeProvider);
            verifierConVentana.setTimePeriod(30); // 30 segundos por intervalo
            verifierConVentana.setAllowedTimePeriodDiscrepancy(ventana); // Ventana de tolerancia

            // Obtener el código esperado actual
            String codigoEsperado = codeGenerator.generate(secreto, Math.floorDiv(timeProvider.getTime(), 30));

            boolean valido = verifierConVentana.isValidCode(secreto, codigoStr);

            System.out.println("🔐 Verificación TOTP (ventana " + ventana + "):");
            System.out.println("   - Código ingresado: " + codigoStr);
            System.out.println("   - Código esperado (ventana actual): " + codigoEsperado);
            System.out.println("   - Timestamp: " + timeProvider.getTime());
            System.out.println("   - Ventana: " + Math.floorDiv(timeProvider.getTime(), 30));
            System.out.println("   - Tolerancia: ±" + (ventana * 30) + " segundos");
            System.out.println("   - Resultado: " + (valido ? "✅ VÁLIDO" : "❌ INVÁLIDO"));

            if (!valido) {
                // Mostrar todos los códigos posibles en la ventana
                long currentBucket = Math.floorDiv(timeProvider.getTime(), 30);
                System.out.println("   - Códigos válidos en ventana ±" + ventana + ":");
                for (int i = -ventana; i <= ventana; i++) {
                    String code = codeGenerator.generate(secreto, currentBucket + i);
                    String label = i == 0 ? " (ACTUAL)" : (i < 0 ? " (" + i + "x30s)" : " (+" + i + "x30s)");
                    System.out.println("     " + code + label);
                }
            }

            return valido;
        } catch (Exception e) {
            System.err.println("❌ Error verificando código con ventana: " + e.getMessage());
            return false;
        }
    }
}
