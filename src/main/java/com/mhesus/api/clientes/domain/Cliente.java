package com.mhesus.api.clientes.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    public String id;

    @Column(nullable = false, unique = true)
    public String dni;

    @Column(nullable = false)
    public String nombres;

    @Column(nullable = false)
    public String apellidos;

    @Column(nullable = false)
    public String celular;

    public String email;

    public String direccion;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Cliente() {}

    public Cliente(String id, String dni, String nombres, String apellidos, String celular, String direccion, String creadoEn) {
        this.id = id;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.celular = celular;
        this.direccion = direccion;
        this.creadoEn = creadoEn;
    }
}
