package com.mhesus.api.clientes.application;

public record MotoRequest(String clienteId, String placa, String marca, String modelo, int anio, Integer kmActual) {}
