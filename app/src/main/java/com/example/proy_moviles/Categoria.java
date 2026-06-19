package com.example.proy_moviles;

public class Categoria {

    private String nombre;
    private String iconUrl;
    private String color;
    private int orden;
    private boolean activo;

    public Categoria() {
    }

    public Categoria(String nombre, String iconUrl, String color,
                     int orden, boolean activo) {

        this.nombre = nombre;
        this.iconUrl = iconUrl;
        this.color = color;
        this.orden = orden;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}