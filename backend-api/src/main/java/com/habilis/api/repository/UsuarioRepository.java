package com.habilis.api.repository;

import com.habilis.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * Extiende JpaRepository para obtener métodos CRUD automáticos
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico
     * 
     * @param correoElectronico Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    /**
     * Busca un usuario por su token de recuperación
     * 
     * @param tokenRecuperacion Token generado
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

    /**
     * Verifica si existe un usuario con el correo dado
     * 
     * @param correoElectronico Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByCorreoElectronico(String correoElectronico);
}
