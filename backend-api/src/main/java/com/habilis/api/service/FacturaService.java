package com.habilis.api.service;

import com.habilis.api.entity.Factura;
import com.habilis.api.entity.Pedido;
import com.habilis.api.entity.Usuario;
import com.habilis.api.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para gestión de facturas
 */
@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    /**
     * Generar código único de factura
     * Formato: FAC-YYYYMMDD-XXXXX
     * Ejemplo: FAC-20260126-00001
     */
    public String generarCodigoFactura() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Formato de fecha: YYYYMMDD
        String fecha = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Obtener número secuencial del día
        LocalDateTime inicioDia = today.atStartOfDay();
        LocalDateTime finDia = today.atTime(23, 59, 59);

        long count = facturaRepository.countByFechaEmisionBetween(inicioDia, finDia);

        // Número secuencial con 5 dígitos (00001, 00002, etc.)
        String secuencial = String.format("%05d", count + 1);

        return "FAC-" + fecha + "-" + secuencial;
    }

    /**
     * Crear una nueva factura para un pedido
     */
    @Transactional
    public Factura crearFactura(Pedido pedido, String codigoFactura) {
        // Verificar que no exista ya una factura para este pedido
        if (facturaRepository.existsByPedidoId(pedido.getId())) {
            throw new RuntimeException("Ya existe una factura para este pedido");
        }

        Factura factura = new Factura();
        factura.setCodigoFactura(codigoFactura);
        factura.setPedido(pedido);
        factura.setUsuario(pedido.getUsuario());
        factura.setTotal(pedido.getTotalPedido());
        factura.setFechaEmision(LocalDateTime.now());

        return facturaRepository.save(factura);
    }

    /**
     * Listar todas las facturas de un usuario
     */
    public List<Factura> listarPorUsuario(Long usuarioId) {
        return facturaRepository.findByUsuarioIdOrderByFechaEmisionDesc(usuarioId);
    }

    /**
     * Buscar factura por código
     */
    public Factura buscarPorCodigo(String codigoFactura) {
        return facturaRepository.findByCodigoFactura(codigoFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + codigoFactura));
    }

    /**
     * Buscar factura por ID de pedido
     */
    public Factura buscarPorPedido(Long pedidoId) {
        return facturaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No se encontró factura para el pedido: " + pedidoId));
    }

    /**
     * Buscar factura por ID
     */
    public Factura buscarPorId(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
    }

    /**
     * Actualizar ruta del PDF
     */
    @Transactional
    public Factura actualizarRutaPdf(Long facturaId, String rutaPdf) {
        Factura factura = buscarPorId(facturaId);
        factura.setRutaPdf(rutaPdf);
        return facturaRepository.save(factura);
    }
}
