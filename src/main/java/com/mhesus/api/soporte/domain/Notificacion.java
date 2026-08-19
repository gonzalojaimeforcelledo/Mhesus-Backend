package com.mhesus.api.soporte.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    @Id
    public String id;

    @Column(name = "usuario_id", nullable = false)
    public String usuarioId;

    @Column(nullable = false, length = 500)
    public String mensaje;

    @Column(name = "ot_id")
    public String otId;

    @Column(nullable = false)
    public boolean leida;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public Notificacion() {}

    public Notificacion(String id, String usuarioId, String mensaje, String otId, boolean leida, String creadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.mensaje = mensaje;
        this.otId = otId;
        this.leida = leida;
        this.creadoEn = creadoEn;
    }
}
