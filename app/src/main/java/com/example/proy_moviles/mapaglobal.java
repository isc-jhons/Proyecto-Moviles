package com.example.proy_moviles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.widget.AdapterView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;

import java.util.ArrayList;

public class mapaglobal extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    Spinner spinnerCategoriasMapa;
    FirebaseFirestore db;
    ArrayList<String> listaCategorias = new ArrayList<>();
    Map<String, String> coloresFirebase = new HashMap<>();

    boolean trazarRuta = false;
    double latitudDestino = 0;
    double longitudDestino = 0;
    LatLng miUbicacionActual;
    LinearLayout cardNegocioMapa;
    ImageView imgNegocioMapa;
    TextView txtNombreNegocioMapa, txtDireccionNegocioMapa, txtEstrellasNegocioMapa, txtEstadoNegocioMapa, txtCategoriasMapa;
    Button btnMensajeMapa, btnLlamarMapa, btnIrMapa;

    public mapaglobal() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_mapaglobal,
                        container,
                        false
                );

        spinnerCategoriasMapa =
                view.findViewById(R.id.spinnerCategoriasMapa);
        cardNegocioMapa = view.findViewById(R.id.cardNegocioMapa);
        imgNegocioMapa = view.findViewById(R.id.imgNegocioMapa);
        txtNombreNegocioMapa = view.findViewById(R.id.txtNombreNegocioMapa);
        txtDireccionNegocioMapa = view.findViewById(R.id.txtDireccionNegocioMapa);
        txtCategoriasMapa = view.findViewById(R.id.txtCategoriasMapa);
        txtEstrellasNegocioMapa = view.findViewById(R.id.txtEstrellasNegocioMapa);
        txtEstadoNegocioMapa = view.findViewById(R.id.txtEstadoNegocioMapa);
        btnMensajeMapa = view.findViewById(R.id.btnMensajeMapa);
        btnLlamarMapa = view.findViewById(R.id.btnLlamarMapa);
        btnIrMapa = view.findViewById(R.id.btnIrMapa);


        if (getArguments() != null) {
            trazarRuta = getArguments().getBoolean("trazarRuta", false);
            latitudDestino = getArguments().getDouble("latitudDestino", 0);
            longitudDestino = getArguments().getDouble("longitudDestino", 0);
        }

        if (trazarRuta) {
            spinnerCategoriasMapa.setVisibility(View.GONE);
        } else {
            spinnerCategoriasMapa.setVisibility(View.VISIBLE);
        }

        db = FirebaseFirestore.getInstance();
        cargarColoresCategorias();

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(
                        requireActivity()
                );

        cargarCategoriasSpinner();


        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getChildFragmentManager()
                                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                )
        );

        mMap.getUiSettings()
                .setZoomControlsEnabled(true);

        mMap.getUiSettings()
                .setMyLocationButtonEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            cardNegocioMapa.setVisibility(View.GONE);
        });

        activarUbicacion();

        if (!trazarRuta) {
            cargarNegociosAprobados();
        }
    }

    private void activarUbicacion() {


        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );

            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        LatLng miUbicacion =
                                new LatLng(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );
                        miUbicacionActual = miUbicacion;

                        mMap.clear();
                        dibujarRutaSiCorresponde();

                        mMap.moveCamera(
                                CameraUpdateFactory
                                        .newLatLngZoom(
                                                miUbicacion,
                                                16
                                        )
                        );

                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(miUbicacion)
                                        .title("Estoy aquí")
                        );
                    }
                });
    }

    private void cargarCategoriasSpinner() {

        listaCategorias.clear();

        listaCategorias.add("Todos los servicios");

        db.collection("categorias")
                .orderBy("orden")
                .get()
                .addOnSuccessListener(query -> {

                    query.forEach(documento -> {

                        Categoria categoria =
                                documento.toObject(Categoria.class);

                        if (categoria.isActivo()) {
                            listaCategorias.add(
                                    categoria.getNombre()
                            );
                        }
                    });

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    listaCategorias
                            );

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    spinnerCategoriasMapa.setAdapter(adapter);
                    spinnerCategoriasMapa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (mMap != null && !trazarRuta) {
                                cargarNegociosAprobados();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                });
    }
    private void cargarNegociosAprobados() {

        String categoriaSeleccionada =
                spinnerCategoriasMapa.getSelectedItem() == null
                        ? "Todos los servicios"
                        : spinnerCategoriasMapa.getSelectedItem().toString();

        mMap.clear();

        db.collection("negocios")
                .whereEqualTo("estado", "Aprobado")
                .whereEqualTo("activo", true)
                .get()
                .addOnSuccessListener(query -> {

                    for (var documento : query) {
                        NegocioServicio negocioServicio = documento.toObject(NegocioServicio.class);
                        String idDocumento = documento.getId();

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
                                continue;
                            }
                        }

                        LatLng ubicacion = new LatLng(
                                negocioServicio.getLatitud(),
                                negocioServicio.getLongitud()
                        );

                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(ubicacion)
                                        .title(negocioServicio.getRazonSocial())
                                        .icon(crearMarcadorColorNegocioServicio(negocioServicio))
                        ).setTag(new Object[]{idDocumento, negocioServicio});
                    }
                    mMap.setOnMarkerClickListener(marker -> {
                        Object tag = marker.getTag();

                        if (tag instanceof Object[]) {
                            Object[] datos = (Object[]) tag;

                            String idDocumento = (String) datos[0];
                            NegocioServicio negocioServicio = (NegocioServicio) datos[1];

                            mostrarCardNegocio(idDocumento, negocioServicio);
                            return true;
                        }

                        return false;
                    });
                    dibujarRutaSiCorresponde();
                });
    }
    private BitmapDescriptor crearMarcadorColor(String categoriaId) {

        String colorHex = coloresFirebase.get(categoriaId);

        if (colorHex == null) {
            colorHex = "#D32F2F";
        }

        Bitmap bitmap = Bitmap.createBitmap(80, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor(colorHex));

        canvas.drawCircle(40, 35, 25, paint);

        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(40, 95);
        path.lineTo(22, 52);
        path.lineTo(58, 52);
        path.close();
        canvas.drawPath(path, paint);

        Paint centro = new Paint();
        centro.setAntiAlias(true);
        centro.setColor(Color.parseColor("#8B0000"));
        canvas.drawCircle(40, 35, 10, centro);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            activarUbicacion();
        }
    }
    private void cargarColoresCategorias() {
        coloresFirebase.put("ferreterias", "#E53935");
        coloresFirebase.put("gasfiteria", "#F57C00");
        coloresFirebase.put("electricidad", "#FBC02D");
        coloresFirebase.put("mantenimiento", "#90CAF9");
        coloresFirebase.put("instalaciones", "#1E88E5");
        coloresFirebase.put("carpinteria", "#F9A825");
        coloresFirebase.put("tecnicos", "#7E57C2");
        coloresFirebase.put("emergencias", "#E53935");
    }

    private void dibujarRutaSiCorresponde() {
        if (!trazarRuta || miUbicacionActual == null) {
            return;
        }

        LatLng destino = new LatLng(latitudDestino, longitudDestino);

        mMap.addMarker(
                new MarkerOptions()
                        .position(destino)
                        .title("Destino")
        );

        obtenerRutaDirections(miUbicacionActual, destino);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(destino, 16)
        );
    }
    private void obtenerRutaDirections(LatLng origen, LatLng destino) {

        String apiKey = "AIzaSyBYV4Jr-bDegNW2uMSfvhCQQsFmvkowbAM";

        String url =
                "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin=" + origen.latitude + "," + origen.longitude
                        + "&destination=" + destino.latitude + "," + destino.longitude
                        + "&mode=driving"
                        + "&key=" + apiKey;

        new Thread(() -> {
            try {
                URL direccionUrl = new URL(url);
                HttpURLConnection conexion =
                        (HttpURLConnection) direccionUrl.openConnection();

                conexion.connect();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        conexion.getInputStream()
                                )
                        );

                StringBuilder resultado = new StringBuilder();
                String linea;

                while ((linea = reader.readLine()) != null) {
                    resultado.append(linea);
                }

                reader.close();

                JSONObject json = new JSONObject(resultado.toString());

                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() == 0) {
                    return;
                }

                String puntos =
                        routes.getJSONObject(0)
                                .getJSONObject("overview_polyline")
                                .getString("points");

                ArrayList<LatLng> ruta = decodificarPolyline(puntos);

                requireActivity().runOnUiThread(() -> {
                    mMap.addPolyline(
                            new PolylineOptions()
                                    .addAll(ruta)
                                    .width(10)
                                    .color(Color.BLUE)
                    );
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private ArrayList<LatLng> decodificarPolyline(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;
        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0)
                    ? ~(result >> 1)
                    : (result >> 1);
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0)
                    ? ~(result >> 1)
                    : (result >> 1);
            lng += dlng;
            LatLng punto = new LatLng(
                    lat / 100000.0,
                    lng / 100000.0
            );
            poly.add(punto);
        }
        return poly;
    }

    private void mostrarCardNegocio(String idDocumento, NegocioServicio negocioServicio) {

        cardNegocioMapa.setVisibility(View.VISIBLE);

        if (negocioServicio.getTipo() != null
                && negocioServicio.getTipo().equalsIgnoreCase("servicio")) {

            txtNombreNegocioMapa.setText(
                    negocioServicio.getNombreDueno() + " " + negocioServicio.getApellidosDueno()
            );

            txtCategoriasMapa.setVisibility(View.VISIBLE);
            txtCategoriasMapa.setText(
                    "Categorías: " + obtenerCategoriasServicio(negocioServicio)
            );

        } else {
            txtCategoriasMapa.setVisibility(View.GONE);
            txtNombreNegocioMapa.setText(negocioServicio.getRazonSocial());
        }

        boolean abierto = estaAbierto(negocioServicio);

        txtEstadoNegocioMapa.setText(abierto ? "Abierto" : "Cerrado");
        txtEstadoNegocioMapa.setBackgroundColor(
                abierto
                        ? Color.parseColor("#43A047")
                        : Color.parseColor("#D50000")
        );

        txtEstrellasNegocioMapa.setText(
                pintarEstrellas(negocioServicio.getEstrellasPromedio())
                        + " "
                        + negocioServicio.getEstrellasPromedio()
        );

        Glide.with(requireContext())
                .load(negocioServicio.getImagenUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imgNegocioMapa);

        txtDireccionNegocioMapa.setText("Buscando dirección...");

        obtenerDireccion(
                negocioServicio.getLatitud(),
                negocioServicio.getLongitud(),
                txtDireccionNegocioMapa
        );

        btnMensajeMapa.setText("Mensaje");
        btnMensajeMapa.setTextSize(14);
        btnMensajeMapa.setTextColor(Color.WHITE);
        btnMensajeMapa.setAllCaps(false);
        btnMensajeMapa.setPadding(2, 0, 2, 0);

        android.graphics.drawable.GradientDrawable fondoMensaje =
                new android.graphics.drawable.GradientDrawable();
        fondoMensaje.setColor(Color.parseColor("#43A047"));
        fondoMensaje.setCornerRadius(25);
        btnMensajeMapa.setBackgroundTintList(null);
        btnMensajeMapa.setBackground(fondoMensaje);

        android.graphics.drawable.Drawable iconoMensaje =
                requireContext().getDrawable(R.drawable.wsp);
        iconoMensaje.setBounds(0, 0, 65, 65);
        btnMensajeMapa.setCompoundDrawables(iconoMensaje, null, null, null);
        btnMensajeMapa.setCompoundDrawablePadding(2);

        btnLlamarMapa.setText("Llamar");
        btnLlamarMapa.setTextSize(14);
        btnLlamarMapa.setTextColor(Color.WHITE);
        btnLlamarMapa.setAllCaps(false);
        btnLlamarMapa.setPadding(2, 0, 2, 0);

        android.graphics.drawable.GradientDrawable fondoLlamar =
                new android.graphics.drawable.GradientDrawable();
        fondoLlamar.setColor(Color.parseColor("#1565C0"));
        fondoLlamar.setCornerRadius(25);
        btnLlamarMapa.setBackgroundTintList(null);
        btnLlamarMapa.setBackground(fondoLlamar);

        android.graphics.drawable.Drawable iconoLlamar =
                requireContext().getDrawable(R.drawable.llamada);
        iconoLlamar.setBounds(0, 0, 55, 55);
        btnLlamarMapa.setCompoundDrawables(iconoLlamar, null, null, null);
        btnLlamarMapa.setCompoundDrawablePadding(2);

        btnIrMapa.setText("Ir");
        btnIrMapa.setTextSize(14);
        btnIrMapa.setTextColor(Color.WHITE);
        btnIrMapa.setAllCaps(false);
        btnIrMapa.setPadding(2, 0, 2, 0);

        android.graphics.drawable.GradientDrawable fondoIr =
                new android.graphics.drawable.GradientDrawable();
        fondoIr.setColor(Color.parseColor("#F57C00"));
        fondoIr.setCornerRadius(25);
        btnIrMapa.setBackgroundTintList(null);
        btnIrMapa.setBackground(fondoIr);

        btnMensajeMapa.setOnClickListener(v -> {
            String numero = negocioServicio.getWhatsapp()
                    .replace("+", "")
                    .replace(" ", "");

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/" + numero)
            );

            startActivity(intent);
        });

        btnLlamarMapa.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:" + negocioServicio.getTelefono())
            );

            startActivity(intent);
        });

        btnIrMapa.setOnClickListener(v -> {
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

        cardNegocioMapa.setOnClickListener(v -> {
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

    private void obtenerDireccion(double latitud, double longitud, TextView txtDireccion) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), new java.util.Locale("es", "PE"));

            List<Address> direcciones = geocoder.getFromLocation(
                    latitud,
                    longitud,
                    1
            );

            if (direcciones != null && !direcciones.isEmpty()) {
                txtDireccion.setText(direcciones.get(0).getAddressLine(0));
            } else {
                txtDireccion.setText(latitud + ", " + longitud);
            }

        } catch (Exception e) {
            txtDireccion.setText(latitud + ", " + longitud);
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
    private BitmapDescriptor crearMarcadorColorNegocioServicio(NegocioServicio negocioServicio) {

        if (negocioServicio.getTipo() != null
                && negocioServicio.getTipo().equalsIgnoreCase("servicio")
                && negocioServicio.getEspecialidades() != null
                && negocioServicio.getEspecialidades().size() > 1) {

            return crearMarcadorColorPersonalizado("#00BCD4");
        }

        return crearMarcadorColor(negocioServicio.getCategoriaId());
    }
    private BitmapDescriptor crearMarcadorColorPersonalizado(String colorHex) {

        Bitmap bitmap = Bitmap.createBitmap(80, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor(colorHex));

        canvas.drawCircle(40, 35, 25, paint);

        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(40, 95);
        path.lineTo(22, 52);
        path.lineTo(58, 52);
        path.close();
        canvas.drawPath(path, paint);

        Paint centro = new Paint();
        centro.setAntiAlias(true);
        centro.setColor(Color.parseColor("#006064"));
        canvas.drawCircle(40, 35, 10, centro);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
}