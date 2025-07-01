package com.puce.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.puce.dao.ClienteDAO;
import com.puce.dao.ErrorLogDAO;
import com.puce.model.LogError;
import com.google.gson.Gson;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class StatsApiHandler implements HttpHandler {
    private ClienteDAO clienteDAO;
    private ErrorLogDAO errorLogDAO;
    private Gson gson;
    
    public StatsApiHandler() {
        this.clienteDAO = new ClienteDAO();
        this.errorLogDAO = new ErrorLogDAO();
        this.gson = new Gson();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        String method = exchange.getRequestMethod();
        
        try {
            switch (method) {
                case "OPTIONS":
                    exchange.sendResponseHeaders(200, -1);
                    break;
                case "GET":
                    handleGet(exchange);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            // Recopilar estadísticas del sistema
            Map<String, Object> stats = new HashMap<>();
            
            // Estadísticas de clientes
            int totalClientes = clienteDAO.contarClientes();
            stats.put("totalClientes", totalClientes);
            
            // Estadísticas de la base de datos
            int poolSize = com.puce.config.DatabaseConfig.getInstance().getPoolSize();
            stats.put("poolConexiones", poolSize);
            
            // Estadísticas de errores (últimos 50)
            List<LogError> erroresRecientes = errorLogDAO.obtenerErroresRecientes(50);
            stats.put("erroresRecientes", erroresRecientes.size());
            
            // Información del sistema
            Runtime runtime = Runtime.getRuntime();
            long memoriaTotal = runtime.totalMemory();
            long memoriaLibre = runtime.freeMemory();
            long memoriaUsada = memoriaTotal - memoriaLibre;
            
            Map<String, Object> memoria = new HashMap<>();
            memoria.put("total", formatBytes(memoriaTotal));
            memoria.put("usada", formatBytes(memoriaUsada));
            memoria.put("libre", formatBytes(memoriaLibre));
            memoria.put("porcentajeUso", Math.round((double) memoriaUsada / memoriaTotal * 100));
            
            stats.put("memoria", memoria);
            
            // Información de tiempo de actividad
            long tiempoInicio = java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
            long tiempoActual = System.currentTimeMillis();
            long uptime = tiempoActual - tiempoInicio;
            stats.put("tiempoActividad", formatUptime(uptime));
            
            sendJsonResponse(exchange, 200, stats);
            
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error al obtener estadísticas: " + e.getMessage());
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return Math.round(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return Math.round(bytes / (1024.0 * 1024)) + " MB";
        return Math.round(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }
    
    private String formatUptime(long millis) {
        long segundos = millis / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;
        
        if (dias > 0) {
            return dias + "d " + (horas % 24) + "h " + (minutos % 60) + "m";
        } else if (horas > 0) {
            return horas + "h " + (minutos % 60) + "m " + (segundos % 60) + "s";
        } else if (minutos > 0) {
            return minutos + "m " + (segundos % 60) + "s";
        } else {
            return segundos + "s";
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