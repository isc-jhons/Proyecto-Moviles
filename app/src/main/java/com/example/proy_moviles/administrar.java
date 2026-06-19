package com.example.proy_moviles;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class administrar extends Fragment {

    LinearLayout contenedorSolicitudes;
    FirebaseFirestore db;

    public administrar() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_administrar, container, false);

        contenedorSolicitudes = vista.findViewById(R.id.contenedorSolicitudes);
        db = FirebaseFirestore.getInstance();

        cargarSolicitudes();

        return vista;
    }

    private void cargarSolicitudes() {
        db.collection("negocios")
                .get()
                .addOnSuccessListener(query -> {

                    contenedorSolicitudes.removeAllViews();

                    java.util.ArrayList<com.google.firebase.firestore.DocumentSnapshot> lista =
                            new java.util.ArrayList<>();

                    for (var documento : query) {
                        lista.add(documento);
                    }

                    lista.sort((doc1, doc2) -> {
                        NegocioServicio n1 = doc1.toObject(NegocioServicio.class);
                        NegocioServicio n2 = doc2.toObject(NegocioServicio.class);

                        if (n1 == null || n2 == null) {
                            return 0;
                        }

                        int estado1 = prioridadEstado(n1.getEstado());
                        int estado2 = prioridadEstado(n2.getEstado());

                        if (estado1 != estado2) {
                            return Integer.compare(estado1, estado2);
                        }

                        int compararUsuario =
                                n1.getUidDueno().compareTo(n2.getUidDueno());

                        if (compararUsuario != 0) {
                            return compararUsuario;
                        }

                        return Long.compare(
                                n2.getFechaRegistro(),
                                n1.getFechaRegistro()
                        );
                    });

                    for (var documento : lista) {
                        NegocioServicio negocioServicio =
                                documento.toObject(NegocioServicio.class);

                        crearCardSolicitud(
                                documento.getId(),
                                negocioServicio
                        );
                    }
                });
    }

    private void crearCardSolicitud(String idNegocio, NegocioServicio negocioServicio) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(14, 14, 14, 14);
        card.setBackgroundColor(Color.parseColor("#F5F5F5"));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 18);

        card.setLayoutParams(cardParams);

        ImageView imagen = new ImageView(requireContext());

        LinearLayout.LayoutParams imgParams =
                new LinearLayout.LayoutParams(170, 170);

        imagen.setLayoutParams(imgParams);

        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(requireContext())
                .load(negocioServicio.getImagenUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imagen);

        LinearLayout contenido = new LinearLayout(requireContext());
        contenido.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams contenidoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        contenido.setLayoutParams(contenidoParams);

        contenido.setPadding(14, 0, 0, 0);

        TextView nombre = new TextView(requireContext());

        nombre.setText(negocioServicio.getRazonSocial());
        nombre.setTextSize(16);
        nombre.setTextColor(Color.BLACK);
        nombre.setTypeface(null, Typeface.BOLD);

        nombre.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView gps = new TextView(requireContext());

        gps.setText("Buscando dirección...");

        gps.setTextColor(Color.parseColor("#1565C0"));

        gps.setTextSize(13);

        obtenerDireccion(
                negocioServicio.getLatitud(),
                negocioServicio.getLongitud(),
                gps
        );

        TextView usuario = new TextView(requireContext());
        usuario.setTextColor(Color.parseColor("#EF6C00"));
        usuario.setTextSize(13);

        if (negocioServicio.getTipo() != null
                && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {

            nombre.setText(
                    negocioServicio.getNombreDueno()
                            + " "
                            + negocioServicio.getApellidosDueno()
            );

            usuario.setText(
                    "Categorías: " + obtenerCategoriasServicio(negocioServicio)
            );

        } else {

            usuario.setText(
                    "Creado por: "
                            + negocioServicio.getNombreDueno()
                            + " "
                            + negocioServicio.getApellidosDueno()
            );
        }

        TextView estado = new TextView(requireContext());

        estado.setText(negocioServicio.getEstado());

        estado.setTextColor(Color.WHITE);

        estado.setTextSize(12);

        estado.setPadding(12, 4, 12, 4);

        estado.setGravity(Gravity.CENTER);

        estado.setTypeface(null, Typeface.BOLD);

        if (negocioServicio.getEstado().equals("Aprobado")) {

            estado.setBackgroundColor(
                    Color.parseColor("#43A047")
            );

        } else if (negocioServicio.getEstado().equals("Rechazado")) {

            estado.setBackgroundColor(
                    Color.parseColor("#D32F2F")
            );

        } else {

            estado.setBackgroundColor(
                    Color.parseColor("#757575")
            );
        }

        LinearLayout filaSuperior = new LinearLayout(requireContext());

        filaSuperior.setOrientation(LinearLayout.HORIZONTAL);

        filaSuperior.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams estadoParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        estadoParams.gravity = Gravity.END;

        estado.setLayoutParams(estadoParams);

        filaSuperior.addView(nombre);
        filaSuperior.addView(estado);

        LinearLayout botones = new LinearLayout(requireContext());

        botones.setOrientation(LinearLayout.HORIZONTAL);

        botones.setGravity(Gravity.END);

        botones.setPadding(0, 10, 0, 0);

        TextView btnVer = crearBoton("👁", "#9E9E9E");

        btnVer.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("idNegocioServicio", idNegocio);

            perfilNegocioServico fragment = new perfilNegocioServico();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        TextView btnAceptar = crearBoton("✓", "#2E7D32");
        TextView btnEliminar = crearBoton("✕", "#C62828");

        btnAceptar.setOnClickListener(v -> confirmarAprobar(idNegocio));

        btnEliminar.setOnClickListener(v -> confirmarRechazar(idNegocio));

        botones.addView(btnVer);
        botones.addView(btnAceptar);
        botones.addView(btnEliminar);

        contenido.addView(filaSuperior);
        contenido.addView(gps);
        contenido.addView(usuario);
        contenido.addView(botones);

        card.addView(imagen);
        card.addView(contenido);

        contenedorSolicitudes.addView(card);
    }

    private TextView crearBoton(String texto, String color) {

        TextView boton = new TextView(requireContext());
        boton.setText(texto);
        boton.setTextSize(18);
        boton.setTypeface(null, Typeface.BOLD);
        boton.setGravity(Gravity.CENTER);
        boton.setTextColor(Color.WHITE);
        boton.setBackgroundColor(Color.parseColor(color));
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(70, 70);
        params.setMargins(12, 0, 0, 0);
        boton.setLayoutParams(params);
        return boton;
    }

    private void confirmarAprobar(String idNegocio) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Aprobar registro")
                .setMessage("¿Quieres aprobar este registro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("negocios")
                            .document(idNegocio)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    Toast.makeText(
                                            requireContext(),
                                            "El registro ya no existe",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }
                                NegocioServicio negocioServicioActual =
                                        documentSnapshot.toObject(NegocioServicio.class);
                                if (negocioServicioActual == null) {
                                    Toast.makeText(
                                            requireContext(),
                                            "Error al leer el registro",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }
                                db.collection("negocios")
                                        .whereEqualTo("uidDueno", negocioServicioActual.getUidDueno())
                                        .whereEqualTo("tipo", negocioServicioActual.getTipo())
                                        .get()
                                        .addOnSuccessListener(query -> {
                                            boolean yaTieneAprobado = false;
                                            for (var doc : query) {
                                                if (doc.getId().equals(idNegocio)) {
                                                    continue;
                                                }

                                                NegocioServicio otro =
                                                        doc.toObject(NegocioServicio.class);
                                                if (otro.getEstado() != null &&
                                                        otro.getEstado().equalsIgnoreCase("Aprobado")) {

                                                    yaTieneAprobado = true;
                                                    break;
                                                }
                                            }
                                            if (yaTieneAprobado) {
                                                Toast.makeText(
                                                        requireContext(),
                                                        "Este usuario ya tiene un registro aprobado de este tipo",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                return;
                                            }
                                            db.collection("negocios")
                                                    .document(idNegocio)
                                                    .update("estado", "Aprobado")
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(
                                                                requireContext(),
                                                                "Registro aprobado",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        cargarSolicitudes();
                                                    });
                                        });
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmarRechazar(String idNegocio) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Rechazar negocio")
                .setMessage("¿Quieres rechazar este negocio?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("negocios")
                            .document(idNegocio)
                            .update("estado", "Rechazado")
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(requireContext(), "Negocio rechazado", Toast.LENGTH_SHORT).show();
                                cargarSolicitudes();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void obtenerDireccion(
            double latitud,
            double longitud,
            TextView txtDireccion
    ) {
        try {
            Geocoder geocoder =
                    new Geocoder(
                            requireContext(),
                            new java.util.Locale("es", "PE")
                    );
            List<Address> direcciones =
                    geocoder.getFromLocation(
                            latitud,
                            longitud,
                            1
                    );
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccion = direcciones.get(0);
                String textoDireccion =
                        direccion.getAddressLine(0);
                txtDireccion.setText(textoDireccion);
            } else {
                txtDireccion.setText(
                        latitud + ", " + longitud
                );
            }
        } catch (Exception e) {
            txtDireccion.setText(
                    latitud + ", " + longitud
            );
        }
    }
    private String obtenerCategoriasServicio(NegocioServicio negocioServicio) {
        String categorias = "";

        if (negocioServicio.getEspecialidades() == null) {
            return "";
        }

        for (Object value : negocioServicio.getEspecialidades().values()) {
            Map<String, Object> esp = (Map<String, Object>) value;

            categorias += esp.get("nombre") + ", ";
        }

        if (categorias.endsWith(", ")) {
            categorias = categorias.substring(0, categorias.length() - 2);
        }

        return categorias;
    }
    private int prioridadEstado(String estado) {
        if (estado == null) {
            return 99;
        }

        if (estado.equalsIgnoreCase("Pendiente")) {
            return 1;
        }

        if (estado.equalsIgnoreCase("Aprobado")) {
            return 2;
        }

        if (estado.equalsIgnoreCase("Rechazado")) {
            return 3;
        }

        return 99;
    }
}