package com.mhesus.api.ot.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "diagnosticos")
public class Diagnostico {
    @Id
    public String id;

    @Column(name = "ot_id", nullable = false, unique = true)
    public String otId;

    @Column(length = 2000)
    public String diagnostico;

    @Column(length = 2000)
    public String sugerencias;

    @Column(name = "mecanico_nombre")
    public String mecanicoNombre;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    @Column(name = "foto_diagnostico", columnDefinition = "TEXT")
    public String fotoDiagnostico;

    public Diagnostico() {}
}
