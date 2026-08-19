package com.mhesus.api.clientes.application;

import com.mhesus.api.clientes.application.ClienteRequest;
import com.mhesus.api.clientes.application.MotoRequest;
import com.mhesus.api.clientes.domain.Cliente;
import com.mhesus.api.clientes.domain.Motocicleta;
import com.mhesus.api.clientes.domain.ClienteRepository;
import com.mhesus.api.clientes.domain.MotocicletaRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final MotocicletaRepository motocicletaRepository;

    public ClienteService(ClienteRepository clienteRepository, MotocicletaRepository motocicletaRepository) {
        this.clienteRepository = clienteRepository;
        this.motocicletaRepository = motocicletaRepository;
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public Cliente crear(ClienteRequest req) {
        Cliente c = new Cliente(
                IdGenerator.generar("cli"), req.dni(), req.nombres(), req.apellidos(),
                req.celular(), req.direccion(), Instant.now().toString()
        );
        return clienteRepository.save(c);
    }

    public List<Motocicleta> motosDeCliente(String clienteId) {
        return motocicletaRepository.findByClienteId(clienteId);
    }

    public List<Motocicleta> todasLasMotos() {
        return motocicletaRepository.findAll();
    }

    public Optional<Motocicleta> buscarMotoPorPlaca(String placa) {
        return motocicletaRepository.findByPlacaIgnoreCase(placa);
    }

    public List<Motocicleta> buscarMotosPorPlacaParcial(String query) {
        return motocicletaRepository.findByPlacaContainingIgnoreCase(query);
    }

    public Optional<Motocicleta> moto(String id) {
        return motocicletaRepository.findById(id);
    }

    public Motocicleta agregarMoto(MotoRequest req) {
        Motocicleta m = new Motocicleta(
                IdGenerator.generar("moto"), req.clienteId(), req.placa().toUpperCase(),
                req.marca(), req.modelo(), req.anio(), req.kmActual() == null ? 0 : req.kmActual()
        );
        return motocicletaRepository.save(m);
    }

    public Motocicleta actualizarKm(String motoId, int km) {
        Motocicleta m = motocicletaRepository.findById(motoId).orElseThrow();
        m.kmActual = km;
        return motocicletaRepository.save(m);
    }
}
