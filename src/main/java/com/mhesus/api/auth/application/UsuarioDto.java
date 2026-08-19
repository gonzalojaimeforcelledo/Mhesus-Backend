package com.mhesus.api.auth.application;

import com.mhesus.api.auth.domain.Usuario;

public record UsuarioDto(String id, String nombre, String usuario, String rol, boolean activo) {
    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.id, u.nombre, u.usuario, u.rol, u.activo);
    }
}
