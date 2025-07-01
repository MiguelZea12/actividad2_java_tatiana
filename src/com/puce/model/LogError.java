package com.puce.model;

import java.time.LocalDateTime;

public class LogError {
    private Integer idError;
    private String tipoError;
    private String descripcion;
    private String stackTrace;
    private LocalDateTime fechaError;
    private String operacion;
    private String usuarioAfectado;
    
    public LogError() {}
    
    public LogError(String tipoError, String descripcion, String stackTrace, 
                   String operacion, String usuarioAfectado) {
        this.tipoError = tipoError;
        this.descripcion = descripcion;
        this.stackTrace = stackTrace;
        this.operacion = operacion;
        this.usuarioAfectado = usuarioAfectado;
        this.fechaError = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Integer getIdError() { return idError; }
    public void setIdError(Integer idError) { this.idError = idError; }
    
    public String getTipoError() { return tipoError; }
    public void setTipoError(String tipoError) { this.tipoError = tipoError; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    
    public LocalDateTime getFechaError() { return fechaError; }
    public void setFechaError(LocalDateTime fechaError) { this.fechaError = fechaError; }
    
    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }
    
    public String getUsuarioAfectado() { return usuarioAfectado; }
    public void setUsuarioAfectado(String usuarioAfectado) { this.usuarioAfectado = usuarioAfectado; }
}
