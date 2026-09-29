package com.automarket.express.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.automarket.express.model.Usuario;

@Service
public class UsuarioService {

    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioService() {

        usuarios.add(new Usuario("EMP-0001", "Satoru Gojo", "u23257285@automarket.pe", "Cajero", true));
        usuarios.add(new Usuario("SUP-0001", "David Messi", "u23257286@automarket.pe", "Supervisor", true));
        usuarios.add(new Usuario("EMP-0003", "Michael Newman", "u23257287@automarket.pe", "Cajero", false));
    }

    public List<Usuario> listarTodos() {
        return usuarios;
    }

    public void guardar(Usuario usuario) {
        usuario.setActivo(true);
        usuarios.add(usuario);
    }

    public Usuario buscarPorId(String id) {
        return usuarios.stream()
                .filter(u -> u.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public void cambiarEstado(String id) {
        Usuario u = buscarPorId(id);
        if (u != null) {
            u.setActivo(!u.isActivo()); 
        }
    }

    public void actualizar(String id, String nombre, String correo, String rol) {
        Usuario u = buscarPorId(id);
        if (u != null) {
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setRol(rol);
        }
    }
}