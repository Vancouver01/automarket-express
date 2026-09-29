package com.automarket.express.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.automarket.express.model.Producto;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();

    public ProductoService() {

        productos.add(new Producto(1, "7750001", "Inca Kola 600ml", 3.50, 120, "Bebidas", true));
productos.add(new Producto(2, "4901005", "Pocky de Matcha", 6.90, 12, "Snacks", true));
productos.add(new Producto(3, "7751112", "Galletas Morochas", 1.50, 45, "Snacks", true));
productos.add(new Producto(4, "7752223", "Leche Gloria 1L", 5.20, 24, "Lácteos", true));
    }

    public List<Producto> listarTodos() {
        return productos;
    }

    public void guardar(Producto producto) {
        producto.setId(productos.size() + 1);
        producto.setActivo(true);
        productos.add(producto);
    }

    public Producto buscarPorId(Integer id) {
        return productos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void cambiarEstado(Integer id) {
        Producto p = buscarPorId(id);
        if (p != null) {
            p.setActivo(!p.isActivo());
        }
    }
    
public List<Producto> listarDisponiblesParaVenta() {
    return productos.stream()
            .filter(p -> p.isActivo() && p.getStock() > 0)
            .toList();
}
    public void actualizar(Integer id, String nombre, Double precio, Integer stock, String categoria) {
        Producto p = buscarPorId(id);
        if (p != null) {
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setCategoria(categoria);
        }
    }
}