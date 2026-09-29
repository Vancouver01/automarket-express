package com.automarket.express.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.automarket.express.model.Categoria;
import com.automarket.express.model.Producto;
import com.automarket.express.model.Usuario;
import com.automarket.express.service.CategoriaService;
import com.automarket.express.service.ProductoService;
import com.automarket.express.service.UsuarioService;
import com.automarket.express.service.VentaService;

@Controller
public class HomeController {

    private final CategoriaService categoriaService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final VentaService ventaService;

    public HomeController(CategoriaService categoriaService, 
                          ProductoService productoService, 
                          UsuarioService usuarioService,
                          VentaService ventaService) {
        this.categoriaService = categoriaService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.ventaService = ventaService;
    }

    @GetMapping("/")
    public String login() { 
        return "login"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("id") String id, 
                                @RequestParam(value = "password", required = false) String password, 
                                Model model) {
        Usuario usuario = usuarioService.buscarPorId(id.trim());

        if (usuario == null || !usuario.isActivo()) {
            model.addAttribute("error", "Acceso denegado: Credenciales no válidas o cuenta suspendida.");
            return "login";
        }

        return "redirect:/main";
    }

    @GetMapping("/main")
    public String main() { 
        return "main"; 
    }

    @GetMapping("/caja")
    public String cajaAutopago(Model model) {

        List<String> categoriasActivas = categoriaService.listarTodas().stream()
                .filter(Categoria::isActivo)
                .map(Categoria::getNombre)
                .toList();

        List<Producto> disponibles = productoService.listarTodos().stream()
                .filter(p -> p.isActivo() && p.getStock() > 0 && categoriasActivas.contains(p.getCategoria()))
                .toList();

        model.addAttribute("productos", disponibles);
        model.addAttribute("carrito", ventaService.getCarrito());
        model.addAttribute("total", ventaService.calcularTotal());
        return "caja_autopago"; 
    }

    @PostMapping("/caja/agregar")
    public String agregarAlCarrito(@RequestParam("sku") String sku, RedirectAttributes redirectAttributes) {
        Producto producto = productoService.listarTodos().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku.trim()))
                .findFirst()
                .orElse(null);

        if (producto == null || !producto.isActivo()) {
            redirectAttributes.addFlashAttribute("error", "El producto no existe o se encuentra inactivo.");
            return "redirect:/caja";
        }

        Categoria categoria = categoriaService.listarTodas().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(producto.getCategoria()))
                .findFirst()
                .orElse(null);

        if (categoria == null || !categoria.isActivo()) {
            redirectAttributes.addFlashAttribute("error", "Venta bloqueada: La categoría del artículo está oculta (RN1).");
            return "redirect:/caja";
        }

        boolean agregado = ventaService.agregarProducto(producto, 1);
        if (!agregado) {
            redirectAttributes.addFlashAttribute("error", "Quiebre de existencias: Stock insuficiente para " + producto.getNombre() + " (RN2).");
            return "redirect:/caja";
        }

        return "redirect:/caja";
    }

    @PostMapping("/caja/finalizar")
    public String finalizarTransaccion() {
        if (ventaService.getCarrito().isEmpty()) {
            return "redirect:/caja";
        }

        ventaService.procesarVenta(productoService);
        return "redirect:/boleta";
    }

    @GetMapping("/boleta")
    public String boleta(Model model) { 
        model.addAttribute("items", ventaService.getUltimaVentaItems());
        model.addAttribute("total", ventaService.getUltimoTotal());
        model.addAttribute("comprobante", ventaService.getUltimoComprobante());
        return "boleta"; 
    }

    @GetMapping("/categorias")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "listar_categorias";
    }

    @GetMapping("/categorias/crear")
    public String crearCategoria() {
        return "crear_categoria";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@RequestParam("nombre") String nombre,
                                   @RequestParam("descripcion") String descripcion) {
        categoriaService.guardar(new Categoria(null, nombre, descripcion, true));
        return "redirect:/categorias";
    }

    @GetMapping("/categorias/editar/{id}")
    public String editarCategoria(@PathVariable("id") Integer id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        if (categoria == null) {
            return "redirect:/categorias";
        }
        model.addAttribute("categoria", categoria);
        return "editar_categoria";
    }

    @PostMapping("/categorias/actualizar")
    public String actualizarCategoria(@RequestParam("id") Integer id,
                                      @RequestParam("nombre") String nombre,
                                      @RequestParam("descripcion") String descripcion) {
        categoriaService.actualizar(id, nombre, descripcion);
        return "redirect:/categorias";
    }

    @GetMapping("/categorias/cambiar-estado/{id}")
    public String cambiarEstadoCategoria(@PathVariable("id") Integer id) {
        categoriaService.cambiarEstado(id);
        return "redirect:/categorias";
    }

    @GetMapping("/categorias/ocultar")
    public String vistaConfirmacionOcultarCategoria() { 
        return "ocultar_categoria"; 
    }

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "listar_productos";
    }

    @GetMapping("/productos/crear")
    public String crearProducto(Model model) {
        List<Categoria> activas = categoriaService.listarTodas().stream()
                .filter(Categoria::isActivo)
                .toList();
        model.addAttribute("categorias", activas);
        return "crear_producto";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@RequestParam("nombre") String nombre,
                                  @RequestParam("precio") Double precio,
                                  @RequestParam("stock") Integer stock,
                                  @RequestParam("categoria") String categoria) {
        String skuGenerado = "SKU-" + (int)(Math.random() * 900000 + 100000);
        productoService.guardar(new Producto(null, skuGenerado, nombre, precio, stock, categoria, true));
        return "redirect:/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable("id") Integer id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        if (producto == null) {
            return "redirect:/productos";
        }
        model.addAttribute("producto", producto);
        List<Categoria> activas = categoriaService.listarTodas().stream()
                .filter(Categoria::isActivo)
                .toList();
        model.addAttribute("categorias", activas);
        return "editar_producto";
    }

    @PostMapping("/productos/actualizar")
    public String actualizarProducto(@RequestParam("id") Integer id,
                                     @RequestParam("nombre") String nombre,
                                     @RequestParam("precio") Double precio,
                                     @RequestParam("stock") Integer stock,
                                     @RequestParam("categoria") String categoria) {
        productoService.actualizar(id, nombre, precio, stock, categoria);
        return "redirect:/productos";
    }

    @GetMapping("/productos/cambiar-estado/{id}")
    public String cambiarEstadoProducto(@PathVariable("id") Integer id) {
        productoService.cambiarEstado(id);
        return "redirect:/productos";
    }

    @GetMapping("/productos/ocultar")
    public String vistaConfirmacionOcultarProducto() { 
        return "ocultar_producto"; 
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "listar_usuarios";
    }

    @GetMapping("/usuarios/crear")
    public String crearUsuario() {
        return "crear_usuario";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam("nombre") String nombre,
                                 @RequestParam("correo") String correo,
                                 @RequestParam("rol") String rol) {
        String idGenerado = "EMP-000" + (usuarioService.listarTodos().size() + 1);
        usuarioService.guardar(new Usuario(idGenerado, nombre, correo, rol, true));
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable("id") String id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("usuario", usuario);
        return "editar_usuario";
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@RequestParam("id") String id,
                                    @RequestParam("nombre") String nombre,
                                    @RequestParam("correo") String correo,
                                    @RequestParam("rol") String rol) {
        usuarioService.actualizar(id, nombre, correo, rol);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/cambiar-estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable("id") String id) {
        usuarioService.cambiarEstado(id);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/ocultar")
    public String vistaConfirmacionOcultarUsuario() { 
        return "ocultar_usuario"; 
    }

    @GetMapping("/metricas")
    public String metricas() { 
        return "metricas"; 
    }

    @GetMapping("/publicidad")
    public String publicidad() { 
        return "publicidad"; 
    }

    @GetMapping("/contacto")
    public String contacto() { 
        return "contacto"; 
    }

   @PostMapping("/contacto")
public String procesarTicketSoporte(@RequestParam("nombre") String nombre,
                                    @RequestParam("correo") String correo,
                                    @RequestParam("mensaje") String mensaje) {
    System.out.println("Ticket registrado - Colaborador: " + nombre + " (" + correo + ") | Asunto: " + mensaje);
    return "redirect:/contacto?enviado=true";
}
}