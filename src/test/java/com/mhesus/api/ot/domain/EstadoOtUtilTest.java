package com.mhesus.api.ot.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias puras (sin levantar Spring) de la máquina de estados de la
 * OT. Sirven también como ejemplo de cómo agregar más tests JUnit al proyecto:
 * cualquier clase en src/test/java terminada en *Test se detecta y corre sola,
 * tanto desde la terminal (`mvn test`) como desde el panel de pruebas del IDE.
 */
class EstadoOtUtilTest {

    @Test
    void primerEstadoEsCreada() {
        assertEquals("Creada", EstadoOtUtil.SECUENCIA.get(0));
    }

    @Test
    void siguienteEstadoDespuesDeCreadaEsAsignada() {
        assertEquals("Asignada", EstadoOtUtil.siguiente("Creada"));
    }

    @Test
    void noHaySiguienteEstadoDespuesDeCerrada() {
        assertNull(EstadoOtUtil.siguiente("Cerrada"));
    }

    @Test
    void estadoDesconocidoNoTieneSiguiente() {
        assertNull(EstadoOtUtil.siguiente("Estado inexistente"));
    }

    @ParameterizedTest
    @CsvSource({
        "Creada, Asignada, true",
        "Asignada, 'Pedido de repuestos', true",
        "Creada, Cerrada, false",
        "Cerrada, Creada, false"
    })
    void validaSoloLaTransicionInmediataSiguiente(String actual, String siguiente, boolean esperado) {
        assertEquals(esperado, EstadoOtUtil.esTransicionValida(actual, siguiente));
    }

    @Test
    void recorrerTodaLaSecuenciaLlegaHastaCerrada() {
        String estado = EstadoOtUtil.SECUENCIA.get(0);
        int pasos = 0;
        while (EstadoOtUtil.siguiente(estado) != null) {
            estado = EstadoOtUtil.siguiente(estado);
            pasos++;
        }
        assertEquals("Cerrada", estado);
        assertEquals(EstadoOtUtil.SECUENCIA.size() - 1, pasos);
    }
}
