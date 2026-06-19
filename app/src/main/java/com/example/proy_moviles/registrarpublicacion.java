package com.example.proy_moviles;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class registrarpublicacion extends Fragment {

    EditText txtTituloPublicacion;
    EditText txtDescripcionPublicacion;
    EditText txtImagenPublicacion;
    ImageView imgPreviewPublicacion;
    Button btnPublicarNuevaPublicacion;
    Button btnCancelarPublicacion;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String idNegocioServicio = "";
    String tipo = "negocio";

    boolean imagenValida = false;
    String imagenValidadaUrl = "";

    public registrarpublicacion() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View vista = inflater.inflate(
                R.layout.fragment_registrarpublicacion,
                container,
                false
        );

        txtTituloPublicacion = vista.findViewById(R.id.txtTituloPublicacion);
        txtDescripcionPublicacion = vista.findViewById(R.id.txtDescripcionPublicacion);
        txtImagenPublicacion = vista.findViewById(R.id.txtImagenPublicacion);
        imgPreviewPublicacion = vista.findViewById(R.id.imgPreviewPublicacion);
        btnPublicarNuevaPublicacion = vista.findViewById(R.id.btnPublicarNuevaPublicacion);
        btnCancelarPublicacion = vista.findViewById(R.id.btnCancelarPublicacion);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            idNegocioServicio = getArguments().getString("idNegocioServicio", "");
            tipo = getArguments().getString("tipo", "negocio");
        }

        txtDescripcionPublicacion.setOnKeyListener((v, keyCode, event) -> {

            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER
                    && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {

                String texto =
                        txtDescripcionPublicacion.getText().toString();

                int enters =
                        texto.length() -
                                texto.replace("\n", "").length();

                // Ya hay 2 enters = 3 líneas
                if (enters >= 2) {
                    return true;
                }
            }

            return false;
        });
        txtDescripcionPublicacion.addTextChangedListener(new TextWatcher() {

            private String textoAnterior = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textoAnterior = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                txtDescripcionPublicacion.removeTextChangedListener(this);

                txtDescripcionPublicacion.post(() -> {
                    if (txtDescripcionPublicacion.getLineCount() > 3) {
                        txtDescripcionPublicacion.setText(textoAnterior);
                        txtDescripcionPublicacion.setSelection(
                                txtDescripcionPublicacion.getText().length()
                        );
                    }

                    txtDescripcionPublicacion.addTextChangedListener(this);
                });
            }
        });

        txtImagenPublicacion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();

                imagenValida = false;
                imagenValidadaUrl = "";

                if (!url.isEmpty() && url.startsWith("http")) {
                    Glide.with(requireContext())
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(imgPreviewPublicacion);
                }
            }
        });

        btnCancelarPublicacion.setOnClickListener(v -> volverEditar());

        btnPublicarNuevaPublicacion.setOnClickListener(v -> validarYPublicar());

        return vista;
    }

    private void validarYPublicar() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idNegocioServicio.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró el negocio o servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = txtTituloPublicacion.getText().toString().trim();
        String descripcion = txtDescripcionPublicacion.getText().toString().trim();



        String imagenUrl = txtImagenPublicacion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || imagenUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenUrl.startsWith("http")) {
            Toast.makeText(requireContext(), "El enlace ingresado no proyecta una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenValida || !imagenUrl.equals(imagenValidadaUrl)) {
            validarImagen(imagenUrl);
            return;
        }

        guardarPublicacion(titulo, descripcion, imagenUrl);
    }

    private void validarImagen(String imagenUrl) {
        Glide.with(requireContext())
                .load(imagenUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            GlideException e,
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
                        imagenValidadaUrl = imagenUrl;
                        validarYPublicar();
                        return false;
                    }
                })
                .preload();
    }

    private void guardarPublicacion(
            String titulo,
            String descripcion,
            String imagenUrl
    ) {

        String uidDueno = auth.getCurrentUser().getUid();

        Publicacion publicacion = new Publicacion(
                uidDueno,
                titulo,
                descripcion,
                imagenUrl,
                System.currentTimeMillis()
        );

        btnPublicarNuevaPublicacion.setEnabled(false);

        db.collection("negocios")
                .document(idNegocioServicio)
                .collection("publicaciones")
                .add(publicacion)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(
                            requireContext(),
                            "Publicación registrada",
                            Toast.LENGTH_SHORT
                    ).show();

                    volverEditar();
                })
                .addOnFailureListener(e -> {
                    btnPublicarNuevaPublicacion.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Error al publicar",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void volverEditar() {
        editarnegocioservicio fragment = new editarnegocioservicio();

        Bundle bundle = new Bundle();
        bundle.putString("tipo", tipo);

        fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .commit();
    }
}