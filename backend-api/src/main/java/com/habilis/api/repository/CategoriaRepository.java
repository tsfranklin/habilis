package com.habilis.api.repository;

import com.habilis.api.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Categoria
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su nombre
     * 
     * @param nombre Nombre de la categoría
     * @return Optional con la categoría si existe
     */
    Optional<Categoria> findByNombre(String nombre);

    /**
     * Verifica si existe una categoría con el nombre dado
     * 
     * @param nombre Nombre a verificar
     * @return true si existe, false si no
     */
    boolean existsByNombre(String nombre);
}
