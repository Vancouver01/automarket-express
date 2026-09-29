package com.automarket.express.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.automarket.express.model.ItemCarrito;
import com.automarket.express.model.Producto;

@Service
public class VentaService {

    private final List<ItemCarrito> carrito = new ArrayList<>();
    private final List<ItemCarrito> ultimaVentaItems = new ArrayList<>();
    private int contadorBoletas = 1;
    private String ultimoComprobante = "B001-000001";
    private double ultimoTotal = 0.0;

    public List<ItemCarrito> getCarrito() { 
        return carrito; 
    }

    public boolean agregarProducto(Producto producto, int cantidad) {
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getId().equals(producto.getId())) {
                if (item.getCantidad() + cantidad > producto.getStock()) {
                    return false; 
                }
                item.setCantidad(item.getCantidad() + cantidad);
                return true;
            }
        }
        if (cantidad > producto.getStock()) {
            return false;
        }
        carrito.add(new ItemCarrito(producto, cantidad));
        return true;
    }

    public double calcularTotal() {
        return carrito.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
    }

    public void procesarVenta(ProductoService productoService) {

        for (ItemCarrito item : carrito) {
            Producto p = productoService.buscarPorId(item.getProducto().getId());
            if (p != null) {
                p.setStock(p.getStock() - item.getCantidad());
            }
        }

        this.ultimoComprobante = String.format("B001-%06d", contadorBoletas++);

        this.ultimoTotal = calcularTotal();
        this.ultimaVentaItems.clear();
        this.ultimaVentaItems.addAll(carrito);

        this.carrito.clear();
    }

    public List<ItemCarrito> getUltimaVentaItems() { 
        return ultimaVentaItems; 
    }

    public double getUltimoTotal() { 
        return ultimoTotal; 
    }

    public String getUltimoComprobante() { 
        return ultimoComprobante; 
    }

    public void limpiarCarrito() {
        this.carrito.clear();
    }
}