package com.example.proy_moviles;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.text.InputFilter;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class registrarsn extends Fragment {

    TextView tabNegocio, tabServicio, btnAgregarHorario;
    Spinner spinnerDia, spinnerHoraInicio, spinnerHoraFin;
    LinearLayout layoutNegocio, layoutServicio;

    String tipoSeleccionado = "negocio";

    ArrayList<String[]> horariosAgregados = new ArrayList<>();
    LinearLayout listaHorarios;

    EditText txtImagenUrl;
    ImageView imgBannerPreview;

    ArrayList<String> horasBase = new ArrayList<>();

    EditText txtRuc, txtRazonSocial, txtDescripcion, txtWhatsapp, txtTelefono, txtReferencia;
    Button btnRegistrarNegocio;

    FirebaseAuth auth;
    FirebaseFirestore db;

    EditText txtDniServicio, txtWhatsappServicio, txtTelefonoServicio,
            txtDireccionServicio, txtDescripcionServicio, txtImagenServicio,
            txtTiempoExperiencia;

    Spinner spinnerEspecialidad, spinnerTipoExperiencia,
            spinnerDiaServicio, spinnerHoraInicioServicio, spinnerHoraFinServicio;

    Button btnRegistrarServicioFinal;

    ArrayList<String[]> especialidadesAgregadas = new ArrayList<>();
    ArrayList<String[]> horariosServicioAgregados = new ArrayList<>();

    TextView btnAgregarHorarioServicio, btnAgregarEspecialidad;
    LinearLayout listaHorariosServicio, listaEspecialidades;
    ImageView imgServicioPreview;

    double latitudServicio = 0;
    double longitudServicio = 0;

    EditText txtCoordenadaGps;
    double latitudNegocio = 0;
    double longitudNegocio = 0;

    boolean imagenNegocioValida = false;
    String imagenNegocioValidadaUrl = "";

    boolean imagenServicioValida = false;
    String imagenServicioValidadaUrl = "";

    public registrarsn() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_registrarsn, container, false);

        tabNegocio = vista.findViewById(R.id.tabNegocio);
        tabServicio = vista.findViewById(R.id.tabServicio);
        btnAgregarHorario = vista.findViewById(R.id.btnAgregarHorario);

        spinnerDia = vista.findViewById(R.id.spinnerDia);
        spinnerHoraInicio = vista.findViewById(R.id.spinnerHoraInicio);
        spinnerHoraFin = vista.findViewById(R.id.spinnerHoraFin);

        layoutNegocio = vista.findViewById(R.id.layoutNegocio);
        layoutServicio = vista.findViewById(R.id.layoutServicio);

        listaHorarios = vista.findViewById(R.id.listaHorarios);

        cargarCombosHorario();
        seleccionarTipo("negocio");

        tabNegocio.setOnClickListener(v -> seleccionarTipo("negocio"));
        tabServicio.setOnClickListener(v -> seleccionarTipo("servicio"));

        btnAgregarHorario.setOnClickListener(v -> validarHorario());

        txtImagenUrl = vista.findViewById(R.id.txtImagenUrl);
        imgBannerPreview = vista.findViewById(R.id.imgBannerPreview);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtRuc = vista.findViewById(R.id.txtRuc);
        txtRazonSocial = vista.findViewById(R.id.txtRazonSocial);
        txtDescripcion = vista.findViewById(R.id.txtDescripcion);
        txtWhatsapp = vista.findViewById(R.id.txtWhatsapp);
        txtTelefono = vista.findViewById(R.id.txtTelefono);
        txtReferencia = vista.findViewById(R.id.txtReferencia);
        btnRegistrarNegocio = vista.findViewById(R.id.btnRegistrarNegocio);


        btnRegistrarNegocio.setOnClickListener(v -> registrarNegocio());

        txtImagenUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();

                imagenNegocioValida = false;
                imagenNegocioValidadaUrl = "";

                if (url.isEmpty()) {
                    imgBannerPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                } else if (url.startsWith("http")) {
                    Glide.with(requireContext())
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgBannerPreview);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        txtCoordenadaGps = vista.findViewById(R.id.txtCoordenadaGps);

        txtCoordenadaGps.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new seleccionarmapa())
                    .addToBackStack(null)
                    .commit();
        });

        getParentFragmentManager().setFragmentResultListener(
                "ubicacionNegocio",
                this,
                (requestKey, bundle) -> {

                    double lat = bundle.getDouble("latitud");
                    double lng = bundle.getDouble("longitud");

                    String origen =
                            bundle.getString("origen", "negocio");

                    if (origen.equals("servicio")) {

                        latitudServicio = lat;
                        longitudServicio = lng;

                        txtDireccionServicio.setText(lat + ", " + lng);

                        seleccionarTipo("servicio");

                        repintarHorariosServicio();
                        repintarEspecialidadesServicio();

                    } else {

                        latitudNegocio = lat;
                        longitudNegocio = lng;

                        txtCoordenadaGps.setText(lat + ", " + lng);

                        seleccionarTipo("negocio");

                        repintarHorariosNegocio();
                    }
                }
        );

        txtDniServicio = vista.findViewById(R.id.txtDniServicio);
        txtWhatsappServicio = vista.findViewById(R.id.txtWhatsappServicio);
        txtTelefonoServicio = vista.findViewById(R.id.txtTelefonoServicio);
        txtDireccionServicio = vista.findViewById(R.id.txtDireccionServicio);
        txtDescripcionServicio = vista.findViewById(R.id.txtDescripcionServicio);
        txtImagenServicio = vista.findViewById(R.id.txtImagenServicio);
        txtTiempoExperiencia = vista.findViewById(R.id.txtTiempoExperiencia);

        spinnerEspecialidad = vista.findViewById(R.id.spinnerEspecialidad);
        spinnerTipoExperiencia = vista.findViewById(R.id.spinnerTipoExperiencia);
        spinnerDiaServicio = vista.findViewById(R.id.spinnerDiaServicio);
        spinnerHoraInicioServicio = vista.findViewById(R.id.spinnerHoraInicioServicio);
        spinnerHoraFinServicio = vista.findViewById(R.id.spinnerHoraFinServicio);

        btnRegistrarServicioFinal = vista.findViewById(R.id.btnRegistrarServicioFinal);
        btnAgregarHorarioServicio = vista.findViewById(R.id.btnAgregarHorarioServicio);
        btnAgregarEspecialidad = vista.findViewById(R.id.btnAgregarEspecialidad);

        listaHorariosServicio = vista.findViewById(R.id.listaHorariosServicio);
        listaEspecialidades = vista.findViewById(R.id.listaEspecialidades);
        imgServicioPreview = vista.findViewById(R.id.imgServicioPreview);

        txtRuc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        txtDniServicio.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        txtWhatsapp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        txtWhatsappServicio.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        txtTelefono.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        txtTelefonoServicio.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});

        cargarCombosServicio();

        btnRegistrarServicioFinal.setOnClickListener(v -> registrarServicio());
        btnAgregarEspecialidad.setOnClickListener(v -> validarEspecialidadServicio());
        btnAgregarHorarioServicio.setOnClickListener(v -> validarHorarioServicio());

        txtDireccionServicio.setOnClickListener(v -> {
            tipoSeleccionado = "servicio";

            seleccionarmapa fragment = new seleccionarmapa();

            Bundle bundle = new Bundle();
            bundle.putString("origen", "servicio");
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        txtImagenServicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();

                imagenServicioValida = false;
                imagenServicioValidadaUrl = "";

                if (url.isEmpty()) {
                    imgServicioPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                } else if (url.startsWith("http")) {
                    Glide.with(requireContext())
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgServicioPreview);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return vista;
    }

    private void cargarCombosHorario() {
        String[] dias = {
                "Día", "lunes", "martes", "miércoles", "jueves",
                "viernes", "sábado", "domingo"
        };

        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dias
        );
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapterDias);

        horasBase.clear();
        horasBase.add("Hora");

        for (int h = 0; h <= 23; h++) {
            horasBase.add(String.format("%02d:00", h));
            horasBase.add(String.format("%02d:30", h));
        }

        cargarHorasInicio();
        cargarHorasFinDesde(0);

        spinnerHoraInicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarHorasFinDesde(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarHorasInicio() {
        ArrayAdapter<String> adapterInicio = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                horasBase
        );
        adapterInicio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHoraInicio.setAdapter(adapterInicio);
    }

    private void cargarHorasFinDesde(int posicionInicio) {
        ArrayList<String> horasFin = new ArrayList<>();
        horasFin.add("Hora");

        if (posicionInicio > 0) {
            for (int i = posicionInicio; i < horasBase.size(); i++) {
                horasFin.add(horasBase.get(i));
            }
        }

        ArrayAdapter<String> adapterFin = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                horasFin
        );
        adapterFin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHoraFin.setAdapter(adapterFin);
    }

    private void validarHorario() {
        String dia = spinnerDia.getSelectedItem().toString();
        String inicio = spinnerHoraInicio.getSelectedItem().toString();
        String fin = spinnerHoraFin.getSelectedItem().toString();

        if (dia.equals("Día")) {
            Toast.makeText(requireContext(), "Seleccione un día", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inicio.equals("Hora")) {
            Toast.makeText(requireContext(), "Seleccione hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fin.equals("Hora")) {
            Toast.makeText(requireContext(), "Seleccione hora de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        int nuevoInicio = convertirMinutos(inicio);
        int nuevoFin = convertirMinutos(fin);

        if (nuevoFin <= nuevoInicio) {
            Toast.makeText(requireContext(), "La hora fin debe ser mayor", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String[] h : horariosAgregados) {
            if (h[0].equals(dia)) {
                int inicioGuardado = convertirMinutos(h[1]);
                int finGuardado = convertirMinutos(h[2]);

                boolean seCruza = nuevoInicio < finGuardado && nuevoFin > inicioGuardado;

                if (seCruza) {
                    Toast.makeText(requireContext(), "Ese horario se cruza con otro", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        agregarFilaHorario(dia, inicio, fin);

        spinnerDia.setSelection(0);
        spinnerHoraInicio.setSelection(0);
        spinnerHoraFin.setSelection(0);
    }

    private void seleccionarTipo(String tipo) {
        tipoSeleccionado = tipo;

        if (tipo.equals("negocio")) {
            layoutNegocio.setVisibility(View.VISIBLE);
            layoutServicio.setVisibility(View.GONE);

            tabNegocio.setBackgroundColor(Color.parseColor("#1976D2"));
            tabNegocio.setTextColor(Color.WHITE);
            tabServicio.setBackgroundColor(Color.WHITE);
            tabServicio.setTextColor(Color.BLACK);
        } else {
            layoutNegocio.setVisibility(View.GONE);
            layoutServicio.setVisibility(View.VISIBLE);

            tabServicio.setBackgroundColor(Color.parseColor("#1976D2"));
            tabServicio.setTextColor(Color.WHITE);
            tabNegocio.setBackgroundColor(Color.WHITE);
            tabNegocio.setTextColor(Color.BLACK);
        }
    }

    private int convertirMinutos(String hora) {
        String[] partes = hora.split(":");
        int h = Integer.parseInt(partes[0]);
        int m = Integer.parseInt(partes[1]);
        return h * 60 + m;
    }

    private void agregarFilaHorario(String dia, String inicio, String fin) {
        String[] horario = {dia, inicio, fin};
        horariosAgregados.add(horario);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtDia = new TextView(requireContext());
        txtDia.setText(dia);
        txtDia.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtInicio = new TextView(requireContext());
        txtInicio.setText(inicio);
        txtInicio.setGravity(android.view.Gravity.CENTER);
        txtInicio.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtFin = new TextView(requireContext());
        txtFin.setText(fin);
        txtFin.setGravity(android.view.Gravity.CENTER);
        txtFin.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(android.view.Gravity.CENTER);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(45, 40));

        btnEliminar.setOnClickListener(v -> {
            horariosAgregados.remove(horario);
            listaHorarios.removeView(fila);
        });

        fila.addView(txtDia);
        fila.addView(txtInicio);
        fila.addView(txtFin);
        fila.addView(btnEliminar);

        listaHorarios.addView(fila);
    }
    private void registrarNegocio() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidUsuario = auth.getCurrentUser().getUid();

        String ruc = txtRuc.getText().toString().trim();
        String razonSocial = txtRazonSocial.getText().toString().trim();
        String descripcion = txtDescripcion.getText().toString().trim();
        String whatsapp = txtWhatsapp.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String referencia = txtReferencia.getText().toString().trim();
        String imagenUrl = txtImagenUrl.getText().toString().trim();
        String coordenada = txtCoordenadaGps.getText().toString().trim();

        if (ruc.isEmpty() || razonSocial.isEmpty() || descripcion.isEmpty()
                || whatsapp.isEmpty() || telefono.isEmpty() || referencia.isEmpty()
                || imagenUrl.isEmpty() || coordenada.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horariosAgregados.isEmpty()) {
            Toast.makeText(requireContext(), "Agregue al menos un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ruc.matches("\\d{11}")) {
            Toast.makeText(requireContext(), "El RUC debe tener 11 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!telefono.matches("\\d+")) {
            Toast.makeText(requireContext(), "El teléfono solo debe contener números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(telefono.length() == 7 || telefono.length() == 9)) {
            Toast.makeText(requireContext(), "El teléfono debe tener 7 o 9 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!whatsapp.matches("\\d{9}")) {
            Toast.makeText(requireContext(), "El WhatsApp debe tener 9 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenUrl.startsWith("http")) {
            Toast.makeText(requireContext(), "El enlace ingresado no proyecta una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenNegocioValida || !imagenUrl.equals(imagenNegocioValidadaUrl)) {
            validarImagenNegocioYRegistrar(imagenUrl);
            return;
        }

        db.collection("negocios")
                .whereEqualTo("ruc", ruc)
                .get()
                .addOnSuccessListener(queryRuc -> {

                    for (var documento : queryRuc) {
                        NegocioServicio existente = documento.toObject(NegocioServicio.class);

                        if (existente.getEstado() != null &&
                                (existente.getEstado().equalsIgnoreCase("Pendiente")
                                        || existente.getEstado().equalsIgnoreCase("Aprobado"))) {

                            Toast.makeText(requireContext(), "El RUC ya está registrado", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    db.collection("negocios")
                            .whereEqualTo("uidDueno", uidUsuario)
                            .whereEqualTo("tipo", "negocio")
                            .get()
                            .addOnSuccessListener(query -> {

                                for (var documento : query) {
                                    NegocioServicio existente =
                                            documento.toObject(NegocioServicio.class);

                                    if (existente.getEstado() != null &&
                                            (existente.getEstado().equalsIgnoreCase("Pendiente")
                                                    || existente.getEstado().equalsIgnoreCase("Aprobado"))) {

                                        Toast.makeText(
                                                requireContext(),
                                                "Ya tienes un negocio pendiente o aprobado, por ende no puedes registrar uno nuevo",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        return;
                                    }
                                }

                                Map<String, Object> horarios = new HashMap<>();

                                for (String[] h : horariosAgregados) {
                                    Map<String, Object> horarioDia = new HashMap<>();
                                    horarioDia.put("abierto", true);
                                    horarioDia.put("inicio", h[1]);
                                    horarioDia.put("fin", h[2]);

                                    horarios.put(h[0], horarioDia);
                                }

                                db.collection("usuariologin")
                                        .document(uidUsuario)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {

                                            UsuarioLogin usuarioLogin =
                                                    userDoc.toObject(UsuarioLogin.class);

                                            String nombreDueno = "";
                                            String apellidosDueno = "";

                                            if (usuarioLogin != null) {
                                                nombreDueno = usuarioLogin.getNombre();
                                                apellidosDueno = usuarioLogin.getApellidos();
                                            }

                                            NegocioServicio negocioServicio = new NegocioServicio(
                                                    uidUsuario,
                                                    "negocio",
                                                    "ferreterias",
                                                    "Ferreteria",
                                                    ruc,
                                                    razonSocial,
                                                    descripcion,
                                                    whatsapp,
                                                    telefono,
                                                    referencia,
                                                    latitudNegocio,
                                                    longitudNegocio,
                                                    imagenUrl,
                                                    horarios,
                                                    true,
                                                    System.currentTimeMillis(),
                                                    "Pendiente",
                                                    0,
                                                    0,
                                                    0,
                                                    "",
                                                    null,
                                                    nombreDueno,
                                                    apellidosDueno
                                            );

                                            db.collection("negocios")
                                                    .add(negocioServicio)
                                                    .addOnSuccessListener(documentReference -> {
                                                        Toast.makeText(
                                                                requireContext(),
                                                                "Negocio registrado. Esperando aprobación.",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        requireActivity()
                                                                .getSupportFragmentManager()
                                                                .beginTransaction()
                                                                .replace(R.id.contenedorFragments, new mapaglobal())
                                                                .commit();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(
                                                                requireContext(),
                                                                "Error: " + e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                    });
                                        });
                            });
                });
    }
    private void registrarServicio() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidUsuario = auth.getCurrentUser().getUid();

        String dni = txtDniServicio.getText().toString().trim();
        String whatsapp = txtWhatsappServicio.getText().toString().trim();
        String telefono = txtTelefonoServicio.getText().toString().trim();
        String coordenada = txtDireccionServicio.getText().toString().trim();
        String descripcion = txtDescripcionServicio.getText().toString().trim();
        String imagenUrl = txtImagenServicio.getText().toString().trim();

        if (dni.isEmpty() || whatsapp.isEmpty() || telefono.isEmpty()
                || coordenada.isEmpty() || descripcion.isEmpty()
                || imagenUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (especialidadesAgregadas.isEmpty()) {
            Toast.makeText(requireContext(), "Agregue al menos una especialidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horariosServicioAgregados.isEmpty()) {
            Toast.makeText(requireContext(), "Agregue al menos un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dni.matches("\\d{8}")) {
            Toast.makeText(requireContext(), "El DNI / CE debe tener 8 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!telefono.matches("\\d+")) {
            Toast.makeText(requireContext(), "El teléfono solo debe contener números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(telefono.length() == 7 || telefono.length() == 9)) {
            Toast.makeText(requireContext(), "El teléfono debe tener 7 o 9 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!whatsapp.matches("\\d{9}")) {
            Toast.makeText(requireContext(), "El WhatsApp debe tener 9 números", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenUrl.startsWith("http")) {
            Toast.makeText(requireContext(), "El enlace ingresado no proyecta una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenServicioValida || !imagenUrl.equals(imagenServicioValidadaUrl)) {
            validarImagenServicioYRegistrar(imagenUrl);
            return;
        }

        db.collection("negocios")
                .whereEqualTo("dni", dni)
                .get()
                .addOnSuccessListener(queryDni -> {

                    for (var documento : queryDni) {
                        NegocioServicio existente = documento.toObject(NegocioServicio.class);

                        if (existente.getEstado() != null &&
                                (existente.getEstado().equalsIgnoreCase("Pendiente")
                                        || existente.getEstado().equalsIgnoreCase("Aprobado"))) {

                            Toast.makeText(requireContext(), "El DNI / CE ya está registrado", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    db.collection("negocios")
                            .whereEqualTo("uidDueno", uidUsuario)
                            .whereEqualTo("tipo", "servicio")
                            .get()
                            .addOnSuccessListener(query -> {

                                for (var documento : query) {

                                    NegocioServicio existente =
                                            documento.toObject(NegocioServicio.class);

                                    if (existente.getEstado() != null &&
                                            (existente.getEstado().equalsIgnoreCase("Pendiente")
                                                    || existente.getEstado().equalsIgnoreCase("Aprobado"))) {

                                        Toast.makeText(
                                                requireContext(),
                                                "Ya tienes un servicio pendiente o aprobado, por ende no puedes registrar uno nuevo",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }
                                }

                                Map<String, Object> horarios = new HashMap<>();

                                for (String[] h : horariosServicioAgregados) {

                                    Map<String, Object> horarioDia = new HashMap<>();

                                    horarioDia.put("abierto", true);
                                    horarioDia.put("inicio", h[1]);
                                    horarioDia.put("fin", h[2]);

                                    horarios.put(h[0], horarioDia);
                                }

                                Map<String, Object> especialidades = new HashMap<>();

                                for (String[] e : especialidadesAgregadas) {

                                    Map<String, Object> especialidad = new HashMap<>();

                                    especialidad.put("nombre", e[1]);
                                    especialidad.put("experienciaNumero", Integer.parseInt(e[2]));
                                    especialidad.put("experienciaTipo", e[3]);

                                    especialidades.put(e[0], especialidad);
                                }

                                String primeraCategoriaId = especialidadesAgregadas.get(0)[0];
                                String primeraCategoriaNombre = especialidadesAgregadas.get(0)[1];

                                db.collection("usuariologin")
                                        .document(uidUsuario)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {

                                            UsuarioLogin usuarioLogin =
                                                    userDoc.toObject(UsuarioLogin.class);

                                            String nombreDueno = "";
                                            String apellidosDueno = "";

                                            if (usuarioLogin != null) {
                                                nombreDueno = usuarioLogin.getNombre();
                                                apellidosDueno = usuarioLogin.getApellidos();
                                            }

                                            NegocioServicio servicio = new NegocioServicio(
                                                    uidUsuario,
                                                    "servicio",
                                                    primeraCategoriaId,
                                                    primeraCategoriaNombre,
                                                    "",
                                                    "",
                                                    descripcion,
                                                    whatsapp,
                                                    telefono,
                                                    "",
                                                    latitudServicio,
                                                    longitudServicio,
                                                    imagenUrl,
                                                    horarios,
                                                    true,
                                                    System.currentTimeMillis(),
                                                    "Pendiente",
                                                    0,
                                                    0,
                                                    0,
                                                    dni,
                                                    especialidades,
                                                    nombreDueno,
                                                    apellidosDueno
                                            );

                                            db.collection("negocios")
                                                    .add(servicio)
                                                    .addOnSuccessListener(documentReference -> {

                                                        Toast.makeText(
                                                                requireContext(),
                                                                "Servicio registrado. Esperando aprobación.",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        requireActivity()
                                                                .getSupportFragmentManager()
                                                                .beginTransaction()
                                                                .replace(
                                                                        R.id.contenedorFragments,
                                                                        new mapaglobal()
                                                                )
                                                                .commit();
                                                    })
                                                    .addOnFailureListener(e -> {

                                                        Toast.makeText(
                                                                requireContext(),
                                                                "Error: " + e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                    });
                                        });
                            });
                });
    }

    private void cargarCombosServicio() {

        String[] tiposExperiencia = {"Tipo", "meses", "años"};

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                tiposExperiencia
        );
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoExperiencia.setAdapter(adapterTipo);

        String[] dias = {
                "Día", "lunes", "martes", "miércoles", "jueves",
                "viernes", "sábado", "domingo"
        };

        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dias
        );
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiaServicio.setAdapter(adapterDias);

        ArrayAdapter<String> adapterInicio = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                horasBase
        );
        adapterInicio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHoraInicioServicio.setAdapter(adapterInicio);

        cargarHorasFinServicioDesde(0);

        spinnerHoraInicioServicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarHorasFinServicioDesde(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayList<String> especialidades = new ArrayList<>();
        especialidades.add("Especialidad");

        db.collection("categorias")
                .orderBy("orden")
                .get()
                .addOnSuccessListener(query -> {
                    query.forEach(documento -> {
                        Categoria categoria = documento.toObject(Categoria.class);

                        if (categoria.isActivo()
                                && categoria.getNombre() != null
                                && !categoria.getNombre().equalsIgnoreCase("Ferreteria")
                                && !documento.getId().equalsIgnoreCase("ferreterias")) {

                            especialidades.add(categoria.getNombre());
                        }
                    });

                    ArrayAdapter<String> adapterEspecialidad = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            especialidades
                    );

                    adapterEspecialidad.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    spinnerEspecialidad.setAdapter(adapterEspecialidad);
                });
    }

    private void cargarHorasFinServicioDesde(int posicionInicio) {
        ArrayList<String> horasFin = new ArrayList<>();
        horasFin.add("Hora");

        if (posicionInicio > 0) {
            for (int i = posicionInicio; i < horasBase.size(); i++) {
                horasFin.add(horasBase.get(i));
            }
        }

        ArrayAdapter<String> adapterFin = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                horasFin
        );
        adapterFin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHoraFinServicio.setAdapter(adapterFin);
    }

    private void validarEspecialidadServicio() {
        String especialidad = spinnerEspecialidad.getSelectedItem().toString();
        String experiencia = txtTiempoExperiencia.getText().toString().trim();
        String tipoExperiencia = spinnerTipoExperiencia.getSelectedItem().toString();

        if (especialidad.equals("Especialidad")) {
            Toast.makeText(requireContext(), "Seleccione una especialidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (experiencia.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese experiencia", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipoExperiencia.equals("Tipo")) {
            Toast.makeText(requireContext(), "Seleccione meses o años", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoriaId = especialidad.toLowerCase()
                .replace(" ", "")
                .replace("í", "i")
                .replace("é", "e")
                .replace("á", "a")
                .replace("ó", "o")
                .replace("ú", "u");

        for (String[] e : especialidadesAgregadas) {
            if (e[0].equals(categoriaId)) {
                Toast.makeText(requireContext(), "Esa especialidad ya fue agregada", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        agregarFilaEspecialidad(categoriaId, especialidad, experiencia, tipoExperiencia);

        spinnerEspecialidad.setSelection(0);
        txtTiempoExperiencia.setText("");
        spinnerTipoExperiencia.setSelection(0);
    }

    private void agregarFilaEspecialidad(String id, String nombre, String experiencia, String tipo) {

        String[] especialidad = {id, nombre, experiencia, tipo};
        especialidadesAgregadas.add(especialidad);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtNombre = new TextView(requireContext());
        txtNombre.setText(nombre);
        txtNombre.setGravity(Gravity.CENTER);
        txtNombre.setLayoutParams(
                new LinearLayout.LayoutParams(0, 40, 1)
        );

        TextView txtNumero = new TextView(requireContext());
        txtNumero.setText(experiencia);
        txtNumero.setGravity(Gravity.CENTER);
        txtNumero.setLayoutParams(
                new LinearLayout.LayoutParams(0, 40, 1)
        );

        TextView txtTipo = new TextView(requireContext());
        txtTipo.setText(tipo);
        txtTipo.setGravity(Gravity.CENTER);
        txtTipo.setLayoutParams(
                new LinearLayout.LayoutParams(0, 40, 1)
        );

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setLayoutParams(
                new LinearLayout.LayoutParams(45, 40)
        );

        btnEliminar.setOnClickListener(v -> {
            especialidadesAgregadas.remove(especialidad);
            listaEspecialidades.removeView(fila);
        });

        fila.addView(txtNombre);
        fila.addView(txtNumero);
        fila.addView(txtTipo);
        fila.addView(btnEliminar);

        listaEspecialidades.addView(fila);
    }

    private void validarHorarioServicio() {
        String dia = spinnerDiaServicio.getSelectedItem().toString();
        String inicio = spinnerHoraInicioServicio.getSelectedItem().toString();
        String fin = spinnerHoraFinServicio.getSelectedItem().toString();

        if (dia.equals("Día")) {
            Toast.makeText(requireContext(), "Seleccione un día", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inicio.equals("Hora")) {
            Toast.makeText(requireContext(), "Seleccione hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fin.equals("Hora")) {
            Toast.makeText(requireContext(), "Seleccione hora de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        int nuevoInicio = convertirMinutos(inicio);
        int nuevoFin = convertirMinutos(fin);

        if (nuevoFin <= nuevoInicio) {
            Toast.makeText(requireContext(), "La hora fin debe ser mayor", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String[] h : horariosServicioAgregados) {
            if (h[0].equals(dia)) {
                int inicioGuardado = convertirMinutos(h[1]);
                int finGuardado = convertirMinutos(h[2]);

                boolean seCruza = nuevoInicio < finGuardado && nuevoFin > inicioGuardado;

                if (seCruza) {
                    Toast.makeText(requireContext(), "Ese horario se cruza con otro", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        agregarFilaHorarioServicio(dia, inicio, fin);

        spinnerDiaServicio.setSelection(0);
        spinnerHoraInicioServicio.setSelection(0);
        spinnerHoraFinServicio.setSelection(0);
    }

    private void agregarFilaHorarioServicio(String dia, String inicio, String fin) {
        String[] horario = {dia, inicio, fin};
        horariosServicioAgregados.add(horario);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtDia = new TextView(requireContext());
        txtDia.setText(dia);
        txtDia.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtInicio = new TextView(requireContext());
        txtInicio.setText(inicio);
        txtInicio.setGravity(android.view.Gravity.CENTER);
        txtInicio.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtFin = new TextView(requireContext());
        txtFin.setText(fin);
        txtFin.setGravity(android.view.Gravity.CENTER);
        txtFin.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(android.view.Gravity.CENTER);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(45, 40));

        btnEliminar.setOnClickListener(v -> {
            horariosServicioAgregados.remove(horario);
            listaHorariosServicio.removeView(fila);
        });

        fila.addView(txtDia);
        fila.addView(txtInicio);
        fila.addView(txtFin);
        fila.addView(btnEliminar);

        listaHorariosServicio.addView(fila);
    }
    private void repintarHorariosNegocio() {
        listaHorarios.removeAllViews();

        for (String[] h : horariosAgregados) {
            pintarFilaHorarioNegocio(h);
        }
    }

    private void pintarFilaHorarioNegocio(String[] horario) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtDia = new TextView(requireContext());
        txtDia.setText(horario[0]);
        txtDia.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtInicio = new TextView(requireContext());
        txtInicio.setText(horario[1]);
        txtInicio.setGravity(Gravity.CENTER);
        txtInicio.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtFin = new TextView(requireContext());
        txtFin.setText(horario[2]);
        txtFin.setGravity(Gravity.CENTER);
        txtFin.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(45, 40));

        btnEliminar.setOnClickListener(v -> {
            horariosAgregados.remove(horario);
            listaHorarios.removeView(fila);
        });

        fila.addView(txtDia);
        fila.addView(txtInicio);
        fila.addView(txtFin);
        fila.addView(btnEliminar);

        listaHorarios.addView(fila);
    }
    private void repintarHorariosServicio() {
        listaHorariosServicio.removeAllViews();

        for (String[] h : horariosServicioAgregados) {
            pintarFilaHorarioServicio(h);
        }
    }

    private void pintarFilaHorarioServicio(String[] horario) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtDia = new TextView(requireContext());
        txtDia.setText(horario[0]);
        txtDia.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtInicio = new TextView(requireContext());
        txtInicio.setText(horario[1]);
        txtInicio.setGravity(Gravity.CENTER);
        txtInicio.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtFin = new TextView(requireContext());
        txtFin.setText(horario[2]);
        txtFin.setGravity(Gravity.CENTER);
        txtFin.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(45, 40));

        btnEliminar.setOnClickListener(v -> {
            horariosServicioAgregados.remove(horario);
            listaHorariosServicio.removeView(fila);
        });

        fila.addView(txtDia);
        fila.addView(txtInicio);
        fila.addView(txtFin);
        fila.addView(btnEliminar);

        listaHorariosServicio.addView(fila);
    }
    private void repintarEspecialidadesServicio() {
        listaEspecialidades.removeAllViews();

        for (String[] e : especialidadesAgregadas) {
            pintarFilaEspecialidadServicio(e);
        }
    }

    private void pintarFilaEspecialidadServicio(String[] especialidad) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtNombre = new TextView(requireContext());
        txtNombre.setText(especialidad[1]);
        txtNombre.setGravity(Gravity.CENTER);
        txtNombre.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtNumero = new TextView(requireContext());
        txtNumero.setText(especialidad[2]);
        txtNumero.setGravity(Gravity.CENTER);
        txtNumero.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView txtTipo = new TextView(requireContext());
        txtTipo.setText(especialidad[3]);
        txtTipo.setGravity(Gravity.CENTER);
        txtTipo.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(45, 40));

        btnEliminar.setOnClickListener(v -> {
            especialidadesAgregadas.remove(especialidad);
            listaEspecialidades.removeView(fila);
        });

        fila.addView(txtNombre);
        fila.addView(txtNumero);
        fila.addView(txtTipo);
        fila.addView(btnEliminar);

        listaEspecialidades.addView(fila);
    }
    private void validarImagenNegocioYRegistrar(String imagenUrl) {
        Glide.with(requireContext())
                .load(imagenUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        imagenNegocioValida = false;
                        imagenNegocioValidadaUrl = "";
                        Toast.makeText(requireContext(), "El enlace ingresado no proyecta una imagen", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        imagenNegocioValida = true;
                        imagenNegocioValidadaUrl = imagenUrl;
                        registrarNegocio();
                        return false;
                    }
                })
                .preload();
    }

    private void validarImagenServicioYRegistrar(String imagenUrl) {
        Glide.with(requireContext())
                .load(imagenUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        imagenServicioValida = false;
                        imagenServicioValidadaUrl = "";
                        Toast.makeText(requireContext(), "El enlace ingresado no proyecta una imagen", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        imagenServicioValida = true;
                        imagenServicioValidadaUrl = imagenUrl;
                        registrarServicio();
                        return false;
                    }
                })
                .preload();
    }
}