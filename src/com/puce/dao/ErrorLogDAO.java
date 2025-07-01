package com.puce.dao;

import com.puce.config.DatabaseConfig;
import com.puce.model.LogError;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ErrorLogDAO {
    private DatabaseConfig dbConfig;
    
    public ErrorLogDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    public boolean registrarError(LogError logError) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConfig.getConnection();
            
            String sql = "INSERT INTO log_errores (tipo_error, descripcion, stack_trace, " +
                        "fecha_error, operacion, usuario_afectado) VALUES (?, ?, ?, ?, ?, ?)";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, logError.getTipoError());
            stmt.setString(2, logError.getDescripcion());
            stmt.setString(3, logError.getStackTrace());
            stmt.setTimestamp(4, Timestamp.valueOf(logError.getFechaError()));
            stmt.setString(5, logError.getOperacion());
            stmt.setString(6, logError.getUsuarioAfectado());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                conn.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al registrar log de error: " + e.getMessage());
            // No registramos este error para evitar recursión infinita
            
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (conn != null) {
                dbConfig.releaseConnection(conn);
            }
        }
        
        return false;
    }
    
    public List<LogError> obtenerErroresRecientes(int limite) {
        List<LogError> errores = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConfig.getConnection();
            String sql = "SELECT * FROM log_errores ORDER BY fecha_error DESC LIMIT ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limite);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                LogError error = new LogError();
                error.setIdError(rs.getInt("id_error"));
                error.setTipoError(rs.getString("tipo_error"));
                error.setDescripcion(rs.getString("descripcion"));
                error.setStackTrace(rs.getString("stack_trace"));
                error.setFechaError(rs.getTimestamp("fecha_error").toLocalDateTime());
                error.setOperacion(rs.getString("operacion"));
                error.setUsuarioAfectado(rs.getString("usuario_afectado"));
                
                errores.add(error);
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener errores recientes: " + e.getMessage());
            
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (conn != null) {
                dbConfig.releaseConnection(conn);
            }
        }
        
        return errores;
    }
}