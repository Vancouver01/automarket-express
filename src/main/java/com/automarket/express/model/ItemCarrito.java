package com.automarket.express.model;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;
    private double subtotal;

    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = producto.getPrecio() * cantidad;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getSubtotal() { return subtotal; }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = this.producto.getPrecio() * cantidad;
    }
}