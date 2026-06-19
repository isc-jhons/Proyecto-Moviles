package com.example.proy_moviles;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import android.widget.Button;

import java.util.ArrayList;


class PublicacionInicio {

    Publicacion publicacion;
    String idNegocio;

    PublicacionInicio(
            Publicacion publicacion,
            String idNegocio
    ) {
        this.publicacion = publicacion;
        this.idNegocio = idNegocio;
    }
}
public class inicio extends Fragment {

    LinearLayout listaPublicacionesInicio;
    GridLayout gridCategorias;
    FirebaseFirestore db;
    ArrayList<Categoria> categoriasCache = new ArrayList<>();

    Button btnMapa;
    ListenerRegistration publicacionesListener;

    public inicio() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_inicio, container, false);
        gridCategorias = vista.findViewById(R.id.gridCategorias);
        db = FirebaseFirestore.getInstance();
        btnMapa = vista.findViewById(R.id.btnMapa);

        listaPublicacionesInicio = vista.findViewById(R.id.listaPublicacionesInicio);

        btnMapa.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new listasn())
                    .commit();
        });

        cargarCategorias();
        cargarUltimasPublicaciones();

        return vista;
    }

    private void cargarCategorias() {

        if (publicacionesListener != null) {
            publicacionesListener.remove();
        }
        db.collection("categorias")
                .orderBy("orden")
                .addSnapshotListener((query, error) -> {
                    if (error != null) {
                        Toast.makeText(
                                requireContext(),
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                    if (query == null) {
                        return;
                    }
                    categoriasCache.clear();
                    gridCategorias.removeAllViews();
                    query.forEach(documento -> {
                        Categoria categoria =
                                documento.toObject(Categoria.class);

                        categoriasCache.add(categoria);
                        crearBotonCategoria(
                                categoria.getNombre(),
                                categoria.getIconUrl(),
                                categoria.getColor()
                        );
                    });
                });
    }

    private void crearBotonCategoria(String nombre, String iconoUrl, String color) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(8, 8, 8, 8);

        android.graphics.drawable.GradientDrawable fondo =
                new android.graphics.drawable.GradientDrawable();

        try {
            fondo.setColor(Color.parseColor(color));
        } catch (Exception e) {
            fondo.setColor(Color.GRAY);
        }
        fondo.setCornerRadius(25);
        card.setBackground(fondo);
        card.setClickable(true);
        card.setFocusable(true);
        android.util.TypedValue outValue = new android.util.TypedValue();

        requireContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
        );
        card.setForeground(requireContext().getDrawable(outValue.resourceId));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 220;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        card.setLayoutParams(params);

        ImageView icono = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(90, 90);
        icono.setLayoutParams(iconParams);
        icono.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Glide.with(requireContext())
                .load(iconoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(icono);

        TextView texto = new TextView(requireContext());
        texto.setText(nombre);
        texto.setTextColor(Color.WHITE);
        texto.setTextSize(15);
        texto.setGravity(Gravity.CENTER);
        texto.setTypeface(null, Typeface.BOLD);

        card.addView(icono);
        card.addView(texto);

        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("categoriaSeleccionada", nombre);

            listasn fragment = new listasn();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, fragment)
                    .commit();
        });

        gridCategorias.addView(card);
    }

    private void cargarUltimasPublicaciones() {

        listaPublicacionesInicio.removeAllViews();

        TextView cargando = new TextView(requireContext());
        cargando.setText("Cargando publicaciones...");
        cargando.setTextColor(Color.GRAY);
        cargando.setGravity(Gravity.CENTER);
        cargando.setPadding(0, 20, 0, 20);
        listaPublicacionesInicio.addView(cargando);

        publicacionesListener =
                db.collectionGroup("publicaciones")
                        .addSnapshotListener((query, error) -> {

                    if (error != null) {
                        Toast.makeText(
                                requireContext(),
                                "Error publicaciones: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (query == null) return;

                    listaPublicacionesInicio.removeAllViews();

                    ArrayList<PublicacionInicio> publicaciones = new ArrayList<>();

                    for (var doc : query.getDocuments()) {
                        Publicacion p = doc.toObject(Publicacion.class);

                        if (p != null) {

                            String idNegocio =
                                    doc.getReference()
                                            .getParent()
                                            .getParent()
                                            .getId();

                            publicaciones.add(
                                    new PublicacionInicio(
                                            p,
                                            idNegocio
                                    )
                            );
                        }
                    }

                    publicaciones.sort((a, b) ->
                            Long.compare(
                                    b.publicacion.getFecha(),
                                    a.publicacion.getFecha()
                            )
                    );

                    if (publicaciones.isEmpty()) {
                        TextView vacio = new TextView(requireContext());
                        vacio.setText("Aún no hay publicaciones");
                        vacio.setTextColor(Color.GRAY);
                        vacio.setGravity(Gravity.CENTER);
                        vacio.setPadding(0, 20, 0, 20);
                        listaPublicacionesInicio.addView(vacio);
                        return;
                    }

                    int limite = Math.min(10, publicaciones.size());

                    for (int i = 0; i < limite; i++) {

                        PublicacionInicio item = publicaciones.get(i);

                        crearPublicacionInicio(
                                item.publicacion.getTitulo(),
                                item.publicacion.getDescripcion(),
                                item.publicacion.getImagenUrl(),
                                item.idNegocio
                        );
                    }
                });
    }

    private void crearPublicacionInicio(
            String titulo,
            String descripcion,
            String imagenUrl,
            String idNegocio
    ) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams paramsCard =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        paramsCard.setMargins(10, 0, 10, 20);
        card.setLayoutParams(paramsCard);

        ImageView imagen = new ImageView(requireContext());
        imagen.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        390
                )
        );
        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(imagen.getContext())
                .load(imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imagen);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setBackgroundColor(Color.parseColor("#D9D9D9"));
        info.setPadding(10, 6, 10, 6);

        TextView txtTitulo = new TextView(requireContext());
        txtTitulo.setText(titulo);
        txtTitulo.setTypeface(null, Typeface.BOLD);
        txtTitulo.setTextColor(Color.BLACK);
        txtTitulo.setTextSize(13);

        TextView txtDescripcion = new TextView(requireContext());
        txtDescripcion.setText(descripcion);
        txtDescripcion.setTextColor(Color.BLACK);
        txtDescripcion.setTextSize(11);
        txtDescripcion.setMaxLines(3);
        txtDescripcion.setEllipsize(android.text.TextUtils.TruncateAt.END);

        info.addView(txtTitulo);
        info.addView(txtDescripcion);

        card.addView(imagen);
        card.addView(info);

        listaPublicacionesInicio.addView(card);

        card.setClickable(true);

        card.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("idNegocioServicio", idNegocio);

            perfilNegocioServico fragment = new perfilNegocioServico();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, fragment)
                    .commit();
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (publicacionesListener != null) {
            publicacionesListener.remove();
            publicacionesListener = null;
        }
    }
}