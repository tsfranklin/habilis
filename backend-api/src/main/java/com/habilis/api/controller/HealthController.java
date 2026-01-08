package com.habilis.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de Health Check
 * Proporciona endpoints para verificar el estado de la API
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Endpoint de health check básico
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Habilis API");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "API funcionando correctamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de bienvenida
     * GET /api/welcome
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, String>> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "¡Bienvenido a Habilis API!");
        response.put("version", "1.0.0");
        response.put("documentation", "http://localhost:8080/api/health");

        return ResponseEntity.ok(response);
    }
}
