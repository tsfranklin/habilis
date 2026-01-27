package com.habilis.api.controller;

import com.habilis.api.entity.Factura;
import com.habilis.api.service.FacturaService;
import com.habilis.api.service.PdfService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de facturas
 */
@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;
    private final PdfService pdfService;

    public FacturaController(FacturaService facturaService, PdfService pdfService) {
        this.facturaService = facturaService;
        this.pdfService = pdfService;
    }

    /**
     * GET /api/facturas/mis-facturas
     * Listar todas las facturas del usuario actual
     */
    @GetMapping("/mis-facturas")
    public ResponseEntity<?> listarMisFacturas(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        List<Factura> facturas = facturaService.listarPorUsuario(userId);
        return ResponseEntity.ok(facturas);
    }

    /**
     * GET /api/facturas/{codigo}/descargar
     * Descargar factura en PDF por código
     */
    @GetMapping("/{codigo}/descargar")
    public ResponseEntity<byte[]> descargarFactura(
            @PathVariable String codigo,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Factura factura = facturaService.buscarPorCodigo(codigo);

            // Verificar que el usuario puede descargar esta factura
            if (!factura.getUsuario().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Generar PDF
            byte[] pdfBytes = pdfService.generarFacturaPedido(factura.getPedido().getId());

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "factura_" + codigo + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/facturas/pedido/{pedidoId}
     * Obtener factura de un pedido específico
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> obtenerFacturaPorPedido(
            @PathVariable Long pedidoId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        try {
            Factura factura = facturaService.buscarPorPedido(pedidoId);

            // Verificar que el usuario puede ver esta factura
            if (!factura.getUsuario().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("error", "No tienes permiso para ver esta factura"));
            }

            return ResponseEntity.ok(factura);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage()));
        }
    }
}
