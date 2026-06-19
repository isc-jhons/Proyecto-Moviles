package com.example.proy_moviles;

public class Publicacion {

    private String uidDueno;
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private long fecha;

    public Publicacion() {
    }

    public Publicacion(
            String uidDueno,
            String titulo,
            String descripcion,
            String imagenUrl,
            long fecha
    ) {
        this.uidDueno = uidDueno;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.fecha = fecha;
    }

    public String getUidDueno() {
        return uidDueno;
    }

    public void setUidDueno(String uidDueno) {
        this.uidDueno = uidDueno;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }
}