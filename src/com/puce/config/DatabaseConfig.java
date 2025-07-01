package com.puce.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/puce_postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "12345678";
    private static final int MIN_CONNECTIONS = 2;
    private static final int MAX_CONNECTIONS = 10;
    private static final int CONNECTION_TIMEOUT = 30;
    
    private static BlockingQueue<Connection> connectionPool;
    private static DatabaseConfig instance;
    
    private DatabaseConfig() {
        initializeConnectionPool();
    }
    
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    private void initializeConnectionPool() {
        connectionPool = new ArrayBlockingQueue<>(MAX_CONNECTIONS);
        
        try {
            // Crear conexiones iniciales
            for (int i = 0; i < MIN_CONNECTIONS; i++) {
                Connection conn = createConnection();
                connectionPool.offer(conn);
            }
            System.out.println("Pool de conexiones inicializado con " + MIN_CONNECTIONS + " conexiones");
        } catch (SQLException e) {
            System.err.println("Error al inicializar el pool de conexiones: " + e.getMessage());
        }
    }
    
    private Connection createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("ssl", "false");
        
        Connection conn = DriverManager.getConnection(DB_URL, props);
        conn.setAutoCommit(false); // Importante para transacciones
        return conn;
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed()) {
                conn = createConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Error al obtener conexión del pool", e);
        }
    }
    
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback(); // Asegurar rollback si no se commitó
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                System.err.println("Error al liberar conexión: " + e.getMessage());
            }
        }
    }
    
    public void closeAllConnections() {
        while (!connectionPool.isEmpty()) {
            try {
                Connection conn = connectionPool.poll();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
    
    public int getPoolSize() {
        return connectionPool.size();
    }
}