package com.example.proy_moviles;

import java.util.Map;

public class Comentario {

    private String uidUsuario;
    private String nombreUsuario;
    private String comentario;
    private long fecha;
    private String fotoUrl;
    private int likes;
    private Map<String, Boolean> usuariosLike;

    public Comentario() {
    }

    public Comentario(String uidUsuario, String nombreUsuario, String comentario,
                      long fecha, String fotoUrl, int likes,
                      Map<String, Boolean> usuariosLike) {
        this.uidUsuario = uidUsuario;
        this.nombreUsuario = nombreUsuario;
        this.comentario = comentario;
        this.fecha = fecha;
        this.fotoUrl = fotoUrl;
        this.likes = likes;
        this.usuariosLike = usuariosLike;
    }

    public String getUidUsuario() { return uidUsuario; }
    public void setUidUsuario(String uidUsuario) { this.uidUsuario = uidUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public Map<String, Boolean> getUsuariosLike() { return usuariosLike; }
    public void setUsuariosLike(Map<String, Boolean> usuariosLike) { this.usuariosLike = usuariosLike; }
}