package com.example.proy_moviles;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.firebase.firestore.Source;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.location.Address;
import android.location.Geocoder;
import android.content.Intent;
import android.net.Uri;

import java.util.List;

public class listasn extends Fragment {

    Spinner spinnerCategoriaLista;
    Button btnVerMapaLista;
    LinearLayout contenedorListaNegocios;
    FirebaseFirestore db;
    String categoriaInicial = "Todos los servicios";
    ArrayList<String> categorias = new ArrayList<>();
    FusedLocationProviderClient fusedLocationClient;
    double miLatitud = 0;
    double miLongitud = 0;
    boolean tengoUbicacion = false;
    private static final int LOCATION_PERMISSION_LISTA = 300;
    int cargaActual = 0;

    public listasn() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_listasn, container, false);

        spinnerCategoriaLista = vista.findViewById(R.id.spinnerCategoriaLista);
        btnVerMapaLista = vista.findViewById(R.id.btnVerMapaLista);
        contenedorListaNegocios = vista.findViewById(R.id.contenedorListaNegocios);

        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        obtenerMiUbicacion();

        cargarCategorias();

        if (getArguments() != null) {
            categoriaInicial = getArguments().getString(
                    "categoriaSeleccionada",
                    "Todos los servicios"
            );
        }

        btnVerMapaLista.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new mapaglobal())
                    .commit();
        });

        return vista;
    }

    private void cargarCategorias() {
        categorias.clear();
        categorias.add("Todos los servicios");

        db.collection("categorias")
                .orderBy("orden")
                .get()
                .addOnSuccessListener(query -> {
                    query.forEach(documento -> {
                        Categoria categoria = documento.toObject(Categoria.class);
                        if (categoria.isActivo()) {
                            categorias.add(categoria.getNombre());
                        }
                    });

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            categorias
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoriaLista.setAdapter(adapter);

                    int posicion = categorias.indexOf(categoriaInicial);

                    if (posicion >= 0) {
                        spinnerCategoriaLista.setSelection(posicion);
                    }

                    spinnerCategoriaLista.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            cargarNegocios();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                });
    }

    private void cargarNegocios() {

        int numeroCarga = ++cargaActual;

        String categoriaSeleccionada =
                spinnerCategoriaLista.getSelectedItem() == null
                        ? "Todos los servicios"
                        : spinnerCategoriaLista.getSelectedItem().toString();

        contenedorListaNegocios.removeAllViews();

        db.collection("negocios")
                .whereEqualTo("estado", "Aprobado")
                .whereEqualTo("activo", true)
                .get(Source.SERVER)
                .addOnSuccessListener(query -> {

                    if (numeroCarga != cargaActual) {
                        return;
                    }

                    contenedorListaNegocios.removeAllViews();

                    ArrayList<ItemNegocio> lista = new ArrayList<>();

                    query.forEach(documento -> {
                        NegocioServicio negocioServicio =
                                documento.toObject(NegocioServicio.class);

                        if (!categoriaSeleccionada.equals("Todos los servicios")) {

                            boolean coincide = false;

                            if (negocioServicio.getTipo() != null
                                    && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {

                                coincide = servicioTieneCategoria(
                                        negocioServicio,
                                        categoriaSeleccionada
                                );

                            } else {

                                coincide = negocioServicio.getCategoriaNombre() != null
                                        && negocioServicio.getCategoriaNombre()
                                        .trim()
                                        .equalsIgnoreCase(categoriaSeleccionada.trim());
                            }

                            if (!coincide) {
                                return;
                            }
                        }

                        double distancia = calcularDistancia(
                                miLatitud,
                                miLongitud,
                                negocioServicio.getLatitud(),
                                negocioServicio.getLongitud()
                        );

                        lista.add(
                                new ItemNegocio(
                                        documento.getId(),
                                        negocioServicio,
                                        distancia
                                )
                        );
                    });

                    if (tengoUbicacion) {
                        lista.sort((a, b) ->
                                Double.compare(a.distancia, b.distancia)
                        );
                    }

                    if (lista.isEmpty()) {
                        TextView vacio = new TextView(requireContext());
                        vacio.setText("No se encontraron resultados");
                        vacio.setTextSize(18);
                        vacio.setTypeface(null, Typeface.BOLD);
                        vacio.setTextColor(Color.GRAY);
                        vacio.setGravity(Gravity.CENTER);
                        vacio.setPadding(0, 120, 0, 0);
                        contenedorListaNegocios.addView(vacio);
                        return;
                    }

                    for (ItemNegocio item : lista) {
                        crearCardNegocio(item.idDocumento, item.negocioServicio);
                    }
                });
    }

    private void crearCardNegocio(String idDocumento, NegocioServicio negocioServicio) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(10, 10, 10, 10);
        card.setBackgroundColor(Color.parseColor("#F5F5F5"));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 12);

        card.setLayoutParams(cardParams);

        ImageView imagen = new ImageView(requireContext());

        LinearLayout.LayoutParams imgParams =
                new LinearLayout.LayoutParams(210, 210);

        imagen.setLayoutParams(imgParams);

        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(requireContext())
                .load(negocioServicio.getImagenUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imagen);

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        info.setLayoutParams(infoParams);

        info.setPadding(10, 0, 0, 0);

        LinearLayout filaTitulo = new LinearLayout(requireContext());
        filaTitulo.setOrientation(LinearLayout.HORIZONTAL);

        TextView nombre = new TextView(requireContext());
        if (negocioServicio.getTipo() != null
                && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {

            nombre.setText(
                    negocioServicio.getNombreDueno()
                            + " "
                            + negocioServicio.getApellidosDueno()
            );

        } else {
            nombre.setText(negocioServicio.getRazonSocial());
        }
        nombre.setTextColor(Color.BLACK);
        nombre.setTextSize(16);
        nombre.setTypeface(null, Typeface.BOLD);

        nombre.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView estado = new TextView(requireContext());

        boolean abierto = estaAbierto(negocioServicio);

        estado.setText(abierto ? "Abierto" : "Cerrado");

        estado.setTextColor(Color.WHITE);

        estado.setTextSize(11);

        estado.setTypeface(null, Typeface.BOLD);

        estado.setPadding(12, 3, 12, 3);

        estado.setGravity(Gravity.CENTER);

        estado.setBackgroundColor(
                abierto
                        ? Color.parseColor("#43A047")
                        : Color.parseColor("#D50000")
        );

        filaTitulo.addView(nombre);
        filaTitulo.addView(estado);

        TextView coordenadas = new TextView(requireContext());

        coordenadas.setText("Cargando dirección...");

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(
                        requireContext(),
                        new Locale("es", "PE")
                );

                List<Address> direcciones = geocoder.getFromLocation(
                        negocioServicio.getLatitud(),
                        negocioServicio.getLongitud(),
                        1
                );

                if (direcciones != null && !direcciones.isEmpty()) {
                    String direccion = direcciones.get(0).getAddressLine(0);

                    requireActivity().runOnUiThread(() -> {
                        coordenadas.setText(direccion);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        coordenadas.setTextColor(Color.GRAY);

        coordenadas.setTextSize(12);

        TextView categoriasServicio = new TextView(requireContext());
        categoriasServicio.setTextColor(Color.parseColor("#EF6C00"));
        categoriasServicio.setTextSize(12);

        if (negocioServicio.getTipo() != null
                && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {
            categoriasServicio.setText(
                    "Categorías: " + obtenerCategoriasServicio(negocioServicio)
            );
        }

        TextView estrellas = new TextView(requireContext());

        estrellas.setText(
                pintarEstrellas(negocioServicio.getEstrellasPromedio())
                        + " "
                        + negocioServicio.getEstrellasPromedio()
        );

        estrellas.setTextColor(Color.parseColor("#FBC02D"));

        estrellas.setTextSize(12);

        LinearLayout botones = new LinearLayout(requireContext());

        botones.setOrientation(LinearLayout.HORIZONTAL);

        botones.setGravity(Gravity.END);

        botones.setPadding(0, 8, 0, 0);

        Button btnMensaje = new Button(requireContext());
        btnMensaje.setText("Mensaje");
        btnMensaje.setTextSize(14);
        btnMensaje.setTextColor(Color.WHITE);
        btnMensaje.setAllCaps(false);
        btnMensaje.setPadding(2, 0, 2, 0);
        android.graphics.drawable.GradientDrawable fondoMensaje =
                new android.graphics.drawable.GradientDrawable();

        fondoMensaje.setColor(Color.parseColor("#43A047"));
        fondoMensaje.setCornerRadius(25);

        btnMensaje.setBackground(fondoMensaje);

        android.graphics.drawable.Drawable iconoMensaje =
                requireContext().getDrawable(R.drawable.wsp);
        iconoMensaje.setBounds(0, 0, 65, 65);
        btnMensaje.setCompoundDrawables(iconoMensaje, null, null, null);
        btnMensaje.setCompoundDrawablePadding(2);

        Button btnLlamar = new Button(requireContext());
        btnLlamar.setText("Llamar");
        btnLlamar.setTextSize(14);
        btnLlamar.setTextColor(Color.WHITE);
        btnLlamar.setAllCaps(false);
        btnLlamar.setPadding(2, 0, 2, 0);
        android.graphics.drawable.GradientDrawable fondoLlamar =
                new android.graphics.drawable.GradientDrawable();

        fondoLlamar.setColor(Color.parseColor("#1565C0"));
        fondoLlamar.setCornerRadius(25);

        btnLlamar.setBackground(fondoLlamar);

        android.graphics.drawable.Drawable iconoLlamar =
                requireContext().getDrawable(R.drawable.llamada);
        iconoLlamar.setBounds(0, 0, 55, 55);
        btnLlamar.setCompoundDrawables(iconoLlamar, null, null, null);
        btnLlamar.setCompoundDrawablePadding(2);

        Button btnIr = new Button(requireContext());
        btnIr.setText("Ir");
        btnIr.setTextSize(14);
        btnIr.setTextColor(Color.WHITE);
        btnIr.setAllCaps(false);
        btnIr.setPadding(2, 0, 2, 0);
        android.graphics.drawable.GradientDrawable fondoIr =
                new android.graphics.drawable.GradientDrawable();

        fondoIr.setColor(Color.parseColor("#F57C00"));
        fondoIr.setCornerRadius(25);

        btnIr.setBackground(fondoIr);


        btnMensaje.setOnClickListener(v -> {

            String numero = negocioServicio.getWhatsapp();

            numero = numero.replace("+", "")
                    .replace(" ", "");

            String url = "https://wa.me/" + numero;

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            startActivity(intent);
        });

        btnLlamar.setOnClickListener(v -> {

            String telefono = negocioServicio.getTelefono();

            Intent intent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:" + telefono)
            );

            startActivity(intent);
        });

        btnIr.setOnClickListener(v -> {
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

        android.util.TypedValue outValue = new android.util.TypedValue();

        requireContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
        );

        btnMensaje.setForeground(
                requireContext().getDrawable(outValue.resourceId)
        );

        btnLlamar.setForeground(
                requireContext().getDrawable(outValue.resourceId)
        );

        btnIr.setForeground(
                requireContext().getDrawable(outValue.resourceId)
        );

        LinearLayout.LayoutParams paramsMensaje =
                new LinearLayout.LayoutParams(260, 80);
        paramsMensaje.setMargins(8, 0, 0, 0);

        LinearLayout.LayoutParams paramsLlamar =
                new LinearLayout.LayoutParams(240, 80);
        paramsLlamar.setMargins(8, 0, 0, 0);

        LinearLayout.LayoutParams paramsIr =
                new LinearLayout.LayoutParams(110, 80);
        paramsIr.setMargins(8, 0, 0, 0);

        btnMensaje.setLayoutParams(paramsMensaje);
        btnLlamar.setLayoutParams(paramsLlamar);
        btnIr.setLayoutParams(paramsIr);

        botones.addView(btnMensaje);
        botones.addView(btnLlamar);
        botones.addView(btnIr);

        info.addView(filaTitulo);
        info.addView(coordenadas);

        if (negocioServicio.getTipo() != null
                && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {
            info.addView(categoriasServicio);
        }

        info.addView(estrellas);
        info.addView(botones);

        card.addView(imagen);
        card.addView(info);

        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("idNegocioServicio", idDocumento);

            perfilNegocioServico fragment = new perfilNegocioServico();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        contenedorListaNegocios.addView(card);
    }

    private TextView crearBotonConIcono(String texto, int icono) {
        TextView boton = crearBotonTexto(texto);
        boton.setCompoundDrawablesWithIntrinsicBounds(icono, 0, 0, 0);
        boton.setCompoundDrawablePadding(3);
        return boton;
    }

    private TextView crearBotonTexto(String texto) {
        TextView boton = new TextView(requireContext());
        boton.setText(texto);
        boton.setTextSize(11);
        boton.setTextColor(Color.WHITE);
        boton.setGravity(Gravity.CENTER);
        boton.setTypeface(null, Typeface.BOLD);
        boton.setPadding(8, 2, 8, 2);
        boton.setBackgroundColor(Color.parseColor("#1976D2"));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(85, 30);
        params.setMargins(5, 0, 0, 0);
        boton.setLayoutParams(params);

        return boton;
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
    private String pintarEstrellas(double promedio) {
        int estrellasLlenas = (int) Math.round(promedio);

        String resultado = "";

        for (int i = 1; i <= 5; i++) {
            if (i <= estrellasLlenas) {
                resultado += "★";
            } else {
                resultado += "☆";
            }
        }

        return resultado;
    }
    private boolean servicioTieneCategoria(
            NegocioServicio negocioServicio,
            String categoriaSeleccionada
    ) {
        if (negocioServicio.getEspecialidades() == null) {
            return false;
        }

        for (Object value : negocioServicio.getEspecialidades().values()) {
            Map<String, Object> especialidad =
                    (Map<String, Object>) value;

            Object nombre = especialidad.get("nombre");

            if (nombre != null &&
                    nombre.toString()
                            .trim()
                            .equalsIgnoreCase(categoriaSeleccionada.trim())) {
                return true;
            }
        }

        return false;
    }
    private String obtenerCategoriasServicio(NegocioServicio negocioServicio) {
        String categorias = "";

        if (negocioServicio.getEspecialidades() == null) {
            return "";
        }

        for (Object value : negocioServicio.getEspecialidades().values()) {
            Map<String, Object> esp = (Map<String, Object>) value;

            String nombre = String.valueOf(esp.get("nombre"));

            android.util.Log.d(
                    "CATEGORIAS_LISTA",
                    "Especialidad encontrada: " + nombre
            );

            categorias += nombre + ", ";
        }

        if (categorias.endsWith(", ")) {
            categorias = categorias.substring(0, categorias.length() - 2);
        }

        return categorias;
    }

    private void obtenerMiUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_LISTA
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        miLatitud = location.getLatitude();
                        miLongitud = location.getLongitude();
                        tengoUbicacion = true;

                        if (spinnerCategoriaLista.getSelectedItem() != null
                                && contenedorListaNegocios.getChildCount() == 0) {
                            cargarNegocios();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_LISTA
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerMiUbicacion();
        }
    }

    private double calcularDistancia(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        float[] resultado = new float[1];

        Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                resultado
        );

        return resultado[0];
    }

    private static class ItemNegocio {
        String idDocumento;
        NegocioServicio negocioServicio;
        double distancia;

        ItemNegocio(
                String idDocumento,
                NegocioServicio negocioServicio,
                double distancia
        ) {
            this.idDocumento = idDocumento;
            this.negocioServicio = negocioServicio;
            this.distancia = distancia;
        }
    }
}