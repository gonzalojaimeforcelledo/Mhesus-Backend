package com.mhesus.api.auth.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    public String id;

    @Column(nullable = false)
    public String nombre;

    @Column(nullable = false, unique = true)
    public String usuario;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String rol; // recepcion | mecanico | almacen | jefe_taller | administracion

    @Column(nullable = false)
    public boolean activo = true;

    public Usuario() {}

    public Usuario(String id, String nombre, String usuario, String passwordHash, String rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }
}
