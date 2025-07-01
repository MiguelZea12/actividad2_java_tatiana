package com.puce.model;

import java.time.LocalDateTime;

public class Cliente {
    private Integer idCliente;
    private String nombreCliente;
    private String apellidoCliente;
    private String email;
    private String razonSocial;
    private String telefono;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public Cliente() {}
    
    public Cliente(String nombreCliente, String apellidoCliente, String email, 
                  String razonSocial, String telefono) {
        this.nombreCliente = nombreCliente;
        this.apellidoCliente = apellidoCliente;
        this.email = email;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
    }
    
    // Getters y Setters
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    
    public String getApellidoCliente() { return apellidoCliente; }
    public void setApellidoCliente(String apellidoCliente) { this.apellidoCliente = apellidoCliente; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    @Override
    public String toString() {
        return String.format("Cliente[id=%d, nombre=%s %s, email=%s, razonSocial=%s, telefono=%s]",
                idCliente, nombreCliente, apellidoCliente, email, razonSocial, telefono);
    }
}