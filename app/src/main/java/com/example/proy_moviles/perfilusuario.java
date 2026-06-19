package com.example.proy_moviles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class perfilusuario extends Fragment {

    ImageView imgPerfilUsuario;
    EditText txtFotoUrlPerfil, txtNombrePerfil, txtApellidoPerfil,
            txtNicknamePerfil, txtCorreoPerfil, txtTelefonoPerfil,
            txtPasswordPerfil;

    Button btnCambiarFoto, btnGuardarPerfil;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String uid;
    boolean imagenValida = true;
    String imagenValidadaUrl = "";

    public perfilusuario() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_perfilusuario, container, false);

        imgPerfilUsuario = vista.findViewById(R.id.imgPerfilUsuario);
        txtFotoUrlPerfil = vista.findViewById(R.id.txtFotoUrlPerfil);
        txtNombrePerfil = vista.findViewById(R.id.txtNombrePerfil);
        txtApellidoPerfil = vista.findViewById(R.id.txtApellidoPerfil);
        txtNicknamePerfil = vista.findViewById(R.id.txtNicknamePerfil);
        txtCorreoPerfil = vista.findViewById(R.id.txtCorreoPerfil);
        txtTelefonoPerfil = vista.findViewById(R.id.txtTelefonoPerfil);
        txtPasswordPerfil = vista.findViewById(R.id.txtPasswordPerfil);

        btnCambiarFoto = vista.findViewById(R.id.btnCambiarFoto);
        btnGuardarPerfil = vista.findViewById(R.id.btnGuardarPerfil);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión", Toast.LENGTH_SHORT).show();
            return vista;
        }

        uid = auth.getCurrentUser().getUid();

        cargarDatosUsuario();

        btnCambiarFoto.setOnClickListener(v -> {
            txtFotoUrlPerfil.setVisibility(View.VISIBLE);
        });

        txtFotoUrlPerfil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();

                imagenValida = false;
                imagenValidadaUrl = "";

                if (!url.isEmpty() && url.startsWith("http")) {
                    cargarImagen(url);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        activarEdicion(txtNombrePerfil, false);
        activarEdicion(txtApellidoPerfil, false);
        activarEdicion(txtNicknamePerfil, false);
        activarEdicion(txtCorreoPerfil, false);
        activarEdicion(txtTelefonoPerfil, false);
        activarEdicion(txtPasswordPerfil, true);

        btnGuardarPerfil.setOnClickListener(v -> guardarCambios());

        return vista;
    }

    private void cargarDatosUsuario() {
        txtPasswordPerfil.setText("********");
        db.collection("usuariologin")
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {
                    UsuarioLogin usuario = documento.toObject(UsuarioLogin.class);

                    if (usuario == null) {
                        Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    txtNombrePerfil.setText(usuario.getNombre());
                    txtApellidoPerfil.setText(usuario.getApellidos());
                    txtNicknamePerfil.setText(usuario.getNickname());
                    txtCorreoPerfil.setText(usuario.getCorreo());
                    txtTelefonoPerfil.setText(usuario.getTelefono());

                    if (usuario.getFotoUrl() != null && !usuario.getFotoUrl().isEmpty()) {
                        txtFotoUrlPerfil.setText(usuario.getFotoUrl());
                        imagenValida = true;
                        imagenValidadaUrl = usuario.getFotoUrl();
                        cargarImagen(usuario.getFotoUrl());
                    } else {
                        imgPerfilUsuario.setImageResource(R.drawable.perfilusuario);
                    }
                });
    }

    private void cargarImagen(String url) {
        Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.perfilusuario)
                .error(R.drawable.perfilusuario)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        imagenValida = false;
                        imagenValidadaUrl = "";
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        imagenValida = true;
                        imagenValidadaUrl = url;
                        return false;
                    }
                })
                .into(imgPerfilUsuario);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void activarEdicion(EditText editText, boolean esPassword) {

        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setCursorVisible(false);

        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                int drawableEnd = 2;

                if (editText.getCompoundDrawables()[drawableEnd] != null &&
                        event.getRawX() >= editText.getRight()
                                - editText.getCompoundDrawables()[drawableEnd].getBounds().width()
                                - editText.getPaddingEnd()) {

                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    editText.setCursorVisible(true);
                    editText.requestFocus();

                    if (esPassword) {
                        editText.setText("");
                        editText.setHint("Nueva contraseña");
                    }

                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }

            return true;
        });
    }

    private void guardarCambios() {

        String fotoUrl = txtFotoUrlPerfil.getText().toString().trim();

        String passwordTemporal =
                txtPasswordPerfil.getText().toString().trim();

        if (passwordTemporal.equals("********")) {
            passwordTemporal = "";
        }

        final String nuevaPassword = passwordTemporal;

        if (!nuevaPassword.isEmpty() && nuevaPassword.length() < 6) {
            Toast.makeText(
                    requireContext(),
                    "La contraseña debe tener mínimo 6 caracteres",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String nombre = txtNombrePerfil.getText().toString().trim();
        String apellido = txtApellidoPerfil.getText().toString().trim();
        String nickname = txtNicknamePerfil.getText().toString().trim();
        String correo = txtCorreoPerfil.getText().toString().trim();
        String telefono = txtTelefonoPerfil.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || nickname.isEmpty()
                || correo.isEmpty() || telefono.isEmpty()) {

            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.length() > 40 || apellido.length() > 40
                || nickname.length() > 40 || correo.length() > 40) {

            Toast.makeText(requireContext(), "Los campos no deben superar 40 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!telefono.matches("\\d{9}")) {
            Toast.makeText(requireContext(), "El teléfono debe tener 9 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fotoUrl.isEmpty()) {

            if (!fotoUrl.startsWith("http")) {
                Toast.makeText(
                        requireContext(),
                        "El enlace ingresado no proyecta una imagen",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (!imagenValida || !fotoUrl.equals(imagenValidadaUrl)) {
                validarImagenAntesDeGuardar(fotoUrl);
                return;
            }
        }

        validarUnicosYGuardar(
                nombre,
                apellido,
                nickname,
                correo,
                telefono,
                fotoUrl,
                nuevaPassword
        );
    }

    private void validarUnicosYGuardar(
            String nombre,
            String apellido,
            String nickname,
            String correo,
            String telefono,
            String fotoUrl,
            String nuevaPassword
    ) {

        db.collection("usuariologin")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryNick -> {

                    for (var doc : queryNick) {
                        if (!doc.getId().equals(uid)) {
                            Toast.makeText(
                                    requireContext(),
                                    "El nickname ya existe",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                    }

                    db.collection("usuariologin")
                            .whereEqualTo("correo", correo)
                            .get()
                            .addOnSuccessListener(queryCorreo -> {

                                for (var doc : queryCorreo) {
                                    if (!doc.getId().equals(uid)) {
                                        Toast.makeText(
                                                requireContext(),
                                                "El correo ya existe",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        return;
                                    }
                                }

                                guardarFirestore(
                                        nombre,
                                        apellido,
                                        nickname,
                                        correo,
                                        telefono,
                                        fotoUrl,
                                        nuevaPassword
                                );
                            });
                });
    }

    private void guardarFirestore(
            String nombre,
            String apellido,
            String nickname,
            String correo,
            String telefono,
            String fotoUrl,
            String nuevaPassword
    ) {

        db.collection("usuariologin")
                .document(uid)
                .update(
                        "nombre", nombre,
                        "apellidos", apellido,
                        "nickname", nickname,
                        "correo", correo,
                        "telefono", telefono,
                        "fotoUrl", fotoUrl
                )
                .addOnSuccessListener(unused -> {

                    if (!nuevaPassword.isEmpty()) {
                        auth.getCurrentUser().updatePassword(nuevaPassword);
                    }

                    desactivarEdicion(txtNombrePerfil);
                    desactivarEdicion(txtApellidoPerfil);
                    desactivarEdicion(txtNicknamePerfil);
                    desactivarEdicion(txtCorreoPerfil);
                    desactivarEdicion(txtTelefonoPerfil);
                    desactivarEdicion(txtPasswordPerfil);

                    txtPasswordPerfil.setText("********");
                    txtFotoUrlPerfil.setVisibility(View.GONE);

                    Toast.makeText(
                            requireContext(),
                            "Perfil actualizado",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void validarImagenAntesDeGuardar(String fotoUrl) {

        Glide.with(requireContext())
                .load(fotoUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        imagenValida = false;
                        imagenValidadaUrl = "";

                        Toast.makeText(
                                requireContext(),
                                "El enlace ingresado no proyecta una imagen",
                                Toast.LENGTH_SHORT
                        ).show();

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        imagenValida = true;
                        imagenValidadaUrl = fotoUrl;
                        guardarCambios();
                        return false;
                    }
                })
                .preload();
    }
    private void desactivarEdicion(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }
}