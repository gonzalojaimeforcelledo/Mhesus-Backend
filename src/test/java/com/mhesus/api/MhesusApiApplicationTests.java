package com.mhesus.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de humo: si el contexto de Spring no levanta (por un bean mal
 * configurado, una dependencia faltante, etc.), este test falla y señala
 * exactamente dónde. Es la primera prueba que corre cualquier proyecto
 * Spring Boot generado por Spring Initializr — aquí se agrega a mano porque
 * este proyecto se escribió sin usar el Initializr.
 */
@SpringBootTest
class MhesusApiApplicationTests {

    @Test
    void contextLoads() {
        // Si llega aquí sin lanzar excepción, el contexto cargó correctamente.
    }
}
