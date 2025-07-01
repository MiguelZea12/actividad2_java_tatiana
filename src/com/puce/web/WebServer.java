package com.puce.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class WebServer {
    private HttpServer server;
    private static final int PORT = 8080;
    
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Configurar rutas
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/clientes", new ClienteApiHandler());
        server.createContext("/api/errores", new ErrorApiHandler());
        server.createContext("/api/stats", new StatsApiHandler());
        server.createContext("/api/concurrency", new ConcurrencyApiHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🌐 Servidor web iniciado en: http://localhost:" + PORT);
        System.out.println("📱 Interfaz web disponible en: http://localhost:" + PORT + "/index.html");
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Servidor web detenido");
        }
    }
    
    // Handler para archivos estáticos (HTML, CSS, JS)
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Redirigir root a index.html
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            try {
                // Buscar archivo en directorio web
                Path filePath = Paths.get("web" + path);
                
                if (Files.exists(filePath)) {
                    byte[] content = Files.readAllBytes(filePath);
                    String contentType = getContentType(path);
                    
                    exchange.getResponseHeaders().add("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, content.length);
                    
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(content);
                    }
                } else {
                    // Archivo no encontrado
                    String response = "404 - Archivo no encontrado";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } catch (Exception e) {
                String response = "500 - Error interno del servidor";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }
} 