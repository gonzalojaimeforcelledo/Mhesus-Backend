package com.mhesus.api.auth.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "intentos_login")
public class IntentoLogin {
    /** El nombre de usuario en minúsculas, usado directo como llave primaria. */
    @Id
    public String usuario;

    @Column(nullable = false)
    public int intentos;

    /** Instant en formato ISO-8601, o null si no está bloqueado. */
    @Column(name = "bloqueado_hasta")
    public String bloqueadoHasta;

    public IntentoLogin() {}
}
