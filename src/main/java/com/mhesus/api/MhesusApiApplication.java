package com.mhesus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MhesusApiApplication {
    public static void main(String[] args) {
        // Fuerza IPv6 antes de que arranque cualquier conexión de red (incluida
        // la de la base de datos) — necesario en redes "IPv6-only" donde el
        // intento por IPv4 se cuelga y nunca llega a probar la IPv6 que sí
        // funciona. Puesto acá además del pom.xml para que también aplique al
        // correr la app directo desde el botón ▶ del IDE, no solo con Maven.
        System.setProperty("java.net.preferIPv6Addresses", "true");
        SpringApplication.run(MhesusApiApplication.class, args);
    }
}
