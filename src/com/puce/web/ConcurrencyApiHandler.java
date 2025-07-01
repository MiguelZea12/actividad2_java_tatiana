package com.puce.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.puce.service.ConcurrencyTest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrencyApiHandler implements HttpHandler {
    private ConcurrencyTest concurrencyTest;
    private Gson gson;
    private ExecutorService executor;
    
    public ConcurrencyApiHandler() {
        this.concurrencyTest = new ConcurrencyTest();
        this.gson = new Gson();
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            switch (method) {
                case "OPTIONS":
                    exchange.sendResponseHeaders(200, -1);
                    break;
                case "POST":
                    handlePost(exchange, path);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/concurrency/test-inserts")) {
            ejecutarPruebaInserciones(exchange);
        } else if (path.equals("/api/concurrency/test-updates")) {
            ejecutarPruebaActualizaciones(exchange);
        } else if (path.equals("/api/concurrency/test-mixed")) {
            ejecutarPruebaMixta(exchange);
        } else if (path.equals("/api/concurrency/test-transactions")) {
            ejecutarPruebaTransacciones(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado");
        }
    }
    
    private void ejecutarPruebaInserciones(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prueba", "Inserciones Concurrentes");
            resultado.put("estado", "ejecutando");
            resultado.put("mensaje", "Ejecutando pruebas de concurrencia...");
            
            // Ejecutar de forma asíncrona
            CompletableFuture.runAsync(() -> {
                concurrencyTest.ejecutarTodasLasPruebas();
            }, executor);
            
            resultado.put("hilos", 20);
            resultado.put("operacion", "INSERT");
            
            sendJsonResponse(exchange, 200, resultado);
            
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error en prueba de inserciones: " + e.getMessage());
        }
    }
    
    private void ejecutarPruebaActualizaciones(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prueba", "Actualizaciones Concurrentes");
            resultado.put("estado", "ejecutando");
            resultado.put("mensaje", "Ejecutando pruebas de concurrencia (actualizaciones)...");
            
            // Ejecutar de forma asíncrona
            CompletableFuture.runAsync(() -> {
                concurrencyTest.ejecutarTodasLasPruebas();
            }, executor);
            
            resultado.put("hilos", 10);
            resultado.put("operacion", "UPDATE");
            
            sendJsonResponse(exchange, 200, resultado);
            
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error en prueba de actualizaciones: " + e.getMessage());
        }
    }
    
    private void ejecutarPruebaMixta(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prueba", "Operaciones Mixtas");
            resultado.put("estado", "ejecutando");
            resultado.put("mensaje", "Ejecutando operaciones mixtas (INSERT, UPDATE, SELECT)...");
            
            // Ejecutar de forma asíncrona
            CompletableFuture.runAsync(() -> {
                concurrencyTest.ejecutarTodasLasPruebas();
            }, executor);
            
            resultado.put("operaciones", new String[]{"INSERT", "UPDATE", "SELECT"});
            resultado.put("hilos", 50);
            
            sendJsonResponse(exchange, 200, resultado);
            
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error en prueba mixta: " + e.getMessage());
        }
    }
    
    private void ejecutarPruebaTransacciones(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("prueba", "Transacciones ACID");
            resultado.put("estado", "ejecutando");
            resultado.put("mensaje", "Probando propiedades ACID con transacciones concurrentes...");
            
            // Ejecutar de forma asíncrona
            CompletableFuture.runAsync(() -> {
                concurrencyTest.ejecutarTodasLasPruebas();
            }, executor);
            
            resultado.put("propiedades", new String[]{"Atomicidad", "Consistencia", "Aislamiento", "Durabilidad"});
            resultado.put("escenarios", new String[]{"Rollback", "Commit", "Deadlock", "Timeout"});
            
            sendJsonResponse(exchange, 200, resultado);
            
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error en prueba de transacciones: " + e.getMessage());
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        String jsonResponse = gson.toJson(errorResponse);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
} 