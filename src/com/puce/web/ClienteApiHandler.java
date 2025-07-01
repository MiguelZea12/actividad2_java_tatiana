package com.puce.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.puce.dao.ClienteDAO;
import com.puce.model.Cliente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ClienteApiHandler implements HttpHandler {
    private ClienteDAO clienteDAO;
    private Gson gson;
    
    public ClienteApiHandler() {
        this.clienteDAO = new ClienteDAO();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) 
                (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            switch (method) {
                case "OPTIONS":
                    exchange.sendResponseHeaders(200, -1);
                    break;
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange, path);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/clientes")) {
            // Listar todos los clientes
            List<Cliente> clientes = clienteDAO.listarClientes();
            sendJsonResponse(exchange, 200, clientes);
        } else if (path.startsWith("/api/clientes/")) {
            // Buscar cliente por ID
            try {
                String idStr = path.substring("/api/clientes/".length());
                int id = Integer.parseInt(idStr);
                Cliente cliente = clienteDAO.buscarClientePorId(id);
                
                if (cliente != null) {
                    sendJsonResponse(exchange, 200, cliente);
                } else {
                    sendErrorResponse(exchange, 404, "Cliente no encontrado");
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 400, "ID inválido");
            }
        } else {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado");
        }
    }
    
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Leer el cuerpo de la petición
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            // Convertir JSON a objeto Cliente
            Cliente cliente = gson.fromJson(requestBody.toString(), Cliente.class);
            
            if (cliente == null || cliente.getNombreCliente() == null || 
                cliente.getApellidoCliente() == null || cliente.getEmail() == null) {
                sendErrorResponse(exchange, 400, "Datos de cliente incompletos");
                return;
            }
            
            boolean resultado = clienteDAO.insertarCliente(cliente);
            
            if (resultado) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "Cliente creado exitosamente");
                sendJsonResponse(exchange, 201, response);
            } else {
                sendErrorResponse(exchange, 500, "Error al crear cliente");
            }
            
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "JSON inválido");
        }
    }
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (!path.startsWith("/api/clientes/")) {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado");
            return;
        }
        
        try {
            String idStr = path.substring("/api/clientes/".length());
            int id = Integer.parseInt(idStr);
            
            // Leer el cuerpo de la petición
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            // Convertir JSON a objeto Cliente
            Cliente cliente = gson.fromJson(requestBody.toString(), Cliente.class);
            cliente.setIdCliente(id);
            
            boolean resultado = clienteDAO.actualizarCliente(cliente);
            
            if (resultado) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "Cliente actualizado exitosamente");
                sendJsonResponse(exchange, 200, response);
            } else {
                sendErrorResponse(exchange, 500, "Error al actualizar cliente");
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "ID inválido");
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "JSON inválido");
        }
    }
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (!path.startsWith("/api/clientes/")) {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado");
            return;
        }
        
        try {
            String idStr = path.substring("/api/clientes/".length());
            int id = Integer.parseInt(idStr);
            
            boolean resultado = clienteDAO.eliminarCliente(id);
            
            if (resultado) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "Cliente eliminado exitosamente");
                sendJsonResponse(exchange, 200, response);
            } else {
                sendErrorResponse(exchange, 500, "Error al eliminar cliente");
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "ID inválido");
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