package com.puce.dao;

import com.puce.config.DatabaseConfig;
import com.puce.model.Cliente;
import com.puce.model.LogError;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ClienteDAO {
    private DatabaseConfig dbConfig;
    private ErrorLogDAO errorLogDAO;
    
    public ClienteDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
        this.errorLogDAO = new ErrorLogDAO();
    }
    
    // Simular fallo de red aleatoriamente
    private void simularFalloRed() throws SQLException {
        if (ThreadLocalRandom.current().nextInt(100) < 10) { // 10% probabilidad
            throw new SQLException("Simulación de fallo de red", "08001");
        }
    }
    
    // Simular bloqueo de registros
    private void simularBloqueoRegistro() throws SQLException {
        if (ThreadLocalRandom.current().nextInt(100) < 15) { // 15% probabilidad
            throw new SQLException("Simulación de bloqueo de registro", "40001");
        }
    }
    
    public boolean insertarCliente(Cliente cliente) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Simular fallos
            simularFalloRed();
            simularBloqueoRegistro();
            
            conn = dbConfig.getConnection();
            
            String sql = "INSERT INTO clientes (nombre_cliente, apellido_cliente, email, razon_social, telefono) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cliente.getNombreCliente());
            stmt.setString(2, cliente.getApellidoCliente());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getRazonSocial());
            stmt.setString(5, cliente.getTelefono());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    cliente.setIdCliente(generatedKeys.getInt(1));
                }
                conn.commit();
                System.out.println("Cliente insertado exitosamente: " + cliente.getEmail());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
            
            // Registrar el error
            LogError logError = new LogError(
                e.getSQLState(),
                "Error al insertar cliente: " + e.getMessage(),
                e.toString(),
                "INSERT_CLIENTE",
                cliente.getEmail()
            );
            errorLogDAO.registrarError(logError);
            
            // Rollback
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Rollback ejecutado por error en inserción");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error en rollback: " + rollbackEx.getMessage());
                }
            }
            
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
    
    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            simularFalloRed();
            
            conn = dbConfig.getConnection();
            String sql = "SELECT * FROM clientes ORDER BY id_cliente";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getInt("id_cliente"));
                cliente.setNombreCliente(rs.getString("nombre_cliente"));
                cliente.setApellidoCliente(rs.getString("apellido_cliente"));
                cliente.setEmail(rs.getString("email"));
                cliente.setRazonSocial(rs.getString("razon_social"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setFechaCreacion(rs.getTimestamp("fecha_creacion") != null ? 
                                       rs.getTimestamp("fecha_creacion").toLocalDateTime() : null);
                cliente.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion") != null ? 
                                            rs.getTimestamp("fecha_actualizacion").toLocalDateTime() : null);
                
                clientes.add(cliente);
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
            
            LogError logError = new LogError(
                e.getSQLState(),
                "Error al listar clientes: " + e.getMessage(),
                e.toString(),
                "LISTAR_CLIENTES",
                "SISTEMA"
            );
            errorLogDAO.registrarError(logError);
            
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
        
        return clientes;
    }
    
    public Cliente buscarClientePorId(int idCliente) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            simularFalloRed();
            
            conn = dbConfig.getConnection();
            String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCliente);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getInt("id_cliente"));
                cliente.setNombreCliente(rs.getString("nombre_cliente"));
                cliente.setApellidoCliente(rs.getString("apellido_cliente"));
                cliente.setEmail(rs.getString("email"));
                cliente.setRazonSocial(rs.getString("razon_social"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setFechaCreacion(rs.getTimestamp("fecha_creacion") != null ? 
                                       rs.getTimestamp("fecha_creacion").toLocalDateTime() : null);
                cliente.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion") != null ? 
                                            rs.getTimestamp("fecha_actualizacion").toLocalDateTime() : null);
                
                conn.commit();
                return cliente;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
            
            LogError logError = new LogError(
                e.getSQLState(),
                "Error al buscar cliente por ID: " + e.getMessage(),
                e.toString(),
                "BUSCAR_CLIENTE_ID",
                String.valueOf(idCliente)
            );
            errorLogDAO.registrarError(logError);
            
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
        
        return null;
    }
    
    public boolean actualizarCliente(Cliente cliente) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            simularFalloRed();
            simularBloqueoRegistro();
            
            conn = dbConfig.getConnection();
            
            String sql = "UPDATE clientes SET nombre_cliente = ?, apellido_cliente = ?, " +
                        "email = ?, razon_social = ?, telefono = ?, fecha_actualizacion = CURRENT_TIMESTAMP " +
                        "WHERE id_cliente = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cliente.getNombreCliente());
            stmt.setString(2, cliente.getApellidoCliente());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getRazonSocial());
            stmt.setString(5, cliente.getTelefono());
            stmt.setInt(6, cliente.getIdCliente());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                conn.commit();
                System.out.println("Cliente actualizado exitosamente: " + cliente.getEmail());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            
            LogError logError = new LogError(
                e.getSQLState(),
                "Error al actualizar cliente: " + e.getMessage(),
                e.toString(),
                "ACTUALIZAR_CLIENTE",
                cliente.getEmail()
            );
            errorLogDAO.registrarError(logError);
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Rollback ejecutado por error en actualización");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error en rollback: " + rollbackEx.getMessage());
                }
            }
            
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
    
    public boolean eliminarCliente(int idCliente) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            simularFalloRed();
            simularBloqueoRegistro();
            
            conn = dbConfig.getConnection();
            
            String sql = "DELETE FROM clientes WHERE id_cliente = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCliente);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                conn.commit();
                System.out.println("Cliente eliminado exitosamente: ID " + idCliente);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            
            LogError logError = new LogError(
                e.getSQLState(),
                "Error al eliminar cliente: " + e.getMessage(),
                e.toString(),
                "ELIMINAR_CLIENTE",
                String.valueOf(idCliente)
            );
            errorLogDAO.registrarError(logError);
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Rollback ejecutado por error en eliminación");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error en rollback: " + rollbackEx.getMessage());
                }
            }
            
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
    
    public int contarClientes() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConfig.getConnection();
            String sql = "SELECT COUNT(*) FROM clientes";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al contar clientes: " + e.getMessage());
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
        
        return 0;
    }
}
