package com.mhesus.api.ot.domain;

import java.util.List;

public final class EstadoOtUtil {
    public static final List<String> SECUENCIA = List.of(
        "Creada", "Asignada", "Pedido de repuestos", "En diagnóstico",
        "En espera de autorización", "En ejecución", "Control de calidad",
        "Lista para entrega", "Cerrada"
    );

    private EstadoOtUtil() {}

    public static String siguiente(String actual) {
        int idx = SECUENCIA.indexOf(actual);
        if (idx == -1 || idx == SECUENCIA.size() - 1) return null;
        return SECUENCIA.get(idx + 1);
    }

    public static boolean esTransicionValida(String actual, String siguiente) {
        return siguiente.equals(siguiente(actual));
    }

    /** true si "objetivo" está más adelante en la secuencia que "actual" (o en un punto no reconocido). Sirve para no retroceder nunca al avanzar automáticamente. */
    public static boolean estaAntesDe(String actual, String objetivo) {
        int idxActual = SECUENCIA.indexOf(actual);
        int idxObjetivo = SECUENCIA.indexOf(objetivo);
        if (idxActual == -1 || idxObjetivo == -1) return false;
        return idxActual < idxObjetivo;
    }
}
