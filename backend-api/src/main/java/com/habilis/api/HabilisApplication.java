package com.habilis.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase principal de la aplicación Habilis
 * 
 * @SpringBootApplication incluye:
 *                        - @Configuration: Marca la clase como fuente de
 *                        definiciones de beans
 *                        - @EnableAutoConfiguration: Habilita la configuración
 *                        automática de Spring Boot
 *                        - @ComponentScan: Escanea componentes en el paquete
 *                        com.habilis.api
 */
@SpringBootApplication
public class HabilisApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabilisApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("✓ Habilis API iniciada correctamente");
        System.out.println("✓ Puerto: 8080");
        System.out.println("✓ Documentación: http://localhost:8080");
        System.out.println("===========================================\n");
    }

    /**
     * Configuración CORS para permitir peticiones desde el frontend
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost", "http://localhost:80")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
