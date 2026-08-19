package com.mhesus.api.ot.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ordenes_trabajo")
public class OrdenTrabajo {
    @Id
    public String id;

    @Column(name = "numero_ot", nullable = false, unique = true)
    public String numeroOT;

    @Column(name = "cliente_id", nullable = false)
    public String clienteId;

    @Column(name = "moto_id", nullable = false)
    public String motoId;

    @Column(name = "mecanico_id")
    public String mecanicoId;

    @Column(name = "asesor_id")
    public String asesorId;

    @Column(nullable = false)
    public String estado;

    @Column(name = "nivel_combustible", nullable = false)
    public String nivelCombustible;

    @Column(name = "observacion_cliente", length = 2000)
    public String observacionCliente;

    @Column(name = "servicio_a_realizar", length = 2000)
    public String servicioARealizar;

    @Column(name = "creado_en", nullable = false)
    public String creadoEn;

    @Column(name = "trabajo_iniciado_en")
    public String trabajoIniciadoEn;

    @Column(name = "trabajo_finalizado_en")
    public String trabajoFinalizadoEn;

    @Column(name = "foto_ingreso", columnDefinition = "TEXT")
    public String fotoIngreso;

    public OrdenTrabajo() {}
}
