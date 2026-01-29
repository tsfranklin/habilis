package com.habilis.api.controller;

import com.habilis.api.entity.Categoria;
import com.habilis.api.repository.CategoriaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de prueba para Categorías y Sesiones
 * Endpoints temporales para verificar conectividad con la BD y sesiones HTTP
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Endpoint de prueba: Listar todas las categorías
     * GET /api/test/categorias
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> obtenerCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Endpoint de prueba: Verificar sesión HTTP
     * GET /api/test/session
     */
    @GetMapping("/session")
    public ResponseEntity<?> testSessionGet(HttpSession session) {
        return testSession(session);
    }

    /**
     * Endpoint de prueba: Verificar sesión HTTP con POST
     * POST /api/test/session
     */
    @PostMapping("/session")
    public ResponseEntity<?> testSessionPost(HttpSession session) {
        return testSession(session);
    }

    private ResponseEntity<?> testSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        response.put("sessionId", session.getId());
        response.put("userId", userId);
        response.put("userRole", userRole);
        response.put("isNew", session.isNew());
        response.put("isAuthenticated", userId != null);
        response.put("maxInactiveInterval", session.getMaxInactiveInterval());

        System.out.println("=== TEST SESSION ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("User ID: " + userId);
        System.out.println("User Role: " + userRole);
        System.out.println("Is New: " + session.isNew());
        System.out.println("Is Authenticated: " + (userId != null));
        System.out.println("===================");

        return ResponseEntity.ok(response);
    }
}
