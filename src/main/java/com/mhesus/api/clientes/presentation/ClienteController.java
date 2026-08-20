package com.mhesus.api.clientes.presentation;

import com.mhesus.api.clientes.application.ClienteRequest;
import com.mhesus.api.shared.dto.ErrorResponse;
import com.mhesus.api.clientes.application.MotoRequest;
import com.mhesus.api.clientes.application.PlacaDuplicadaResponse;
import com.mhesus.api.clientes.domain.Cliente;
import com.mhesus.api.clientes.domain.Motocicleta;
import com.mhesus.api.clientes.application.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/api/v1/clientes")
    public ResponseEntity<?> listar(@RequestParam(required = false) String dni) {
        if (dni != null && !dni.isBlank()) {
            return clienteService.buscarPorDni(dni)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(new ErrorResponse("Cliente no encontrado.")));
        }
        return ResponseEntity.ok(clienteService.listar());
    }

    @PostMapping("/api/v1/clientes")
    public Cliente crear(@RequestBody ClienteRequest req) {
        return clienteService.crear(req);
    }

    @GetMapping("/api/v1/clientes/{id}/motocicletas")
    public List<Motocicleta> motosDeCliente(@PathVariable String id) {
        return clienteService.motosDeCliente(id);
    }

    @PostMapping("/api/v1/clientes/{id}/motocicletas")
    public ResponseEntity<?> agregarMoto(@PathVariable String id, @RequestBody MotoRequest req) {
        MotoRequest conCliente = new MotoRequest(id, req.placa(), req.marca(), req.modelo(), req.anio(), req.kmActual());
        try {
            Motocicleta m = clienteService.agregarMoto(conCliente);
            return ResponseEntity.ok(m);
        } catch (ClienteService.PlacaDuplicadaException e) {
            return ResponseEntity.status(409).body(new PlacaDuplicadaResponse(e.getMessage(), e.motoExistente));
        }
    }
}
