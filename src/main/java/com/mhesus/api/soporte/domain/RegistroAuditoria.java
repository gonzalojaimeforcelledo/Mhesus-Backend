package com.mhesus.api.soporte.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "auditoria")
public class RegistroAuditoria {
    @Id
    public String id;

    @Column(name = "ot_id")
    public String otId;

    @Column(name = "usuario_id")
    public String usuarioId;

    @Column(nullable = false, length = 500)
    public String accion;

    @Column(name = "estado_anterior")
    public String estadoAnterior;

    @Column(name = "estado_nuevo")
    public String estadoNuevo;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    public RegistroAuditoria() {}
}
