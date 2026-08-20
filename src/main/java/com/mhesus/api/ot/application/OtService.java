package com.mhesus.api.ot.application;

import com.mhesus.api.ot.application.DiagnosticoRequest;
import com.mhesus.api.ot.application.OtCrearRequest;
import com.mhesus.api.ot.domain.Diagnostico;
import com.mhesus.api.ot.domain.OrdenTrabajo;
import com.mhesus.api.ot.domain.DiagnosticoRepository;
import com.mhesus.api.ot.domain.OrdenTrabajoRepository;
import com.mhesus.api.ot.domain.EstadoOtUtil;
import com.mhesus.api.clientes.application.ClienteService;
import com.mhesus.api.soporte.application.SoporteService;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class OtService {
    private final OrdenTrabajoRepository otRepository;
    private final DiagnosticoRepository diagnosticoRepository;
    private final ClienteService clienteService;
    private final SoporteService soporteService;

    public OtService(OrdenTrabajoRepository otRepository, DiagnosticoRepository diagnosticoRepository,
                      ClienteService clienteService, SoporteService soporteService) {
        this.otRepository = otRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.clienteService = clienteService;
        this.soporteService = soporteService;
    }

    public List<OrdenTrabajo> listar() {
        return otRepository.findAll();
    }

    public Optional<OrdenTrabajo> porId(String id) {
        return otRepository.findById(id);
    }

    public List<OrdenTrabajo> historialDeMoto(String motoId) {
        return otRepository.findByMotoIdOrderByCreadoEnDesc(motoId);
    }

    private String numeroCorrelativo() {
        int anio = Year.now().getValue();
        long delAnio = otRepository.countByNumeroOTContaining(String.valueOf(anio));
        return "OT-%d-%04d".formatted(anio, delAnio + 1);
    }

    public OrdenTrabajo crear(OtCrearRequest req, String asesorId) {
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.id = IdGenerator.generar("ot");
        ot.numeroOT = numeroCorrelativo();
        ot.clienteId = req.clienteId();
        ot.motoId = req.motoId();
        ot.mecanicoId = null;
        ot.asesorId = asesorId;
        ot.estado = "Creada";
        ot.nivelCombustible = req.nivelCombustible();
        ot.observacionCliente = req.observacionCliente();
        ot.observacionAsesor = req.observacionAsesor();
        ot.servicioARealizar = req.servicioARealizar();
        ot.creadoEn = Instant.now().toString();
        ot.fotoIngreso = req.fotoIngreso();
        ot.fotoIngresoTrasera = req.fotoIngresoTrasera();
        ot.fotoIngresoLateralIzq = req.fotoIngresoLateralIzq();
        ot.fotoIngresoLateralDer = req.fotoIngresoLateralDer();
        ot.fotoTablero = req.fotoTablero();
        ot.tableroNoEnciende = Boolean.TRUE.equals(req.tableroNoEnciende());
        ot = otRepository.save(ot);
        soporteService.registrarAuditoria(ot.id, asesorId, "Creación de OT", null, "Creada");
        if (req.kmActual() != null) {
            clienteService.actualizarKm(req.motoId(), req.kmActual());
        }
        return ot;
    }

    public OrdenTrabajo asignarMecanico(String otId, String mecanicoId, String usuarioId) {
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow();
        ot.mecanicoId = mecanicoId;
        ot.estado = "Asignada";
        ot = otRepository.save(ot);
        soporteService.registrarAuditoria(otId, usuarioId, "Asignación de mecánico", "Creada", "Asignada");
        soporteService.notificar(mecanicoId, "Se te asignó la orden " + ot.numeroOT + ".", otId);
        return ot;
    }

    public record ResultadoCambioEstado(boolean ok, String error, OrdenTrabajo ot) {}

    public ResultadoCambioEstado cambiarEstado(String otId, String nuevoEstado, String usuarioId, boolean forzar) {
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow();
        if (!forzar && !EstadoOtUtil.esTransicionValida(ot.estado, nuevoEstado)) {
            return new ResultadoCambioEstado(false, "No se permite saltar de \"%s\" a \"%s\" sin autorización de Administración.".formatted(ot.estado, nuevoEstado), ot);
        }
        String anterior = ot.estado;
        ot.estado = nuevoEstado;
        ot = otRepository.save(ot);
        soporteService.registrarAuditoria(otId, usuarioId, forzar ? "Cambio de estado (excepción autorizada)" : "Cambio de estado", anterior, nuevoEstado);
        return new ResultadoCambioEstado(true, null, ot);
    }

    public ResultadoCambioEstado avanzarEstado(String otId, String usuarioId) {
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow();
        String siguiente = EstadoOtUtil.siguiente(ot.estado);
        if (siguiente == null) {
            return new ResultadoCambioEstado(false, "La OT ya se encuentra en el último estado del flujo.", ot);
        }
        return cambiarEstado(otId, siguiente, usuarioId, false);
    }

    public ResultadoCambioEstado finalizarServicioYAvanzar(String otId, String usuarioId) {
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow();
        if (ot.trabajoFinalizadoEn == null) {
            ot.trabajoFinalizadoEn = Instant.now().toString();
            otRepository.save(ot);
        }
        return avanzarEstado(otId, usuarioId);
    }

    public void marcarTrabajoIniciado(String otId) {
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow();
        if (ot.trabajoIniciadoEn == null) {
            ot.trabajoIniciadoEn = Instant.now().toString();
            otRepository.save(ot);
        }
    }

    public Diagnostico registrarDiagnostico(String otId, DiagnosticoRequest req) {
        Diagnostico d = diagnosticoRepository.findByOtId(otId).orElseGet(Diagnostico::new);
        d.id = d.id == null ? IdGenerator.generar("diag") : d.id;
        d.otId = otId;
        d.diagnostico = req.diagnostico();
        d.sugerencias = req.sugerencias();
        d.mecanicoNombre = req.mecanicoNombre();
        d.creadoEn = Instant.now().toString();
        if (req.fotoDiagnostico() != null) d.fotoDiagnostico = req.fotoDiagnostico();
        return diagnosticoRepository.save(d);
    }

    public Optional<Diagnostico> diagnosticoDe(String otId) {
        return diagnosticoRepository.findByOtId(otId);
    }
}
