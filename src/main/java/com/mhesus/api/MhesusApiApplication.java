package com.mhesus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MhesusApiApplication {
    public static void main(String[] args) {
        // Solo forzamos IPv6 si se activa explícitamente con la variable de
        // entorno MHESUS_FORCE_IPV6=true. Esto NO va activado por defecto a
        // propósito: es necesario en redes locales "IPv6-only" (como la del
        // taller, donde el intento por IPv4 se cuelga y nunca llega a probar
        // la IPv6 que sí funciona), pero en la nube (Railway, Render, etc.)
        // suele ser justo al revés — esas redes son IPv4 estándar, y forzar
        // IPv6 ahí rompe la conexión a la base de datos por completo. Si vas
        // a correr esto en tu máquina local con el mismo problema de red,
        // define esa variable de entorno solo ahí (ver README).
        if ("true".equalsIgnoreCase(System.getenv("MHESUS_FORCE_IPV6"))) {
            System.setProperty("java.net.preferIPv6Addresses", "true");
        }
        SpringApplication.run(MhesusApiApplication.class, args);
    }
}
