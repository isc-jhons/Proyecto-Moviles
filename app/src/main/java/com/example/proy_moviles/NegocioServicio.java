package com.example.proy_moviles;

import java.util.Map;

public class NegocioServicio {

    private String uidDueno;
    private String tipo;
    private String categoriaId;
    private String categoriaNombre;
    private String ruc;
    private String razonSocial;
    private String descripcion;
    private String whatsapp;
    private String telefono;
    private String referencia;
    private double latitud;
    private double longitud;
    private String imagenUrl;
    private Map<String, Object> horarios;
    private boolean activo;
    private long fechaRegistro;
    private String estado;
    private double estrellasPromedio;
    private int totalCalificaciones;
    private double sumaEstrellas;
    private String dni;
    private Map<String, Object> especialidades;
    private String nombreDueno;
    private String apellidosDueno;

    public NegocioServicio() {
    }

    public NegocioServicio(
            String uidDueno,
            String tipo,
            String categoriaId,
            String categoriaNombre,
            String ruc,
            String razonSocial,
            String descripcion,
            String whatsapp,
            String telefono,
            String referencia,
            double latitud,
            double longitud,
            String imagenUrl,
            Map<String, Object> horarios,
            boolean activo,
            long fechaRegistro,
            String estado,
            double estrellasPromedio,
            int totalCalificaciones,
            double sumaEstrellas,
            String dni,
            Map<String, Object> especialidades,
            String nombreDueno,
            String apellidosDueno
    ) {
        this.uidDueno = uidDueno;
        this.tipo = tipo;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.descripcion = descripcion;
        this.whatsapp = whatsapp;
        this.telefono = telefono;
        this.referencia = referencia;
        this.latitud = latitud;
        this.longitud = longitud;
        this.imagenUrl = imagenUrl;
        this.horarios = horarios;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.estrellasPromedio = estrellasPromedio;
        this.totalCalificaciones = totalCalificaciones;
        this.sumaEstrellas = sumaEstrellas;
        this.dni = dni;
        this.especialidades = especialidades;
        this.nombreDueno = nombreDueno;
        this.apellidosDueno = apellidosDueno;
    }

    public String getUidDueno() {
        return uidDueno;
    }
    public void setUidDueno(String uidDueno) {
        this.uidDueno = uidDueno;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public String getCategoriaId() {
        return categoriaId;
    }
    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }
    public String getCategoriaNombre() {
        return categoriaNombre;
    }
    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }
    public String getRuc() {
        return ruc;
    }
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
    public String getRazonSocial() {
        return razonSocial;
    }
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getWhatsapp() {
        return whatsapp;
    }
    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getReferencia() {
        return referencia;
    }
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    public double getLatitud() {
        return latitud;
    }
    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }
    public double getLongitud() {
        return longitud;
    }
    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
    public String getImagenUrl() {
        return imagenUrl;
    }
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    public Map<String, Object> getHorarios() {
        return horarios;
    }
    public void setHorarios(Map<String, Object> horarios) {
        this.horarios = horarios;
    }
    public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    public long getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public double getEstrellasPromedio() { return estrellasPromedio; }
    public void setEstrellasPromedio(double estrellasPromedio) { this.estrellasPromedio = estrellasPromedio; }
    public int getTotalCalificaciones() { return totalCalificaciones; }
    public void setTotalCalificaciones(int totalCalificaciones) { this.totalCalificaciones = totalCalificaciones; }
    public double getSumaEstrellas() { return sumaEstrellas; }
    public void setSumaEstrellas(double sumaEstrellas) { this.sumaEstrellas = sumaEstrellas; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public Map<String, Object> getEspecialidades() { return especialidades; }
    public void setEspecialidades(Map<String, Object> especialidades) { this.especialidades = especialidades; }
    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }
    public String getApellidosDueno() { return apellidosDueno; }
    public void setApellidosDueno(String apellidosDueno) { this.apellidosDueno = apellidosDueno; }
}