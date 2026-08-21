package com.mhesus.api.auth.domain;

import jakarta.persistence.*;

/**
 * Código de 6 dígitos para que un administrador restablezca su propia
 * contraseña por correo, sin depender de otro administrador que lo haga por
 * él (a diferencia del flujo normal de "olvidé mi contraseña", que solo
 * avisa a otros administradores — si el que se bloqueó es el único admin,
 * no hay a quién avisarle).
 */
@Entity
@Table(name = "codigos_recuperacion")
public class CodigoRecuperacion {
    @Id
    public String id;

    @Column(name = "usuario_id", nullable = false)
    public String usuarioId;

    @Column(nullable = false, length = 6)
    public String codigo;

    @Column(name = "expira_en", nullable = false)
    public String expiraEn;

    @Column(nullable = false)
    public boolean usado;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public CodigoRecuperacion() {}
}
