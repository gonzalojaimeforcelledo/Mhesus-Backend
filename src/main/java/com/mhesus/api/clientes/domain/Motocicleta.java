package com.mhesus.api.clientes.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "motocicletas")
public class Motocicleta {
    @Id
    public String id;

    @Column(name = "cliente_id", nullable = false)
    public String clienteId;

    @Column(nullable = false, unique = true)
    public String placa;

    @Column(nullable = false)
    public String marca;

    @Column(nullable = false)
    public String modelo;

    @Column(nullable = false)
    public int anio;

    @Column(name = "km_actual", nullable = false)
    public int kmActual;

    public Motocicleta() {}

    public Motocicleta(String id, String clienteId, String placa, String marca, String modelo, int anio, int kmActual) {
        this.id = id;
        this.clienteId = clienteId;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.kmActual = kmActual;
    }
}
