package com.mhesus.api.clientes.presentation;

import com.mhesus.api.shared.dto.ErrorResponse;
import com.mhesus.api.clientes.domain.Motocicleta;
import com.mhesus.api.ot.domain.OrdenTrabajo;
import com.mhesus.api.clientes.application.ClienteService;
import com.mhesus.api.ot.application.OtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/motos")
public class MotoController {
    private final ClienteService clienteService;
    private final OtService otService;

    public MotoController(ClienteService clienteService, OtService otService) {
        this.clienteService = clienteService;
        this.otService = otService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String placa, @RequestParam(required = false) String q) {
        if (placa != null && !placa.isBlank()) {
            return clienteService.buscarMotoPorPlaca(placa)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(new ErrorResponse("Placa no encontrada.")));
        }
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(clienteService.buscarMotosPorPlacaParcial(q));
        }
        return ResponseEntity.ok(clienteService.todasLasMotos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Motocicleta> porId(@PathVariable String id) {
        return clienteService.moto(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/historial")
    public List<OrdenTrabajo> historial(@PathVariable String id) {
        return otService.historialDeMoto(id);
    }
}
