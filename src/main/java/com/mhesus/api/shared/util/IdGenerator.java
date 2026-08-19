package com.mhesus.api.shared.util;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private static final String CARACTERES = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong CONTADOR = new AtomicLong(System.currentTimeMillis());

    private IdGenerator() {}

    public static String generar(String prefijo) {
        StringBuilder sb = new StringBuilder(prefijo).append('_');
        sb.append(Long.toString(CONTADOR.incrementAndGet(), 36));
        for (int i = 0; i < 6; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }
}
