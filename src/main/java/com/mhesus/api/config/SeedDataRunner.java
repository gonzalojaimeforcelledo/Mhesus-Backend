package com.mhesus.api.config;

import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.domain.UsuarioRepository;
import com.mhesus.api.clientes.domain.Cliente;
import com.mhesus.api.clientes.domain.ClienteRepository;
import com.mhesus.api.clientes.domain.Motocicleta;
import com.mhesus.api.clientes.domain.MotocicletaRepository;
import com.mhesus.api.almacen.domain.Producto;
import com.mhesus.api.almacen.domain.ProductoRepository;
import com.mhesus.api.shared.util.IdGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SeedDataRunner implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final MotocicletaRepository motocicletaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                           MotocicletaRepository motocicletaRepository, ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.motocicletaRepository = motocicletaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return; // ya sembrado

        String demo = passwordEncoder.encode("demo1234");
        usuarioRepository.save(new Usuario("u_recepcion", "Carla Ramos", "recepcion", demo, "recepcion", true));
        usuarioRepository.save(new Usuario("u_mecanico1", "Jhon Quispe", "mecanico", demo, "mecanico", true));
        usuarioRepository.save(new Usuario("u_mecanico2", "Luis Falcón", "mecanico2", demo, "mecanico", true));
        usuarioRepository.save(new Usuario("u_almacen", "Rosa Injante", "almacen", demo, "almacen", true));
        usuarioRepository.save(new Usuario("u_jefe", "Miguel Huamán", "jefe", demo, "jefe_taller", true));
        usuarioRepository.save(new Usuario("u_admin", "Administrador MHESUS", "admin", demo, "administracion", true));

        String hoy = Instant.now().toString();
        clienteRepository.save(new Cliente("c_1", "42839112", "Renato", "Salcedo Díaz", "956821034", "Jr. Lima 245, Chincha Alta", hoy));
        clienteRepository.save(new Cliente("c_2", "71982045", "Milagros", "Torres Vega", "944210987", "Av. Oscar R. Benavides 810", hoy));
        clienteRepository.save(new Cliente("c_3", "46223190", "Edwin", "Cárdenas Ponce", "987654321", "Calle Los Álamos 112, Pueblo Nuevo", hoy));

        motocicletaRepository.save(new Motocicleta("m_1", "c_1", "MTL-812", "Honda", "CB160F", 2023, 8420));
        motocicletaRepository.save(new Motocicleta("m_2", "c_2", "MTP-334", "Bajaj", "Pulsar NS200", 2022, 15230));
        motocicletaRepository.save(new Motocicleta("m_3", "c_3", "MTQ-556", "Honda", "XR150L", 2021, 22110));

        seedProducto("ACE-10W40", "Aceite motor 10W-40 (1L)", "Lubricantes", 32, 40, 10);
        seedProducto("FIL-AIR-01", "Filtro de aire universal", "Filtros", 18, 14, 5);
        seedProducto("PAS-DEL-01", "Pastillas de freno delanteras", "Frenos", 45, 6, 8);
        seedProducto("CAD-428H", "Cadena de transmisión 428H", "Transmisión", 95, 9, 4);
        seedProducto("BUJ-STD", "Bujía estándar", "Encendido", 12, 30, 10);
        seedProducto("LLA-TRAS-01", "Llanta trasera 100/90-17", "Llantas", 180, 3, 3);
    }

    private void seedProducto(String codigo, String nombre, String categoria, double precio, int stock, int stockMin) {
        Producto p = new Producto();
        p.id = IdGenerator.generar("prod");
        p.codigo = codigo;
        p.nombre = nombre;
        p.categoria = categoria;
        p.precio = precio;
        p.stockActual = stock;
        p.stockMinimo = stockMin;
        productoRepository.save(p);
    }
}
