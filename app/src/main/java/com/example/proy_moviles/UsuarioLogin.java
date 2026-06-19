package com.example.proy_moviles;

public class UsuarioLogin {
    private String uid;
    private String nombre;
    private String apellidos;
    private String nickname;
    private String correo;
    private String telefono;
    private String rol;
    private long fechaRegistro;
    private boolean activo;
    private String fotoUrl;

    public UsuarioLogin() {
    }

    public UsuarioLogin(String uid,
                   String nombre,
                   String apellidos,
                   String nickname,
                   String correo,
                   String telefono,
                   String rol,
                   String fotoUrl,
                   long fechaRegistro,
                   boolean activo) {

        this.uid = uid;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.nickname = nickname;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
        this.fotoUrl = fotoUrl;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
