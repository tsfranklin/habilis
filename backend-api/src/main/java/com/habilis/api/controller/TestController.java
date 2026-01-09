package com.habilis.api.controller;

import com.habilis.api.entity.Categoria;
import com.habilis.api.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de prueba para Categorías
 * Endpoint temporal para verificar conectividad con la BD
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
}
