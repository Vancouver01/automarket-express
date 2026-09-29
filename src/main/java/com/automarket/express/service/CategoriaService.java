package com.automarket.express.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.automarket.express.model.Categoria;

@Service
public class CategoriaService {

    private final List<Categoria> categorias = new ArrayList<>();

    public CategoriaService() {

        categorias.add(new Categoria(1, "Bebidas", "Gaseosas, jugos y aguas embotelladas", true));
        categorias.add(new Categoria(2, "Snacks", "Galletas, papas fritas y golosinas", true));
        categorias.add(new Categoria(3, "Lácteos", "Leche, yogurt y quesos", false));
    }

    public List<Categoria> listarTodas() {
        return categorias;
    }

    public void guardar(Categoria categoria) {
        categoria.setId(categorias.size() + 1);
        categoria.setActivo(true);
        categorias.add(categoria);
    }

    public Categoria buscarPorId(Integer id) {
        return categorias.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void cambiarEstado(Integer id) {
        Categoria c = buscarPorId(id);
        if (c != null) {
            c.setActivo(!c.isActivo()); 
        }
    }
    public List<Categoria> listarActivas() {
    return categorias.stream()
            .filter(Categoria::isActivo)
            .toList();
}
    public void actualizar(Integer id, String nombre, String descripcion) {
        Categoria c = buscarPorId(id);
        if (c != null) {
            c.setNombre(nombre);
            c.setDescripcion(descripcion);
        }
    }
}