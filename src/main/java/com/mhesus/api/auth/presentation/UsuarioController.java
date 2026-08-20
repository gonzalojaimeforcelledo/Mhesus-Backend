package com.mhesus.api.auth.presentation;

import com.mhesus.api.auth.application.UsuarioCrearRequest;
import com.mhesus.api.auth.application.UsuarioDto;
import com.mhesus.api.auth.domain.Usuario;
import com.mhesus.api.auth.application.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return usuarioService.listar().stream().map(UsuarioDto::de).toList();
    }

    @GetMapping("/mecanicos")
    public List<UsuarioDto> mecanicos() {
        return usuarioService.mecanicosActivos().stream().map(UsuarioDto::de).toList();
    }

    @PostMapping
    public UsuarioDto crear(@RequestBody UsuarioCrearRequest req) {
        return UsuarioDto.de(usuarioService.crear(req));
    }

    @PatchMapping("/{id}/alternar")
    public UsuarioDto alternar(@PathVariable String id) {
        Usuario u = usuarioService.alternarActivo(id);
        return UsuarioDto.de(u);
    }

    @PatchMapping("/{id}/restablecer-password")
    public UsuarioDto restablecerPassword(@PathVariable String id, @RequestBody RestablecerPasswordRequest req) {
        Usuario u = usuarioService.restablecerPassword(id, req.nuevaPassword());
        return UsuarioDto.de(u);
    }

    public record RestablecerPasswordRequest(String nuevaPassword) {}
}
