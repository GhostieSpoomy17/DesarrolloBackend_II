package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.dto.RegistroUsuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario registrarUsuario(RegistroUsuario usuario);
    Optional<Usuario> obtenerUsuarioPorId(Long id);
    Optional<Usuario> obtenerUsuarioPorEmail(String email);
    Usuario actualizarUsuario(Long id, Usuario usuario);
    void eliminarUsuario(Long id);
    List<Usuario> listarUsuarios();
}
