package com.puce;

import com.puce.service.ClienteService;
import com.puce.web.WebServer;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== APLICACIÓN JAVA - GESTIÓN DE CLIENTES ===");
        System.out.println("Conectando a la base de datos...");
        
        try {
            // Cargar el driver de PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            ClienteService clienteService = new ClienteService();
            WebServer webServer = new WebServer();
            Scanner scanner = new Scanner(System.in);
            
            // Crear tablas si no existen
            inicializarBaseDatos();
            
            // Mostrar opciones de inicio
            System.out.println("\n¿Cómo desea ejecutar la aplicación?");
            System.out.println("1. Interfaz de consola (tradicional)");
            System.out.println("2. Servidor web + interfaz web");
            System.out.println("3. Ambos (consola + web)");
            System.out.print("Seleccione una opción (1-3): ");
            
            int modoEjecucion = 1;
            try {
                modoEjecucion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opción inválida, usando interfaz de consola");
                modoEjecucion = 1;
            }
            
            // Iniciar servidor web si es necesario
            if (modoEjecucion == 2 || modoEjecucion == 3) {
                try {
                    webServer.start();
                    if (modoEjecucion == 2) {
                        System.out.println("\n✅ Servidor web iniciado exitosamente!");
                        System.out.println("🌐 Abra su navegador en: http://localhost:8080");
                        System.out.println("💡 Presione Enter para detener el servidor...");
                        scanner.nextLine();
                        webServer.stop();
                        scanner.close();
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error al iniciar servidor web: " + e.getMessage());
                    System.out.println("Continuando solo con interfaz de consola...");
                }
            }
            
            int opcion;
            do {
                clienteService.mostrarMenu();
                try {
                    opcion = Integer.parseInt(scanner.nextLine());
                    
                    switch (opcion) {
                        case 1:
                            clienteService.insertarCliente();
                            break;
                        case 2:
                            clienteService.listarClientes();
                            break;
                        case 3:
                            clienteService.buscarClientePorId();
                            break;
                        case 4:
                            clienteService.actualizarCliente();
                            break;
                        case 5:
                            clienteService.eliminarCliente();
                            break;
                        case 6:
                            clienteService.mostrarEstadisticas();
                            break;
                        case 7:
                            clienteService.ejecutarPruebasConcurrencia();
                            break;
                        case 8:
                            clienteService.verErroresRecientes();
                            break;
                        case 9:
                            clienteService.insertarClientesEjemplo();
                            break;
                        case 0:
                            System.out.println("Cerrando aplicación...");
                            break;
                        default:
                            System.out.println("Opción inválida");
                    }
                    
                    if (opcion != 0) {
                        System.out.print("\nPresione Enter para continuar...");
                        scanner.nextLine();
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Por favor ingrese un número válido");
                    opcion = -1;
                }
            } while (opcion != 0);
            
            // Cerrar scanner
            scanner.close();
            
            // Cerrar servidor web si está corriendo
            if (modoEjecucion == 3) {
                webServer.stop();
            }
            
            // Cerrar conexiones
            com.puce.config.DatabaseConfig.getInstance().closeAllConnections();
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver de PostgreSQL no encontrado");
            System.err.println("Asegúrese de tener postgresql-42.x.x.jar en el classpath");
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void inicializarBaseDatos() {
        System.out.println("Inicializando esquema de base de datos...");
        
        try (var conn = com.puce.config.DatabaseConfig.getInstance().getConnection();
             var stmt = conn.createStatement()) {
            
            // Crear tabla clientes si no existe
            String createClientesTable = """
                CREATE TABLE IF NOT EXISTS clientes (
                    id_cliente SERIAL PRIMARY KEY,
                    nombre_cliente VARCHAR(100) NOT NULL,
                    apellido_cliente VARCHAR(100) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    razon_social VARCHAR(255),
                    telefono VARCHAR(20),
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            // Crear tabla log_errores si no existe
            String createLogErroresTable = """
                CREATE TABLE IF NOT EXISTS log_errores (
                    id_error SERIAL PRIMARY KEY,
                    tipo_error VARCHAR(50),
                    descripcion TEXT NOT NULL,
                    stack_trace TEXT,
                    fecha_error TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    operacion VARCHAR(100),
                    usuario_afectado VARCHAR(255)
                )
            """;
            
            stmt.execute(createClientesTable);
            stmt.execute(createLogErroresTable);
            conn.commit();
            
            System.out.println("✓ Esquema de base de datos inicializado correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al inicializar base de datos: " + e.getMessage());
        }
    }
}