package com.habilis.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de Spring Security para HÁBILIS
 * Maneja autenticación, autorización, CORS y sesiones HTTP
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        /**
         * Configuración principal de seguridad
         * Define qué rutas son públicas y cuáles requieren autenticación
         * 
         * TEMPORAL: Seguridad deshabilitada para debugging
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Deshabilitar CSRF
                                .csrf(csrf -> csrf.disable())

                                // Configurar CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // TEMPORAL: Permitir TODAS las peticiones sin autenticación
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest().permitAll())

                                // Configurar manejo de sesiones HTTP
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false))

                                // Deshabilitar el formulario de login por defecto
                                .formLogin(form -> form.disable())

                                // Deshabilitar HTTP Basic Auth
                                .httpBasic(basic -> basic.disable());

                return http.build();
        }

        /**
         * Bean para encriptar contraseñas con BCrypt
         * Fuerza de encriptación: 10 rondas (por defecto)
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Configuración de CORS
         * Permite peticiones desde el frontend (http://localhost)
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Orígenes permitidos
                configuration.setAllowedOrigins(List.of(
                                "http://localhost",
                                "http://localhost:80",
                                "http://127.0.0.1"));

                // Métodos HTTP permitidos
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                // Headers permitidos
                configuration.setAllowedHeaders(List.of("*"));

                // Permitir credenciales (cookies, sesiones HTTP)
                configuration.setAllowCredentials(true);

                // Aplicar configuración a todas las rutas
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
