package com.puce.service;

import com.puce.dao.*;
import com.puce.model.Cliente;
import com.puce.model.LogError;
import java.util.List;
import java.util.Scanner;

public class ClienteService {
    private ClienteDAO clienteDAO;
    private ErrorLogDAO errorLogDAO;
    private Scanner scanner;
    
    public ClienteService() {
        this.clienteDAO = new ClienteDAO();
        this.errorLogDAO = new ErrorLogDAO();
        this.scanner = new Scanner(System.in);
    }
    
    public void mostrarMenu() {
        System.out.println("\n=== GESTIÓN DE CLIENTES ===");
        System.out.println("1. Insertar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Buscar cliente por ID");
        System.out.println("4. Actualizar cliente");
        System.out.println("5. Eliminar cliente");
        System.out.println("6. Mostrar estadísticas");
        System.out.println("7. Ejecutar pruebas de concurrencia");
        System.out.println("8. Ver errores recientes");
        System.out.println("9. Insertar clientes de ejemplo");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }
    
    public void insertarCliente() {
        System.out.println("\n=== INSERTAR CLIENTE ===");
        
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Razón Social (opcional): ");
        String razonSocial = scanner.nextLine();
        if (razonSocial.trim().isEmpty()) razonSocial = null;
        
        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine();
        
        Cliente cliente = new Cliente(nombre, apellido, email, razonSocial, telefono);
        
        if (clienteDAO.insertarCliente(cliente)) {
            System.out.println("✓ Cliente insertado correctamente");
        } else {
            System.out.println("✗ Error al insertar cliente");
        }
    }
    
    public void listarClientes() {
        System.out.println("\n=== LISTA DE CLIENTES ===");
        List<Cliente> clientes = clienteDAO.listarClientes();
        
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
        } else {
            System.out.printf("%-5s %-15s %-15s %-30s %-20s %-15s%n", 
                            "ID", "Nombre", "Apellido", "Email", "Razón Social", "Teléfono");
            System.out.println("-".repeat(100));
            
            for (Cliente cliente : clientes) {
                System.out.printf("%-5d %-15s %-15s %-30s %-20s %-15s%n",
                    cliente.getIdCliente(),
                    cliente.getNombreCliente(),
                    cliente.getApellidoCliente(),
                    cliente.getEmail(),
                    cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "N/A",
                    cliente.getTelefono());
            }
        }
    }
    
    public void buscarClientePorId() {
        System.out.println("\n=== BUSCAR CLIENTE POR ID ===");
        System.out.print("Ingrese el ID del cliente: ");
        
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Cliente cliente = clienteDAO.buscarClientePorId(id);
            
            if (cliente != null) {
                System.out.println("✓ Cliente encontrado:");
                System.out.println(cliente);
            } else {
                System.out.println("✗ No se encontró cliente con ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ ID inválido");
        }
    }
    
    public void actualizarCliente() {
        System.out.println("\n=== ACTUALIZAR CLIENTE ===");
        System.out.print("Ingrese el ID del cliente a actualizar: ");
        
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Cliente cliente = clienteDAO.buscarClientePorId(id);
            
            if (cliente == null) {
                System.out.println("✗ No se encontró cliente con ID: " + id);
                return;
            }
            
            System.out.println("Cliente actual: " + cliente);
            System.out.println("Ingrese los nuevos datos (presione Enter para mantener el valor actual):");
            
            System.out.print("Nombre [" + cliente.getNombreCliente() + "]: ");
            String nombre = scanner.nextLine();
            if (!nombre.trim().isEmpty()) {
                cliente.setNombreCliente(nombre);
            }
            
            System.out.print("Apellido [" + cliente.getApellidoCliente() + "]: ");
            String apellido = scanner.nextLine();
            if (!apellido.trim().isEmpty()) {
                cliente.setApellidoCliente(apellido);
            }
            
            System.out.print("Email [" + cliente.getEmail() + "]: ");
            String email = scanner.nextLine();
            if (!email.trim().isEmpty()) {
                cliente.setEmail(email);
            }
            
            System.out.print("Razón Social [" + (cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "N/A") + "]: ");
            String razonSocial = scanner.nextLine();
            if (!razonSocial.trim().isEmpty()) {
                cliente.setRazonSocial(razonSocial);
            }
            
            System.out.print("Teléfono [" + cliente.getTelefono() + "]: ");
            String telefono = scanner.nextLine();
            if (!telefono.trim().isEmpty()) {
                cliente.setTelefono(telefono);
            }
            
            if (clienteDAO.actualizarCliente(cliente)) {
                System.out.println("✓ Cliente actualizado correctamente");
            } else {
                System.out.println("✗ Error al actualizar cliente");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("✗ ID inválido");
        }
    }
    
    public void eliminarCliente() {
        System.out.println("\n=== ELIMINAR CLIENTE ===");
        System.out.print("Ingrese el ID del cliente a eliminar: ");
        
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Cliente cliente = clienteDAO.buscarClientePorId(id);
            
            if (cliente == null) {
                System.out.println("✗ No se encontró cliente con ID: " + id);
                return;
            }
            
            System.out.println("Cliente a eliminar: " + cliente);
            System.out.print("¿Está seguro? (s/N): ");
            String confirmacion = scanner.nextLine();
            
            if (confirmacion.toLowerCase().startsWith("s")) {
                if (clienteDAO.eliminarCliente(id)) {
                    System.out.println("✓ Cliente eliminado correctamente");
                } else {
                    System.out.println("✗ Error al eliminar cliente");
                }
            } else {
                System.out.println("Operación cancelada");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("✗ ID inválido");
        }
    }
    
    public void mostrarEstadisticas() {
        System.out.println("\n=== ESTADÍSTICAS DEL SISTEMA ===");
        
        int totalClientes = clienteDAO.contarClientes();
        int poolSize = com.puce.config.DatabaseConfig.getInstance().getPoolSize();
        
        System.out.println("Total de clientes: " + totalClientes);
        System.out.println("Conexiones disponibles en el pool: " + poolSize);
        
        // Mostrar errores recientes
        List<LogError> erroresRecientes = errorLogDAO.obtenerErroresRecientes(5);
        System.out.println("Errores recientes (" + erroresRecientes.size() + "):");
        
        if (erroresRecientes.isEmpty()) {
            System.out.println("  No hay errores recientes");
        } else {
            for (LogError error : erroresRecientes) {
                System.out.printf("  [%s] %s - %s%n", 
                    error.getFechaError().toString(), 
                    error.getOperacion(), 
                    error.getDescripcion());
            }
        }
    }
    
    public void verErroresRecientes() {
        System.out.println("\n=== ERRORES RECIENTES ===");
        List<LogError> errores = errorLogDAO.obtenerErroresRecientes(10);
        
        if (errores.isEmpty()) {
            System.out.println("No hay errores registrados.");
        } else {
            System.out.printf("%-5s %-20s %-15s %-50s %-20s%n", 
                            "ID", "Fecha", "Operación", "Descripción", "Usuario");
            System.out.println("-".repeat(110));
            
            for (LogError error : errores) {
                System.out.printf("%-5d %-20s %-15s %-50s %-20s%n",
                    error.getIdError(),
                    error.getFechaError().toString(),
                    error.getOperacion(),
                    error.getDescripcion().length() > 50 ? 
                        error.getDescripcion().substring(0, 47) + "..." : 
                        error.getDescripcion(),
                    error.getUsuarioAfectado());
            }
        }
    }
    
    public void insertarClientesEjemplo() {
        System.out.println("\n=== INSERTAR CLIENTES DE EJEMPLO ===");
        
        Cliente[] clientesEjemplo = {
            new Cliente("Ana", "Gómez", "ana.gomez@pucesm.com", "AnaTech S.A.", "0999999999"),
            new Cliente("Luis", "Pérez", "luis.perez@pucesm.com", "LuisCorp", "022223333"),
            new Cliente("María", "Rodríguez", "maria.rodriguez@pucesm.com", null, "0987654321"),
            new Cliente("Carlos", "Mendoza", "carlos.mendoza@pucesm.com", "CarlosTech Ltda.", "023334444"),
            new Cliente("Sofia", "García", "sofia.garcia@pucesm.com", "SofiaInnovation", "0988887777"),
            new Cliente("Pedro", "Martínez", "pedro.martinez@pucesm.com", null, "022445566")
        };
        
        int insertados = 0;
        for (Cliente cliente : clientesEjemplo) {
            if (clienteDAO.insertarCliente(cliente)) {
                insertados++;
            }
        }
        
        System.out.println("✓ " + insertados + " de " + clientesEjemplo.length + " clientes insertados");
    }
    
    public void ejecutarPruebasConcurrencia() {
        System.out.println("\n=== EJECUTANDO PRUEBAS DE CONCURRENCIA ===");
        System.out.println("Esto puede tomar varios minutos...");
        
        ConcurrencyTest concurrencyTest = new ConcurrencyTest();
        concurrencyTest.ejecutarTodasLasPruebas();
    }
}