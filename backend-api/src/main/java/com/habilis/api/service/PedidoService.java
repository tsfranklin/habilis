package com.habilis.api.service;

import com.habilis.api.dto.ItemPedidoRequest;
import com.habilis.api.entity.DetallePedido;
import com.habilis.api.entity.Pedido;
import com.habilis.api.entity.Producto;
import com.habilis.api.entity.Usuario;
import com.habilis.api.repository.DetallePedidoRepository;
import com.habilis.api.repository.PedidoRepository;
import com.habilis.api.repository.ProductoRepository;
import com.habilis.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar pedidos y carrito de compra
 */
@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Crear un nuevo pedido desde el carrito de compra
     * - Valida stock disponible
     * - Reduce stock automáticamente
     * - Calcula total del pedido
     * - Guarda precio histórico en detalle_pedido
     */
    public Pedido crearPedido(Long usuarioId, List<ItemPedidoRequest> items) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Validar que el carrito no esté vacío
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        pedido.setTotalPedido(BigDecimal.ZERO);

        // Guardar para obtener ID
        pedido = pedidoRepository.save(pedido);

        // Procesar cada item del carrito
        BigDecimal totalPedido = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();

        for (ItemPedidoRequest item : items) {
            // Buscar producto
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado con ID: " + item.getProductoId()));

            // Verificar stock disponible
            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para " + producto.getNombre() +
                                ". Disponible: " + producto.getStock() +
                                ", solicitado: " + item.getCantidad());
            }

            // Reducir stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Crear detalle del pedido (guardar precio histórico)
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); // Precio histórico

            detalles.add(detalle);

            // Calcular subtotal
            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));
            totalPedido = totalPedido.add(subtotal);
        }

        // Guardar todos los detalles
        detallePedidoRepository.saveAll(detalles);

        // Actualizar total del pedido
        pedido.setTotalPedido(totalPedido);
        pedido.setDetalles(detalles);

        return pedidoRepository.save(pedido);
    }

    /**
     * Obtener pedido por ID
     */
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Listar todos los pedidos (solo ADMIN)
     */
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    /**
     * Listar pedidos de un usuario
     */
    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuarioId);
    }

    /**
     * Listar pedidos por estado
     */
    public List<Pedido> listarPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    /**
     * Cambiar estado de un pedido
     * Estados válidos: PENDIENTE, ENVIADO, COMPLETADO, CANCELADO
     */
    public Pedido cambiarEstado(Long pedidoId, String nuevoEstado) {
        // Validar estado
        List<String> estadosValidos = List.of("PENDIENTE", "ENVIADO", "COMPLETADO", "CANCELADO");
        if (!estadosValidos.contains(nuevoEstado)) {
            throw new RuntimeException(
                    "Estado inválido. Estados válidos: " + String.join(", ", estadosValidos));
        }

        Pedido pedido = buscarPorId(pedidoId);
        String estadoAnterior = pedido.getEstado();

        // Si se cancela un pedido PENDIENTE, devolver stock
        if ("CANCELADO".equals(nuevoEstado) && "PENDIENTE".equals(estadoAnterior)) {
            devolverStock(pedido);
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    /**
     * Devolver stock al cancelar un pedido
     */
    private void devolverStock(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }
    }

    /**
     * Calcular total de un pedido (sin guardarlo)
     * Útil para mostrar en el carrito antes de confirmar
     */
    public BigDecimal calcularTotal(List<ItemPedidoRequest> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado con ID: " + item.getProductoId()));

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);
        }

        return total;
    }

    /**
     * Validar carrito antes de crear pedido
     * Retorna lista de productos con stock insuficiente
     */
    public List<String> validarCarrito(List<ItemPedidoRequest> items) {
        List<String> errores = new ArrayList<>();

        for (ItemPedidoRequest item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElse(null);

            if (producto == null) {
                errores.add("Producto con ID " + item.getProductoId() + " no encontrado");
                continue;
            }

            if (producto.getStock() < item.getCantidad()) {
                errores.add(producto.getNombre() + ": Stock insuficiente. " +
                        "Disponible: " + producto.getStock() +
                        ", solicitado: " + item.getCantidad());
            }
        }

        return errores;
    }

    /**
     * Cancelar pedido
     * Solo se puede cancelar si está en estado PENDIENTE
     */
    public Pedido cancelarPedido(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);

        if (!"PENDIENTE".equals(pedido.getEstado())) {
            throw new RuntimeException(
                    "Solo se pueden cancelar pedidos en estado PENDIENTE. " +
                            "Estado actual: " + pedido.getEstado());
        }

        return cambiarEstado(pedidoId, "CANCELADO");
    }

    /**
     * Obtener estadísticas de pedidos por usuario
     */
    public Map<String, Object> obtenerEstadisticasUsuario(Long usuarioId) {
        List<Pedido> pedidos = listarPorUsuario(usuarioId);

        long totalPedidos = pedidos.size();
        BigDecimal totalGastado = pedidos.stream()
                .map(Pedido::getTotalPedido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pedidosPendientes = pedidos.stream()
                .filter(p -> "PENDIENTE".equals(p.getEstado()))
                .count();

        long pedidosCompletados = pedidos.stream()
                .filter(p -> "COMPLETADO".equals(p.getEstado()))
                .count();

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalPedidos", totalPedidos);
        estadisticas.put("totalGastado", totalGastado);
        estadisticas.put("pedidosPendientes", pedidosPendientes);
        estadisticas.put("pedidosCompletados", pedidosCompletados);

        return estadisticas;
    }
}
