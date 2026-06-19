package com.example.proy_moviles;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.graphics.Typeface;
import android.widget.EditText;
import com.google.firebase.firestore.Query;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class perfilNegocioServico extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth auth;
    String idActual = "";

    ImageView imgPerfilBanner, imgPerfilReferencia;
    TextView txtPerfilNombre, txtPerfilCategorias, txtPromedioPerfil,
            txtPerfilPersonas, txtPerfilEstado, txtPerfilDescripcion,
            txtPerfilReferencia, txtPerfilDireccion, txtPerfilTelefono,
            txtPerfilWhatsapp;

    Button btnPerfilIr, btnPerfilLlamar, btnPerfilMensaje;

    LinearLayout layoutHorariosPerfil;

    TextView tabDescripcionPerfil, tabComentariosPerfil, tabProyectosPerfil;
    LinearLayout layoutDescripcionPerfil, layoutComentariosPerfil, layoutProyectosPerfil;

    LinearLayout listaComentariosPerfil, layoutEscribirComentario;
    EditText txtNuevoComentario;
    ImageView imgUsuarioComentario;
    Button btnPublicarComentario;
    RatingBar ratingPerfil;
    ScrollView scrollDescripcionPerfil;
    ScrollView scrollComentariosPerfil;
    LinearLayout listaPublicacionesPerfil;



    double latitud = 0;
    double longitud = 0;

    public perfilNegocioServico() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(
                R.layout.fragment_perfilnegocioservico,
                container,
                false
        );

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        imgPerfilBanner = vista.findViewById(R.id.imgPerfilBanner);
        txtPerfilNombre = vista.findViewById(R.id.txtPerfilNombre);
        txtPerfilCategorias = vista.findViewById(R.id.txtPerfilCategorias);
        ratingPerfil = vista.findViewById(R.id.ratingPerfil);
        txtPromedioPerfil = vista.findViewById(R.id.txtPromedioPerfil);
        txtPerfilPersonas = vista.findViewById(R.id.txtPerfilPersonas);
        txtPerfilEstado = vista.findViewById(R.id.txtPerfilEstado);
        txtPerfilDescripcion = vista.findViewById(R.id.txtPerfilDescripcion);
        txtPerfilReferencia = vista.findViewById(R.id.txtPerfilReferencia);
        txtPerfilDireccion = vista.findViewById(R.id.txtPerfilDireccion);
        txtPerfilTelefono = vista.findViewById(R.id.txtPerfilTelefono);
        txtPerfilWhatsapp = vista.findViewById(R.id.txtPerfilWhatsapp);

        btnPerfilIr = vista.findViewById(R.id.btnPerfilIr);
        btnPerfilLlamar = vista.findViewById(R.id.btnPerfilLlamar);
        btnPerfilMensaje = vista.findViewById(R.id.btnPerfilMensaje);

        layoutHorariosPerfil = vista.findViewById(R.id.layoutHorariosPerfil);

        tabDescripcionPerfil = vista.findViewById(R.id.tabDescripcionPerfil);
        tabComentariosPerfil = vista.findViewById(R.id.tabComentariosPerfil);
        tabProyectosPerfil = vista.findViewById(R.id.tabProyectosPerfil);

        layoutDescripcionPerfil = vista.findViewById(R.id.layoutDescripcionPerfil);
        layoutComentariosPerfil = vista.findViewById(R.id.layoutComentariosPerfil);
        layoutProyectosPerfil = vista.findViewById(R.id.layoutProyectosPerfil);
        imgPerfilReferencia = vista.findViewById(R.id.imgPerfilReferencia);

        listaComentariosPerfil = vista.findViewById(R.id.listaComentariosPerfil);
        layoutEscribirComentario = vista.findViewById(R.id.layoutEscribirComentario);
        txtNuevoComentario = vista.findViewById(R.id.txtNuevoComentario);
        imgUsuarioComentario = vista.findViewById(R.id.imgUsuarioComentario);
        btnPublicarComentario = vista.findViewById(R.id.btnPublicarComentario);
        scrollDescripcionPerfil = vista.findViewById(R.id.scrollDescripcionPerfil);
        scrollComentariosPerfil = vista.findViewById(R.id.scrollComentariosPerfil);

        seleccionarTab("descripcion");

        tabDescripcionPerfil.setOnClickListener(v -> seleccionarTab("descripcion"));
        tabComentariosPerfil.setOnClickListener(v -> seleccionarTab("comentarios"));
        tabProyectosPerfil.setOnClickListener(v -> seleccionarTab("proyectos"));

        listaPublicacionesPerfil =
                vista.findViewById(R.id.listaPublicacionesPerfil);

        String idNegocioServicio = "";

        if (getArguments() != null) {
            idActual = getArguments().getString("idNegocioServicio", "");
        }

        if (!idActual.isEmpty()) {
            cargarDatos(idActual);
        }
        return vista;
    }

    private void cargarDatos(String idNegocioServicio) {
        db.collection("negocios")
                .document(idNegocioServicio)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    NegocioServicio negocioServicio =
                            documentSnapshot.toObject(NegocioServicio.class);

                    if (negocioServicio == null) {
                        return;
                    }

                    latitud = negocioServicio.getLatitud();
                    longitud = negocioServicio.getLongitud();

                    if (negocioServicio.getTipo() != null
                            && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {

                        txtPerfilNombre.setText(
                                negocioServicio.getNombreDueno() + " " +
                                        negocioServicio.getApellidosDueno()
                        );

                        txtPerfilCategorias.setText(
                                obtenerCategoriasServicio(negocioServicio)
                        );

                    } else {
                        txtPerfilNombre.setText(negocioServicio.getRazonSocial());
                        txtPerfilCategorias.setText(negocioServicio.getCategoriaNombre());
                    }

                    txtPerfilDescripcion.setText(
                            "Descripción:\n" + negocioServicio.getDescripcion()
                    );

                    if (negocioServicio.getTipo() != null
                            && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {

                        imgPerfilReferencia.setImageResource(R.drawable.espec_perfil);

                        txtPerfilReferencia.setText(
                                "Especialidades:\n" + obtenerEspecialidadesConTiempo(negocioServicio)
                        );

                    } else {

                        imgPerfilReferencia.setImageResource(R.drawable.refer_perfil);

                        txtPerfilReferencia.setText(
                                "Referencia:\n" + negocioServicio.getReferencia()
                        );
                    }

                    txtPerfilTelefono.setText(
                            "Teléfono / Fijo:\n" + negocioServicio.getTelefono()
                    );

                    txtPerfilWhatsapp.setText(
                            "Número:\n" + negocioServicio.getWhatsapp()
                    );

                    ratingPerfil.setRating(
                            (float) negocioServicio.getEstrellasPromedio()
                    );

                    txtPromedioPerfil.setText(
                            String.valueOf(negocioServicio.getEstrellasPromedio())
                    );

                    ratingPerfil.setIsIndicator(false);
                    ratingPerfil.setEnabled(true);
                    ratingPerfil.setClickable(true);

                    ratingPerfil.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

                        if (!fromUser) {
                            return;
                        }

                        int estrellasMarcadas = Math.round(rating);

                        if (auth.getCurrentUser() == null) {
                            Toast.makeText(
                                    requireContext(),
                                    "Debes iniciar sesión para calificar",
                                    Toast.LENGTH_SHORT
                            ).show();

                            ratingBar.setRating((float) negocioServicio.getEstrellasPromedio());
                            return;
                        }

                        ratingBar.setEnabled(false);

                        Toast.makeText(
                                requireContext(),
                                "Marcaste " + estrellasMarcadas + " estrellas",
                                Toast.LENGTH_SHORT
                        ).show();

                        guardarCalificacion(idNegocioServicio, estrellasMarcadas);
                    });

                    txtPerfilPersonas.setText(
                            negocioServicio.getTotalCalificaciones() + " personas"
                    );

                    boolean abierto = estaAbierto(negocioServicio);

                    txtPerfilEstado.setText(abierto ? "Abierto" : "Cerrado");
                    txtPerfilEstado.setBackgroundColor(
                            abierto
                                    ? Color.parseColor("#43A047")
                                    : Color.parseColor("#D50000")
                    );

                    Glide.with(requireContext())
                            .load(negocioServicio.getImagenUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(imgPerfilBanner);

                    txtPerfilDireccion.setText(
                            "Dirección:\n" +
                                    negocioServicio.getLatitud() + ", " +
                                    negocioServicio.getLongitud()
                    );

                    new Thread(() -> {
                        obtenerDireccion(
                                negocioServicio.getLatitud(),
                                negocioServicio.getLongitud()
                        );
                    }).start();

                    cargarHorarios(negocioServicio);
                    ponerIconosBotones();

                    configurarComentarios(idNegocioServicio);
                    cargarComentarios(idNegocioServicio);
                    cargarPublicaciones(idNegocioServicio);

                    btnPerfilLlamar.setOnClickListener(v -> {
                        Intent intent = new Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:" + negocioServicio.getTelefono())
                        );
                        startActivity(intent);
                    });

                    btnPerfilMensaje.setOnClickListener(v -> {
                        String numero = negocioServicio.getWhatsapp()
                                .replace("+", "")
                                .replace(" ", "");

                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/" + numero)
                        );
                        startActivity(intent);
                    });

                    btnPerfilIr.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("trazarRuta", true);
                        bundle.putDouble("latitudDestino", negocioServicio.getLatitud());
                        bundle.putDouble("longitudDestino", negocioServicio.getLongitud());

                        mapaglobal fragment = new mapaglobal();
                        fragment.setArguments(bundle);

                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.contenedorFragments, fragment)
                                .commit();
                    });
                });
    }

    private void obtenerDireccion(double latitud, double longitud) {
        try {
            Geocoder geocoder = new Geocoder(
                    requireContext(),
                    new Locale("es", "PE")
            );

            List<Address> direcciones =
                    geocoder.getFromLocation(latitud, longitud, 1);

            if (direcciones != null && !direcciones.isEmpty()) {
                String direccion = direcciones.get(0).getAddressLine(0);

                requireActivity().runOnUiThread(() -> {
                    txtPerfilDireccion.setText("Dirección:\n" + direccion);
                });
            }

        } catch (Exception e) {
            requireActivity().runOnUiThread(() -> {
                txtPerfilDireccion.setText(
                        "Dirección:\n" + latitud + ", " + longitud
                );
            });
        }
    }

    private void cargarHorarios(NegocioServicio negocioServicio) {
        layoutHorariosPerfil.removeAllViews();

        if (negocioServicio.getHorarios() == null) {
            return;
        }

        String[] ordenDias = {
                "lunes", "martes", "miércoles", "jueves",
                "viernes", "sábado", "domingo"
        };

        for (String dia : ordenDias) {

            if (!negocioServicio.getHorarios().containsKey(dia)) {
                continue;
            }

            Map<String, Object> horario =
                    (Map<String, Object>) negocioServicio.getHorarios().get(dia);

            TextView fila = new TextView(requireContext());

            fila.setText(
                    dia + "    " +
                            horario.get("inicio") + " - " +
                            horario.get("fin")
            );

            fila.setTextSize(13);
            fila.setTextColor(Color.BLACK);
            fila.setPadding(0, 0, 0, 0);

            layoutHorariosPerfil.addView(fila);
        }
    }

    private boolean estaAbierto(NegocioServicio negocioServicio) {
        if (negocioServicio.getHorarios() == null) {
            return false;
        }

        String diaActual = new SimpleDateFormat("EEEE", new Locale("es", "ES"))
                .format(new Date())
                .toLowerCase();

        Map<String, Object> horarioDia =
                (Map<String, Object>) negocioServicio.getHorarios().get(diaActual);

        if (horarioDia == null) {
            return false;
        }

        String inicio = String.valueOf(horarioDia.get("inicio"));
        String fin = String.valueOf(horarioDia.get("fin"));

        int ahora = minutosActuales();
        int minInicio = convertirMinutos(inicio);
        int minFin = convertirMinutos(fin);

        return ahora >= minInicio && ahora <= minFin;
    }

    private int minutosActuales() {
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        return convertirMinutos(hora);
    }

    private int convertirMinutos(String hora) {
        String[] partes = hora.split(":");
        return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
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
    private void seleccionarTab(String tab) {

        scrollDescripcionPerfil.setVisibility(View.GONE);
        layoutComentariosPerfil.setVisibility(View.GONE);
        layoutProyectosPerfil.setVisibility(View.GONE);

        tabDescripcionPerfil.setBackgroundColor(Color.WHITE);
        tabComentariosPerfil.setBackgroundColor(Color.WHITE);
        tabProyectosPerfil.setBackgroundColor(Color.WHITE);

        if (tab.equals("descripcion")) {
            scrollDescripcionPerfil.setVisibility(View.VISIBLE);
            tabDescripcionPerfil.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else if (tab.equals("comentarios")) {
            layoutComentariosPerfil.setVisibility(View.VISIBLE);
            tabComentariosPerfil.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else {
            layoutProyectosPerfil.setVisibility(View.VISIBLE);
            tabProyectosPerfil.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }
    }
    private void ponerIconosBotones() {
        btnPerfilMensaje.setText("Mensaje");
        btnPerfilMensaje.setTextSize(14);
        btnPerfilMensaje.setTextColor(Color.WHITE);
        btnPerfilMensaje.setAllCaps(false);
        btnPerfilMensaje.setTypeface(null, Typeface.BOLD);
        btnPerfilMensaje.setPadding(2, 0, 2, 0);

        android.graphics.drawable.Drawable iconoMensaje =
                requireContext().getDrawable(R.drawable.wsp);
        iconoMensaje.setBounds(0, 0, 55, 55);
        btnPerfilMensaje.setCompoundDrawables(iconoMensaje, null, null, null);
        btnPerfilMensaje.setCompoundDrawablePadding(2);

        btnPerfilLlamar.setText("Llamar");
        btnPerfilLlamar.setTextSize(14);
        btnPerfilLlamar.setTextColor(Color.WHITE);
        btnPerfilLlamar.setAllCaps(false);
        btnPerfilLlamar.setTypeface(null, Typeface.BOLD);
        btnPerfilLlamar.setPadding(2, 0, 2, 0);

        android.graphics.drawable.Drawable iconoLlamar =
                requireContext().getDrawable(R.drawable.llamada);
        iconoLlamar.setBounds(0, 0, 50, 50);
        btnPerfilLlamar.setCompoundDrawables(iconoLlamar, null, null, null);
        btnPerfilLlamar.setCompoundDrawablePadding(2);
    }
    private String obtenerEspecialidadesConTiempo(NegocioServicio negocioServicio) {
        String texto = "";

        if (negocioServicio.getEspecialidades() == null) {
            return "---";
        }

        for (Object value : negocioServicio.getEspecialidades().values()) {
            Map<String, Object> esp = (Map<String, Object>) value;

            String nombre = String.valueOf(esp.get("nombre"));
            String numero = String.valueOf(esp.get("experienciaNumero"));
            String tipo = String.valueOf(esp.get("experienciaTipo"));

            texto += nombre + " - " + numero + " " + tipo + "\n";
        }

        return texto.trim();
    }



    private void guardarCalificacion(String idNegocioServicio, int estrellas) {

        String uidUsuario = auth.getCurrentUser().getUid();

        DocumentReference negocioRef =
                db.collection("negocios").document(idNegocioServicio);

        DocumentReference calificacionRef =
                negocioRef.collection("calificaciones").document(uidUsuario);

        db.runTransaction(transaction -> {

            com.google.firebase.firestore.DocumentSnapshot negocioSnap =
                    transaction.get(negocioRef);

            NegocioServicio negocioServicio =
                    negocioSnap.toObject(NegocioServicio.class);

            if (negocioServicio == null) {
                return null;
            }

            com.google.firebase.firestore.DocumentSnapshot calificacionSnap =
                    transaction.get(calificacionRef);

            double sumaActual = negocioServicio.getSumaEstrellas();
            int totalActual = negocioServicio.getTotalCalificaciones();

            if (calificacionSnap.exists()) {
                Calificacion calificacionAnterior =
                        calificacionSnap.toObject(Calificacion.class);

                if (calificacionAnterior != null) {
                    sumaActual = sumaActual - calificacionAnterior.getEstrellas();
                }
            } else {
                totalActual = totalActual + 1;
            }

            sumaActual = sumaActual + estrellas;

            double promedio = 0;

            if (totalActual > 0) {
                promedio = sumaActual / totalActual;
            }

            promedio = Math.round(promedio * 10.0) / 10.0;

            Calificacion nuevaCalificacion =
                    new Calificacion(uidUsuario, estrellas);

            transaction.set(calificacionRef, nuevaCalificacion);
            transaction.update(negocioRef, "sumaEstrellas", sumaActual);
            transaction.update(negocioRef, "totalCalificaciones", totalActual);
            transaction.update(negocioRef, "estrellasPromedio", promedio);

            return null;
        }).addOnSuccessListener(unused -> {

            Toast.makeText(
                    requireContext(),
                    "Calificación guardada",
                    Toast.LENGTH_SHORT
            ).show();

            ratingPerfil.setEnabled(true);

            db.collection("negocios")
                    .document(idNegocioServicio)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        NegocioServicio actualizado =
                                documentSnapshot.toObject(NegocioServicio.class);

                        if (actualizado != null) {
                            ratingPerfil.setRating(
                                    (float) actualizado.getEstrellasPromedio()
                            );

                            txtPromedioPerfil.setText(
                                    String.valueOf(actualizado.getEstrellasPromedio())
                            );

                            txtPerfilPersonas.setText(
                                    actualizado.getTotalCalificaciones() + " personas"
                            );
                        }
                    });

        }).addOnFailureListener(e -> {

            Toast.makeText(
                    requireContext(),
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            ratingPerfil.setEnabled(true);
        });
    }
    private void configurarComentarios(String idNegocioServicio) {

        if (auth.getCurrentUser() == null) {

            layoutEscribirComentario.setVisibility(View.GONE);
            imgUsuarioComentario.setImageResource(R.drawable.perfilusuario);
            return;
        }

        layoutEscribirComentario.setVisibility(View.VISIBLE);
        imgUsuarioComentario.setImageResource(R.drawable.perfilusuario);

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuariologin")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    String fotoUrl = documentSnapshot.getString("fotoUrl");

                    if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
                        Glide.with(requireContext())
                                .load(fotoUrl)
                                .placeholder(R.drawable.perfilusuario)
                                .error(R.drawable.perfilusuario)
                                .into(imgUsuarioComentario);
                    } else {
                        imgUsuarioComentario.setImageResource(R.drawable.perfilusuario);
                    }
                });

        btnPublicarComentario.setOnClickListener(v -> {

            String texto = txtNuevoComentario.getText().toString().trim();

            if (texto.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Escribe un comentario",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            publicarComentario(idNegocioServicio, texto);
        });
    }

    private void publicarComentario(String idNegocioServicio, String texto) {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    requireContext(),
                    "Debes iniciar sesión para comentar",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        btnPublicarComentario.setEnabled(false);

        db.collection("usuariologin")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    String nombre = documentSnapshot.getString("nombre");
                    String apellidos = documentSnapshot.getString("apellidos");
                    String fotoUrl = documentSnapshot.getString("fotoUrl");

                    if (nombre == null) {
                        nombre = "";
                    }

                    if (apellidos == null) {
                        apellidos = "";
                    }

                    if (fotoUrl == null) {
                        fotoUrl = "";
                    }

                    String nombreCompleto = (nombre + " " + apellidos).trim();

                    if (nombreCompleto.isEmpty()) {
                        nombreCompleto = "Usuario";
                    }

                    Comentario comentario = new Comentario(
                            uid,
                            nombreCompleto,
                            texto,
                            System.currentTimeMillis(),
                            fotoUrl,
                            0,
                            new java.util.HashMap<>()
                    );

                    db.collection("negocios")
                            .document(idNegocioServicio)
                            .collection("comentarios")
                            .add(comentario)
                            .addOnSuccessListener(documentReference -> {

                                txtNuevoComentario.setText("");
                                btnPublicarComentario.setEnabled(true);

                                Toast.makeText(
                                        requireContext(),
                                        "Comentario publicado",
                                        Toast.LENGTH_SHORT
                                ).show();

                                txtNuevoComentario.setText("");
                                btnPublicarComentario.setEnabled(true);

                                if (listaComentariosPerfil.getChildCount() == 1) {
                                    View primerView = listaComentariosPerfil.getChildAt(0);

                                    if (primerView instanceof TextView) {
                                        listaComentariosPerfil.removeAllViews();
                                    }
                                }

                                crearCardComentario(
                                        idNegocioServicio,
                                        documentReference.getId(),
                                        comentario,
                                        true
                                );

                                scrollComentariosPerfil.post(() -> {
                                    scrollComentariosPerfil.smoothScrollTo(0, 0);
                                });
                            })
                            .addOnFailureListener(e -> {
                                btnPublicarComentario.setEnabled(true);

                                Toast.makeText(
                                        requireContext(),
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnPublicarComentario.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Error al leer usuario",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void cargarComentarios(String idNegocioServicio) {

        listaComentariosPerfil.removeAllViews();

        db.collection("negocios")
                .document(idNegocioServicio)
                .collection("comentarios")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        TextView vacio = new TextView(requireContext());
                        vacio.setText("Aún no hay comentarios");
                        vacio.setTextColor(Color.GRAY);
                        vacio.setTextSize(14);
                        vacio.setGravity(Gravity.CENTER);
                        vacio.setPadding(0, 20, 0, 20);

                        listaComentariosPerfil.addView(vacio);
                        return;
                    }

                    for (var documento : query) {
                        Comentario comentario =
                                documento.toObject(Comentario.class);

                        if (comentario != null) {
                            crearCardComentario(idNegocioServicio, documento.getId(), comentario, false);
                        }
                    }
                });
    }

    private void crearCardComentario(String idNegocioServicio, String idComentario, Comentario comentario, boolean arriba) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(8, 10, 8, 10);
        card.setBackgroundColor(Color.WHITE);

        ImageView imgUsuario = new ImageView(requireContext());
        imgUsuario.setLayoutParams(new LinearLayout.LayoutParams(55, 55));
        imgUsuario.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgUsuario.setImageResource(R.drawable.perfilusuario);

        LinearLayout contenido = new LinearLayout(requireContext());
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(10, 0, 0, 0);
        contenido.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView nombre = new TextView(requireContext());
        nombre.setText(comentario.getNombreUsuario());
        nombre.setTextColor(Color.BLACK);
        nombre.setTypeface(null, Typeface.BOLD);
        nombre.setTextSize(14);

        TextView texto = new TextView(requireContext());
        texto.setText(comentario.getComentario());
        texto.setTextColor(Color.BLACK);
        texto.setTextSize(13);

        TextView fecha = new TextView(requireContext());
        fecha.setText(formatearFechaComentario(comentario.getFecha()));
        fecha.setTextColor(Color.GRAY);
        fecha.setTextSize(11);

        TextView btnLike = new TextView(requireContext());
        btnLike.setText("👍 Me gusta   " + comentario.getLikes());
        btnLike.setTextSize(12);
        btnLike.setTextColor(Color.BLACK);
        btnLike.setPadding(18, 8, 18, 8);

        android.graphics.drawable.GradientDrawable fondoLike =
                new android.graphics.drawable.GradientDrawable();

        fondoLike.setColor(Color.parseColor("#E0E0E0"));
        fondoLike.setStroke(2, Color.parseColor("#9E9E9E"));
        fondoLike.setCornerRadius(4);

        btnLike.setBackground(fondoLike);

        LinearLayout.LayoutParams paramsLike =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        paramsLike.topMargin = 6;

        btnLike.setLayoutParams(paramsLike);



        btnLike.setOnClickListener(v -> darLikeComentario(idNegocioServicio, idComentario, btnLike));

        LinearLayout filaBotones = new LinearLayout(requireContext());
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);
        filaBotones.setGravity(Gravity.CENTER_VERTICAL);

        filaBotones.addView(btnLike);

        if (auth.getCurrentUser() != null
                && auth.getCurrentUser().getUid().equals(comentario.getUidUsuario())) {

            TextView btnEliminar = new TextView(requireContext());

            btnEliminar.setText("🗑 Eliminar ");
            btnEliminar.setTextSize(12);
            btnEliminar.setTextColor(Color.WHITE);
            btnEliminar.setPadding(18, 8, 18, 8);

            android.graphics.drawable.GradientDrawable fondoEliminar =
                    new android.graphics.drawable.GradientDrawable();

            fondoEliminar.setColor(Color.parseColor("#D32F2F"));
            fondoEliminar.setCornerRadius(4);

            btnEliminar.setBackground(fondoEliminar);

            LinearLayout.LayoutParams paramsEliminar =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            paramsEliminar.topMargin = 6;
            paramsEliminar.leftMargin = 14;

            btnEliminar.setLayoutParams(paramsEliminar);

            btnEliminar.setOnClickListener(v -> {

                db.collection("negocios")
                        .document(idNegocioServicio)
                        .collection("comentarios")
                        .document(idComentario)
                        .delete()
                        .addOnSuccessListener(unused -> {

                            Toast.makeText(
                                    requireContext(),
                                    "Comentario eliminado",
                                    Toast.LENGTH_SHORT
                            ).show();

                            db.collection("negocios")
                                    .document(idNegocioServicio)
                                    .collection("comentarios")
                                    .get()
                                    .addOnSuccessListener(query -> {

                                        listaComentariosPerfil.removeAllViews();

                                        if (query.isEmpty()) {
                                            TextView vacio = new TextView(requireContext());
                                            vacio.setText("Aún no hay comentarios");
                                            vacio.setTextColor(Color.GRAY);
                                            vacio.setTextSize(14);
                                            vacio.setGravity(Gravity.CENTER);
                                            vacio.setPadding(0, 20, 0, 20);

                                            listaComentariosPerfil.addView(vacio);
                                        } else {
                                            cargarComentarios(idNegocioServicio);
                                        }
                                    });
                        });
            });

            filaBotones.addView(btnEliminar);
        }

        contenido.addView(nombre);
        contenido.addView(texto);
        contenido.addView(fecha);
        contenido.addView(filaBotones);

        card.addView(imgUsuario);
        card.addView(contenido);

        if (arriba) {
            listaComentariosPerfil.addView(card, 0);
        } else {
            listaComentariosPerfil.addView(card);
        }

        db.collection("usuariologin")
                .document(comentario.getUidUsuario())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String fotoActual = userDoc.getString("fotoUrl");
                    String nombreActual = userDoc.getString("nombre");
                    String apellidosActual = userDoc.getString("apellidos");

                    if (nombreActual != null) {
                        nombre.setText((nombreActual + " " + (apellidosActual == null ? "" : apellidosActual)).trim());
                    }

                    if (fotoActual != null && !fotoActual.trim().isEmpty()) {
                        Glide.with(requireContext())
                                .load(fotoActual)
                                .placeholder(R.drawable.perfilusuario)
                                .error(R.drawable.perfilusuario)
                                .into(imgUsuario);
                    }
                });

        View linea = new View(requireContext());
        linea.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        linea.setBackgroundColor(Color.parseColor("#BDBDBD"));
        listaComentariosPerfil.addView(linea);

        listaComentariosPerfil.setPadding(0, 0, 0, 140);
    }
    private String formatearFechaComentario(long fecha) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(fecha));
    }

    private void darLikeComentario(
            String idNegocioServicio,
            String idComentario,
            TextView btnLike
    ) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    requireContext(),
                    "Debes iniciar sesión para dar like",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        DocumentReference ref = db.collection("negocios")
                .document(idNegocioServicio)
                .collection("comentarios")
                .document(idComentario);

        String textoActual = btnLike.getText().toString();

        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snap =
                    transaction.get(ref);

            Comentario comentario = snap.toObject(Comentario.class);

            btnLike.setEnabled(false);

            if (comentario == null) {
                return 0;
            }

            Map<String, Boolean> usuariosLike = comentario.getUsuariosLike();

            if (usuariosLike == null) {
                usuariosLike = new java.util.HashMap<>();
            }

            int likes = comentario.getLikes();

            if (usuariosLike.containsKey(uid)) {
                usuariosLike.remove(uid);
                likes--;
            } else {
                usuariosLike.put(uid, true);
                likes++;
            }

            if (likes < 0) {
                likes = 0;
            }

            transaction.update(ref, "likes", likes);
            transaction.update(ref, "usuariosLike", usuariosLike);

            return likes;

        }).addOnSuccessListener(nuevosLikes -> {
            btnLike.setText("👍 Me gusta   " + nuevosLikes);
            btnLike.setEnabled(true);


        }).addOnFailureListener(e -> {
            btnLike.setText(textoActual);
            btnLike.setEnabled(true);
        });;

    }
    private void cargarPublicaciones(String idNegocioServicio) {

        listaPublicacionesPerfil.removeAllViews();

        db.collection("negocios")
                .document(idNegocioServicio)
                .collection("publicaciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        TextView vacio = new TextView(requireContext());
                        vacio.setText("Aún no hay publicaciones");
                        vacio.setTextColor(Color.GRAY);
                        vacio.setGravity(Gravity.CENTER);
                        vacio.setPadding(0, 20, 0, 20);
                        listaPublicacionesPerfil.addView(vacio);
                        return;
                    }

                    for (var doc : query.getDocuments()) {
                        Publicacion p = doc.toObject(Publicacion.class);

                        if (p == null) continue;

                        crearPublicacionPerfil(
                                p.getTitulo(),
                                p.getDescripcion(),
                                p.getImagenUrl()
                        );
                    }
                });
    }

    private void crearPublicacionPerfil(
            String titulo,
            String descripcion,
            String imagenUrl
    ) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, 0);

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

        Glide.with(requireContext())
                .load(imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
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

        listaPublicacionesPerfil.addView(card);
    }
}