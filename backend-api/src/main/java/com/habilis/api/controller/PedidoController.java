package com.habilis.api.controller;

import com.habilis.api.dto.PedidoRequest;
import com.habilis.api.entity.Pedido;
import com.habilis.api.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de pedidos
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * POST /api/pedidos
     * Crear un nuevo pedido desde el carrito
     * Requiere sesión activa
     */
    @PostMapping
    public ResponseEntity<?> crearPedido(
            @Valid @RequestBody PedidoRequest request,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        // Sobrescribir usuarioId del request con el de la sesión (seguridad)
        request.setUsuarioId(userId);

        try {
            // Validar carrito
            List<String> errores = pedidoService.validarCarrito(request.getItems());
            if (!errores.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Errores en el carrito",
                        "detalles", errores));
            }

            // Crear pedido
            Pedido pedido = pedidoService.crearPedido(userId, request.getItems());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("pedido", pedido);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/pedidos/calcular-total
     * Calcular total del carrito sin crear pedido
     */
    @PostMapping("/calcular-total")
    public ResponseEntity<?> calcularTotal(@Valid @RequestBody PedidoRequest request) {
        try {
            BigDecimal total = pedidoService.calcularTotal(request.getItems());

            return ResponseEntity.ok(Map.of(
                    "total", total,
                    "cantidadItems", request.getItems().size()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/pedidos/validar-carrito
     * Validar carrito antes de crear pedido
     */
    @PostMapping("/validar-carrito")
    public ResponseEntity<?> validarCarrito(@Valid @RequestBody PedidoRequest request) {
        List<String> errores = pedidoService.validarCarrito(request.getItems());

        if (errores.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "valido", true,
                    "message", "El carrito es válido"));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "valido", false,
                    "errores", errores));
        }
    }

    /**
     * GET /api/pedidos
     * Listar todos los pedidos (solo ADMIN) o pedidos del usuario actual
     */
    @GetMapping
    public ResponseEntity<?> listarPedidos(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String tipoUsuario = (String) session.getAttribute("userRole");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        List<Pedido> pedidos;

        if ("ADMIN".equals(tipoUsuario)) {
            // Admin ve todos los pedidos
            pedidos = pedidoService.listarTodos();
        } else {
            // Usuario normal ve solo sus pedidos
            pedidos = pedidoService.listarPorUsuario(userId);
        }

        return ResponseEntity.ok(pedidos);
    }

    /**
     * GET /api/pedidos/{id}
     * Obtener un pedido por ID
     * Los usuarios solo pueden ver sus propios pedidos
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPedido(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String tipoUsuario = (String) session.getAttribute("userRole");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        try {
            Pedido pedido = pedidoService.buscarPorId(id);

            // Verificar que el usuario puede ver este pedido
            if (!"ADMIN".equals(tipoUsuario) && !pedido.getUsuario().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("error", "No tienes permiso para ver este pedido"));
            }

            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/usuario/{usuarioId}
     * Listar pedidos de un usuario específico (solo ADMIN)
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPedidosPorUsuario(
            @PathVariable Long usuarioId,
            HttpSession session) {

        String tipoUsuario = (String) session.getAttribute("userRole");

        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden ver pedidos de otros usuarios"));
        }

        List<Pedido> pedidos = pedidoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * GET /api/pedidos/estado/{estado}
     * Listar pedidos por estado (solo ADMIN)
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(
            @PathVariable String estado,
            HttpSession session) {

        String tipoUsuario = (String) session.getAttribute("userRole");

        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden filtrar pedidos por estado"));
        }

        List<Pedido> pedidos = pedidoService.listarPorEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * PATCH /api/pedidos/{id}/estado
     * Cambiar estado de un pedido (solo ADMIN)
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado,
            HttpSession session) {

        String tipoUsuario = (String) session.getAttribute("userRole");

        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden cambiar el estado de pedidos"));
        }

        try {
            Pedido pedido = pedidoService.cambiarEstado(id, nuevoEstado);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estado actualizado exitosamente");
            response.put("pedido", pedido);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/pedidos/{id}/cancelar
     * Cancelar un pedido (solo si está PENDIENTE)
     * Los usuarios pueden cancelar sus propios pedidos
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String tipoUsuario = (String) session.getAttribute("userRole");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        try {
            Pedido pedido = pedidoService.buscarPorId(id);

            // Verificar que el usuario puede cancelar este pedido
            if (!"ADMIN".equals(tipoUsuario) && !pedido.getUsuario().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("error", "No tienes permiso para cancelar este pedido"));
            }

            Pedido pedidoCancelado = pedidoService.cancelarPedido(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido cancelado exitosamente. El stock ha sido restaurado.");
            response.put("pedido", pedidoCancelado);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/mis-estadisticas
     * Obtener estadísticas de pedidos del usuario actual
     */
    @GetMapping("/mis-estadisticas")
    public ResponseEntity<?> obtenerMisEstadisticas(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        Map<String, Object> estadisticas = pedidoService.obtenerEstadisticasUsuario(userId);
        return ResponseEntity.ok(estadisticas);
    }
}
