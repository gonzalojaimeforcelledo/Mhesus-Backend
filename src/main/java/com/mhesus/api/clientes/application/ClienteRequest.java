package com.mhesus.api.clientes.application;

public record ClienteRequest(String dni, String nombres, String apellidos, String celular, String email, String direccion) {}
