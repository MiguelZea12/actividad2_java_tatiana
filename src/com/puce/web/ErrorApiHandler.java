package com.puce.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.puce.dao.ErrorLogDAO;
import com.puce.model.LogError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ErrorApiHandler implements HttpHandler {
    private ErrorLogDAO errorLogDAO;
    private Gson gson;
    
    public ErrorApiHandler() {
        this.errorLogDAO = new ErrorLogDAO();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) 
                (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();
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
        // Listar errores recientes (últimos 50)
        List<LogError> errores = errorLogDAO.obtenerErroresRecientes(50);
        sendJsonResponse(exchange, 200, errores);
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
        java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
        errorResponse.put("error", message);
        String jsonResponse = gson.toJson(errorResponse);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
} 