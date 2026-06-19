package com.example.proy_moviles;

public class Calificacion {

    private String uidUsuario;
    private double estrellas;

    public Calificacion() {
    }

    public Calificacion(String uidUsuario, double estrellas) {
        this.uidUsuario = uidUsuario;
        this.estrellas = estrellas;
    }
    public String getUidUsuario() {
        return uidUsuario;
    }
    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }
    public double getEstrellas() {
        return estrellas;
    }
    public void setEstrellas(double estrellas) {
        this.estrellas = estrellas;
    }
}