package com.example.proy_moviles;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputFilter;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class editarnegocioservicio extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;

    String tipoRecibido = "negocio";
    String idDocumento = "";
    double latitud = 0;
    double longitud = 0;

    ImageView imgEditarBanner;
    Button btnEditarFoto, btnGuardarEditar, btnEliminarEditar;
    EditText txtEditarImagenUrl, txtEditarNombre, txtEditarRuc, txtEditarDni,
            txtEditarDescripcion, txtEditarReferencia, txtEditarDireccion,
            txtEditarTelefono, txtEditarWhatsapp, txtEditarTiempoExperiencia;

    TextView txtEditarCategoria, txtEditarPromedio, txtEditarPersonas,
            txtEditarEstado, tabEditarDescripcion, tabEditarComentarios,
            tabEditarProyectos, btnEditarAgregarHorario, btnEditarAgregarEspecialidad;

    RatingBar ratingEditar;

    LinearLayout layoutEditarRuc, layoutEditarDni, layoutEditarReferencia,
            layoutEditarEspecialidades, listaEditarEspecialidades,
            listaEditarHorarios, layoutEditarComentarios,
            listaEditarComentarios;

    FrameLayout layoutEditarProyectos;

    LinearLayout listaEditarPublicaciones;
    TextView txtSinPublicaciones;
    View scrollEditarDescripcion;

    Spinner spinnerEditarEspecialidad, spinnerEditarTipoExperiencia,
            spinnerEditarDia, spinnerEditarHoraInicio, spinnerEditarHoraFin;

    ArrayList<String[]> horariosAgregados = new ArrayList<>();
    ArrayList<String[]> especialidadesAgregadas = new ArrayList<>();
    ArrayList<String> horasBase = new ArrayList<>();

    boolean imagenValida = false;
    String imagenValidadaUrl = "";

    ImageButton btnAgregarPublicacion;

    public editarnegocioservicio() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(
                R.layout.fragment_editarnegocioservicio,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        enlazarVistas(vista);

        if (getArguments() != null) {
            tipoRecibido = getArguments().getString("tipo", "negocio");
        }

        configurarTabs();
        configurarImagen();
        configurarEdicion();
        configurarLimitesCampos();
        cargarCombos();
        escucharMapa();

        btnGuardarEditar.setOnClickListener(v -> guardarCambios());
        btnEliminarEditar.setOnClickListener(v -> eliminarDocumento());
        btnEditarAgregarHorario.setOnClickListener(v -> agregarHorarioDesdeCombo());
        btnEditarAgregarEspecialidad.setOnClickListener(v -> agregarEspecialidadDesdeCombo());

        txtEditarDireccion.setOnClickListener(v -> abrirMapa());

        cargarDocumento();

        btnAgregarPublicacion.setOnClickListener(v -> {

            if(idDocumento.isEmpty()){
                Toast.makeText(
                        requireContext(),
                        "Espere a que cargue la información",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            registrarpublicacion fragment =
                    new registrarpublicacion();

            Bundle bundle = new Bundle();
            bundle.putString("idNegocioServicio", idDocumento);
            bundle.putString("tipo", tipoRecibido);

            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return vista;
    }

    private void enlazarVistas(View vista) {
        imgEditarBanner = vista.findViewById(R.id.imgEditarBanner);
        btnEditarFoto = vista.findViewById(R.id.btnEditarFoto);
        txtEditarImagenUrl = vista.findViewById(R.id.txtEditarImagenUrl);

        txtEditarNombre = vista.findViewById(R.id.txtEditarNombre);
        txtEditarCategoria = vista.findViewById(R.id.txtEditarCategoria);
        ratingEditar = vista.findViewById(R.id.ratingEditar);
        txtEditarPromedio = vista.findViewById(R.id.txtEditarPromedio);
        txtEditarPersonas = vista.findViewById(R.id.txtEditarPersonas);
        txtEditarEstado = vista.findViewById(R.id.txtEditarEstado);

        tabEditarDescripcion = vista.findViewById(R.id.tabEditarDescripcion);
        tabEditarComentarios = vista.findViewById(R.id.tabEditarComentarios);
        tabEditarProyectos = vista.findViewById(R.id.tabEditarProyectos);

        scrollEditarDescripcion = vista.findViewById(R.id.scrollEditarDescripcion);
        layoutEditarComentarios = vista.findViewById(R.id.layoutEditarComentarios);
        layoutEditarProyectos = vista.findViewById(R.id.layoutEditarProyectos);
        listaEditarComentarios = vista.findViewById(R.id.listaEditarComentarios);

        layoutEditarRuc = vista.findViewById(R.id.layoutEditarRuc);
        layoutEditarDni = vista.findViewById(R.id.layoutEditarDni);
        layoutEditarReferencia = vista.findViewById(R.id.layoutEditarReferencia);
        layoutEditarEspecialidades = vista.findViewById(R.id.layoutEditarEspecialidades);

        txtEditarRuc = vista.findViewById(R.id.txtEditarRuc);
        txtEditarDni = vista.findViewById(R.id.txtEditarDni);
        txtEditarDescripcion = vista.findViewById(R.id.txtEditarDescripcion);
        txtEditarReferencia = vista.findViewById(R.id.txtEditarReferencia);
        txtEditarDireccion = vista.findViewById(R.id.txtEditarDireccion);
        txtEditarTelefono = vista.findViewById(R.id.txtEditarTelefono);
        txtEditarWhatsapp = vista.findViewById(R.id.txtEditarWhatsapp);

        spinnerEditarEspecialidad = vista.findViewById(R.id.spinnerEditarEspecialidad);
        txtEditarTiempoExperiencia = vista.findViewById(R.id.txtEditarTiempoExperiencia);
        spinnerEditarTipoExperiencia = vista.findViewById(R.id.spinnerEditarTipoExperiencia);
        btnEditarAgregarEspecialidad = vista.findViewById(R.id.btnEditarAgregarEspecialidad);
        listaEditarEspecialidades = vista.findViewById(R.id.listaEditarEspecialidades);

        spinnerEditarDia = vista.findViewById(R.id.spinnerEditarDia);
        spinnerEditarHoraInicio = vista.findViewById(R.id.spinnerEditarHoraInicio);
        spinnerEditarHoraFin = vista.findViewById(R.id.spinnerEditarHoraFin);
        btnEditarAgregarHorario = vista.findViewById(R.id.btnEditarAgregarHorario);
        listaEditarHorarios = vista.findViewById(R.id.listaEditarHorarios);

        btnGuardarEditar = vista.findViewById(R.id.btnGuardarEditar);
        btnEliminarEditar = vista.findViewById(R.id.btnEliminarEditar);

        btnAgregarPublicacion = vista.findViewById(R.id.btnAgregarPublicacion);
        listaEditarPublicaciones =
                vista.findViewById(R.id.listaEditarPublicaciones);

        txtSinPublicaciones =
                vista.findViewById(R.id.txtSinPublicaciones);

    }

    private void configurarTabs() {
        seleccionarTab("descripcion");

        tabEditarDescripcion.setOnClickListener(v -> seleccionarTab("descripcion"));
        tabEditarComentarios.setOnClickListener(v -> seleccionarTab("comentarios"));
        tabEditarProyectos.setOnClickListener(v -> seleccionarTab("proyectos"));
    }

    private void seleccionarTab(String tab) {
        scrollEditarDescripcion.setVisibility(View.GONE);
        layoutEditarComentarios.setVisibility(View.GONE);
        layoutEditarProyectos.setVisibility(View.GONE);

        tabEditarDescripcion.setBackgroundColor(Color.WHITE);
        tabEditarComentarios.setBackgroundColor(Color.WHITE);
        tabEditarProyectos.setBackgroundColor(Color.WHITE);

        if (tab.equals("descripcion")) {
            scrollEditarDescripcion.setVisibility(View.VISIBLE);
            tabEditarDescripcion.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else if (tab.equals("comentarios")) {
            layoutEditarComentarios.setVisibility(View.VISIBLE);
            tabEditarComentarios.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else {
            layoutEditarProyectos.setVisibility(View.VISIBLE);
            tabEditarProyectos.setBackgroundColor(Color.parseColor("#EEEEEE"));
        }
    }

    private void configurarImagen() {
        btnEditarFoto.setOnClickListener(v -> {
            txtEditarImagenUrl.setVisibility(View.VISIBLE);
        });

        txtEditarImagenUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();

                if (!url.isEmpty() && url.startsWith("http")) {
                    Glide.with(requireContext())
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgEditarBanner);
                }
            }
        });
    }

    private void configurarEdicion() {
        activarEdicion(txtEditarNombre);
        activarEdicion(txtEditarRuc);
        activarEdicion(txtEditarDni);
        activarEdicion(txtEditarDescripcion);
        activarEdicion(txtEditarReferencia);
        activarEdicion(txtEditarTelefono);
        activarEdicion(txtEditarWhatsapp);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void activarEdicion(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setCursorVisible(false);

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2;

                if (editText.getCompoundDrawables()[drawableEnd] != null &&
                        event.getRawX() >= editText.getRight()
                                - editText.getCompoundDrawables()[drawableEnd].getBounds().width()
                                - editText.getPaddingEnd()) {

                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    editText.setCursorVisible(true);
                    editText.requestFocus();
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }

            return false;
        });
    }

    private void cargarCombos() {
        String[] dias = {
                "Día", "lunes", "martes", "miércoles",
                "jueves", "viernes", "sábado", "domingo"
        };

        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dias
        );
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditarDia.setAdapter(adapterDias);


        horasBase.clear();
        horasBase.add("Hora");

        for (int h = 0; h <= 23; h++) {
            horasBase.add(String.format(Locale.getDefault(), "%02d:00", h));
            horasBase.add(String.format(Locale.getDefault(), "%02d:30", h));
        }

        ArrayAdapter<String> adapterHoras = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                horasBase
        );
        adapterHoras.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerEditarHoraInicio.setAdapter(adapterHoras);
        cargarHorasFinEditarDesde(0);

        spinnerEditarHoraInicio.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                cargarHorasFinEditarDesde(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        String[] especialidades = {
                "Especialidad",
                "Gasfiteria",
                "Electricidad",
                "Instalaciones",
                "Mantenimiento",
                "Carpinteria"
        };

        ArrayAdapter<String> adapterEspecialidad = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                especialidades
        );
        adapterEspecialidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditarEspecialidad.setAdapter(adapterEspecialidad);

        String[] tipos = {"Tipo", "meses", "años"};

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                tipos
        );
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditarTipoExperiencia.setAdapter(adapterTipo);
    }

    private void cargarDocumento() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Debe iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("negocios")
                .whereEqualTo("uidDueno", uid)
                .whereEqualTo("estado", "Aprobado")
                .whereEqualTo("tipo", tipoRecibido)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(requireContext(), "No se encontró información", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentReference ref = query.getDocuments().get(0).getReference();
                    idDocumento = ref.getId();

                    NegocioServicio ns =
                            query.getDocuments().get(0).toObject(NegocioServicio.class);

                    if (ns == null) return;

                    cargarDatosPantalla(ns);
                    cargarComentarios();
                    cargarPublicaciones();
                });
    }

    private void cargarDatosPantalla(NegocioServicio ns) {
        latitud = ns.getLatitud();
        longitud = ns.getLongitud();

        if (getArguments() != null &&
                getArguments().containsKey("latitudEditada") &&
                getArguments().containsKey("longitudEditada")) {

            latitud = getArguments().getDouble("latitudEditada");
            longitud = getArguments().getDouble("longitudEditada");
        }

        txtEditarDireccion.setText(latitud + ", " + longitud);

        if (tipoRecibido.equalsIgnoreCase("servicio")) {
            txtEditarNombre.setText(
                    textoSeguro(ns.getNombreDueno()) + " " +
                            textoSeguro(ns.getApellidosDueno())
            );

            txtEditarNombre.setFocusable(false);
            txtEditarNombre.setFocusableInTouchMode(false);
            txtEditarNombre.setCompoundDrawables(null, null, null, null);

            layoutEditarRuc.setVisibility(View.GONE);
            layoutEditarDni.setVisibility(View.VISIBLE);
            layoutEditarReferencia.setVisibility(View.GONE);
            layoutEditarEspecialidades.setVisibility(View.VISIBLE);

            txtEditarDni.setText(textoSeguro(ns.getDni()));
            txtEditarCategoria.setText(obtenerCategoriasServicio(ns));

            cargarEspecialidadesExistentes(ns);

        } else {
            txtEditarNombre.setText(textoSeguro(ns.getRazonSocial()));

            layoutEditarRuc.setVisibility(View.VISIBLE);
            layoutEditarDni.setVisibility(View.GONE);
            layoutEditarReferencia.setVisibility(View.VISIBLE);
            layoutEditarEspecialidades.setVisibility(View.GONE);

            txtEditarRuc.setText(textoSeguro(ns.getRuc()));
            txtEditarReferencia.setText(textoSeguro(ns.getReferencia()));
            txtEditarCategoria.setText(textoSeguro(ns.getCategoriaNombre()));
        }

        txtEditarDescripcion.setText(textoSeguro(ns.getDescripcion()));
        txtEditarWhatsapp.setText(textoSeguro(ns.getWhatsapp()));
        txtEditarTelefono.setText(textoSeguro(ns.getTelefono()));
        txtEditarDireccion.setText(latitud + ", " + longitud);
        txtEditarImagenUrl.setText(textoSeguro(ns.getImagenUrl()));

        ratingEditar.setRating((float) ns.getEstrellasPromedio());
        txtEditarPromedio.setText(String.valueOf(ns.getEstrellasPromedio()));
        txtEditarPersonas.setText(ns.getTotalCalificaciones() + " personas");

        boolean abierto = estaAbierto(ns);
        txtEditarEstado.setText(abierto ? "Abierto" : "Cerrado");
        txtEditarEstado.setBackgroundColor(
                abierto ? Color.parseColor("#43A047") : Color.parseColor("#D50000")
        );

        Glide.with(requireContext())
                .load(ns.getImagenUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imgEditarBanner);

        cargarHorariosExistentes(ns);
    }

    private void cargarHorariosExistentes(NegocioServicio ns) {
        horariosAgregados.clear();
        listaEditarHorarios.removeAllViews();

        if (ns.getHorarios() == null) return;

        String[] ordenDias = {
                "lunes", "martes", "miércoles",
                "jueves", "viernes", "sábado", "domingo"
        };

        for (String dia : ordenDias) {
            if (!ns.getHorarios().containsKey(dia)) continue;

            Map<String, Object> horario =
                    (Map<String, Object>) ns.getHorarios().get(dia);

            String inicio = String.valueOf(horario.get("inicio"));
            String fin = String.valueOf(horario.get("fin"));

            agregarFilaHorario(dia, inicio, fin);
        }
    }

    private void cargarEspecialidadesExistentes(NegocioServicio ns) {
        especialidadesAgregadas.clear();
        listaEditarEspecialidades.removeAllViews();

        if (ns.getEspecialidades() == null) return;

        for (Object value : ns.getEspecialidades().values()) {
            Map<String, Object> esp = (Map<String, Object>) value;

            String nombre = String.valueOf(esp.get("nombre"));
            String numero = String.valueOf(esp.get("experienciaNumero"));
            String tipo = String.valueOf(esp.get("experienciaTipo"));

            agregarFilaEspecialidad(nombre.toLowerCase(), nombre, numero, tipo);
        }
    }

    private void agregarHorarioDesdeCombo() {
        String dia = spinnerEditarDia.getSelectedItem().toString();
        String inicio = spinnerEditarHoraInicio.getSelectedItem().toString();
        String fin = spinnerEditarHoraFin.getSelectedItem().toString();

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

        spinnerEditarDia.setSelection(0);
        spinnerEditarHoraInicio.setSelection(0);
        spinnerEditarHoraFin.setSelection(0);
    }

    private void agregarFilaHorario(String dia, String inicio, String fin) {
        String[] horario = {dia, inicio, fin};
        horariosAgregados.add(horario);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtDia = crearTextoFila(dia);
        TextView txtInicio = crearTextoFila(inicio);
        TextView txtFin = crearTextoFila(fin);

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setTextSize(13);
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setPadding(5, 0, 5, 0);

        btnEliminar.setOnClickListener(v -> {
            horariosAgregados.remove(horario);
            listaEditarHorarios.removeView(fila);
        });

        fila.addView(txtDia);
        fila.addView(txtInicio);
        fila.addView(txtFin);
        fila.addView(btnEliminar);

        listaEditarHorarios.addView(fila);
    }

    private void agregarEspecialidadDesdeCombo() {
        String nombre = spinnerEditarEspecialidad.getSelectedItem().toString();
        String numero = txtEditarTiempoExperiencia.getText().toString().trim();
        String tipo = spinnerEditarTipoExperiencia.getSelectedItem().toString();

        if (nombre.equals("Especialidad")) {
            Toast.makeText(requireContext(), "Seleccione una especialidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numero.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese experiencia", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipo.equals("Tipo")) {
            Toast.makeText(requireContext(), "Seleccione meses o años", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoriaId = nombre.toLowerCase()
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

        agregarFilaEspecialidad(categoriaId, nombre, numero, tipo);

        spinnerEditarEspecialidad.setSelection(0);
        txtEditarTiempoExperiencia.setText("");
        spinnerEditarTipoExperiencia.setSelection(0);
    }

    private void agregarFilaEspecialidad(String id, String nombre, String numero, String tipo) {
        String[] esp = {id, nombre, numero, tipo};
        especialidadesAgregadas.add(esp);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(4, 6, 4, 6);

        TextView txtNombre = crearTextoFila(nombre);
        TextView txtNumero = crearTextoFila(numero);
        TextView txtTipo = crearTextoFila(tipo);

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setText("🗑");
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(45, 40));

        btnEliminar.setOnClickListener(v -> {
            especialidadesAgregadas.remove(esp);
            listaEditarEspecialidades.removeView(fila);
        });

        fila.addView(txtNombre);
        fila.addView(txtNumero);
        fila.addView(txtTipo);
        fila.addView(btnEliminar);

        listaEditarEspecialidades.addView(fila);
    }

    private TextView crearTextoFila(String texto) {
        TextView tv = new TextView(requireContext());
        tv.setText(texto);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1));
        return tv;
    }

    private void guardarCambios() {
        if (idDocumento.isEmpty()) return;

        String nombre = txtEditarNombre.getText().toString().trim();
        String ruc = txtEditarRuc.getText().toString().trim();
        String dni = txtEditarDni.getText().toString().trim();
        String descripcion = txtEditarDescripcion.getText().toString().trim();
        String referencia = txtEditarReferencia.getText().toString().trim();
        String telefono = txtEditarTelefono.getText().toString().trim();
        String whatsapp = txtEditarWhatsapp.getText().toString().trim();
        String imagenUrl = txtEditarImagenUrl.getText().toString().trim();
        String direccion = txtEditarDireccion.getText().toString().trim();

        if (descripcion.isEmpty()
                || telefono.isEmpty()
                || whatsapp.isEmpty()
                || imagenUrl.isEmpty()
                || direccion.isEmpty()) {

            Toast.makeText(requireContext(), "Ningún campo puede quedar vacío", Toast.LENGTH_SHORT).show();
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

        if (horariosAgregados.isEmpty()) {
            Toast.makeText(requireContext(), "Agregue al menos un horario de atención", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenUrl.startsWith("http")) {
            Toast.makeText(requireContext(), "El enlace ingresado no proyecta una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imagenValida || !imagenUrl.equals(imagenValidadaUrl)) {
            validarImagenYGuardar(imagenUrl);
            return;
        }

        if (tipoRecibido.equalsIgnoreCase("negocio")) {

            if (nombre.isEmpty()
                    || ruc.isEmpty()
                    || referencia.isEmpty()) {

                Toast.makeText(requireContext(), "Ningún campo del negocio puede quedar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ruc.matches("\\d{11}")) {
                Toast.makeText(requireContext(), "El RUC debe tener exactamente 11 números", Toast.LENGTH_SHORT).show();
                return;
            }

            validarRucUnicoYGuardar(ruc, nombre, descripcion, referencia, telefono, whatsapp, imagenUrl);

        } else {

            if (dni.isEmpty()) {
                Toast.makeText(requireContext(), "El DNI / CE no puede quedar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!dni.matches("\\d{8}")) {
                Toast.makeText(requireContext(), "El DNI / CE debe tener exactamente 8 números", Toast.LENGTH_SHORT).show();
                return;
            }

            if (especialidadesAgregadas.isEmpty()) {
                Toast.makeText(requireContext(), "Agregue al menos una especialidad", Toast.LENGTH_SHORT).show();
                return;
            }

            validarDniUnicoYGuardar(
                    dni,
                    nombre,
                    ruc,
                    descripcion,
                    referencia,
                    telefono,
                    whatsapp,
                    imagenUrl
            );
        }
    }
    private void validarDniUnicoYGuardar(
            String dni,
            String nombre,
            String ruc,
            String descripcion,
            String referencia,
            String telefono,
            String whatsapp,
            String imagenUrl
    ) {

        db.collection("negocios")
                .whereEqualTo("dni", dni)
                .get()
                .addOnSuccessListener(query -> {

                    for (var doc : query.getDocuments()) {

                        if (!doc.getId().equals(idDocumento)) {

                            NegocioServicio existente =
                                    doc.toObject(NegocioServicio.class);

                            if (existente != null &&
                                    existente.getEstado() != null &&
                                    (existente.getEstado().equalsIgnoreCase("Pendiente")
                                            || existente.getEstado().equalsIgnoreCase("Aprobado"))) {

                                Toast.makeText(
                                        requireContext(),
                                        "Ese DNI / CE ya está registrado",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }
                        }
                    }

                    guardarFinal(
                            nombre,
                            ruc,
                            dni,
                            descripcion,
                            referencia,
                            telefono,
                            whatsapp,
                            imagenUrl
                    );
                });
    }
    private void validarRucUnicoYGuardar(
            String ruc,
            String nombre,
            String descripcion,
            String referencia,
            String telefono,
            String whatsapp,
            String imagenUrl
    ) {
        db.collection("negocios")
                .whereEqualTo("ruc", ruc)
                .get()
                .addOnSuccessListener(query -> {

                    for (var doc : query.getDocuments()) {

                        if (!doc.getId().equals(idDocumento)) {

                            NegocioServicio existente =
                                    doc.toObject(NegocioServicio.class);

                            if (existente != null &&
                                    existente.getEstado() != null &&
                                    (existente.getEstado().equalsIgnoreCase("Pendiente")
                                            || existente.getEstado().equalsIgnoreCase("Aprobado"))) {

                                Toast.makeText(
                                        requireContext(),
                                        "Ese RUC ya está registrado",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }
                        }
                    }

                    guardarFinal(
                            nombre,
                            ruc,
                            "",
                            descripcion,
                            referencia,
                            telefono,
                            whatsapp,
                            imagenUrl
                    );
                });
    }

    private void validarImagenYGuardar(String imagenUrl) {
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
                        guardarCambios();
                        return false;
                    }
                })
                .preload();
    }

    private void guardarFinal(
            String nombre,
            String ruc,
            String dni,
            String descripcion,
            String referencia,
            String telefono,
            String whatsapp,
            String imagenUrl
    ) {
        Map<String, Object> datos = new HashMap<>();

        datos.put("descripcion", descripcion);
        datos.put("telefono", telefono);
        datos.put("whatsapp", whatsapp);
        datos.put("imagenUrl", imagenUrl);
        datos.put("latitud", latitud);
        datos.put("longitud", longitud);
        datos.put("horarios", obtenerHorariosMap());

        if (tipoRecibido.equalsIgnoreCase("negocio")) {
            datos.put("ruc", ruc);
            datos.put("razonSocial", nombre);
            datos.put("referencia", referencia);
        } else {
            String primeraCategoriaId = especialidadesAgregadas.get(0)[0];
            String primeraCategoriaNombre = especialidadesAgregadas.get(0)[1];

            datos.put("dni", dni);
            datos.put("especialidades", obtenerEspecialidadesMap());
            datos.put("categoriaId", primeraCategoriaId);
            datos.put("categoriaNombre", primeraCategoriaNombre);
        }

        db.collection("negocios")
                .document(idDocumento)
                .update(datos)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contenedorFragments, new inicio())
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> obtenerHorariosMap() {
        Map<String, Object> horarios = new HashMap<>();

        for (String[] h : horariosAgregados) {
            Map<String, Object> horarioDia = new HashMap<>();
            horarioDia.put("abierto", true);
            horarioDia.put("inicio", h[1]);
            horarioDia.put("fin", h[2]);

            horarios.put(h[0], horarioDia);
        }

        return horarios;
    }

    private Map<String, Object> obtenerEspecialidadesMap() {
        Map<String, Object> especialidades = new HashMap<>();

        for (String[] e : especialidadesAgregadas) {
            Map<String, Object> esp = new HashMap<>();
            esp.put("nombre", e[1]);
            esp.put("experienciaNumero", Integer.parseInt(e[2]));
            esp.put("experienciaTipo", e[3]);

            especialidades.put(e[0], esp);
        }

        return especialidades;
    }

    private void eliminarDocumento() {
        if (idDocumento.isEmpty()) return;

        db.collection("negocios")
                .document(idDocumento)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Eliminado correctamente", Toast.LENGTH_SHORT).show();

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contenedorFragments, new inicio())
                            .commit();
                });
    }

    private void cargarComentarios() {
        listaEditarComentarios.removeAllViews();

        db.collection("negocios")
                .document(idDocumento)
                .collection("comentarios")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        TextView vacio = new TextView(requireContext());
                        vacio.setText("Aún no hay comentarios");
                        vacio.setTextColor(Color.GRAY);
                        vacio.setGravity(Gravity.CENTER);
                        vacio.setPadding(0, 20, 0, 20);
                        listaEditarComentarios.addView(vacio);
                        return;
                    }

                    for (var doc : query.getDocuments()) {
                        Comentario comentario = doc.toObject(Comentario.class);

                        if (comentario != null) {
                            crearComentarioSoloLectura(comentario);
                        }
                    }
                });
    }

    private void crearComentarioSoloLectura(Comentario comentario) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(8, 10, 8, 10);
        card.setBackgroundColor(Color.WHITE);

        ImageView imgUsuario = new ImageView(requireContext());
        imgUsuario.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        imgUsuario.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgUsuario.setImageResource(R.drawable.perfilusuario);

        LinearLayout contenido = new LinearLayout(requireContext());
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(10, 0, 0, 0);
        contenido.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView nombre = new TextView(requireContext());
        nombre.setText(comentario.getNombreUsuario());
        nombre.setTypeface(null, Typeface.BOLD);
        nombre.setTextColor(Color.BLACK);
        nombre.setTextSize(14);

        TextView texto = new TextView(requireContext());
        texto.setText(comentario.getComentario());
        texto.setTextColor(Color.BLACK);
        texto.setTextSize(13);

        TextView fecha = new TextView(requireContext());
        fecha.setText(formatearFecha(comentario.getFecha()));
        fecha.setTextColor(Color.GRAY);
        fecha.setTextSize(11);

        TextView likes = new TextView(requireContext());

        likes.setText("👍 Me gusta   " + comentario.getLikes());
        likes.setTextSize(12);
        likes.setTextColor(Color.BLACK);
        likes.setPadding(18, 8, 18, 8);

        android.graphics.drawable.GradientDrawable fondoLike =
                new android.graphics.drawable.GradientDrawable();

        fondoLike.setColor(Color.parseColor("#E0E0E0"));
        fondoLike.setStroke(2, Color.parseColor("#9E9E9E"));
        fondoLike.setCornerRadius(4);

        likes.setBackground(fondoLike);

        LinearLayout.LayoutParams paramsLike =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        paramsLike.topMargin = 6;

        likes.setLayoutParams(paramsLike);

        contenido.addView(nombre);
        contenido.addView(texto);
        contenido.addView(fecha);
        contenido.addView(likes);

        card.addView(imgUsuario);
        card.addView(contenido);

        listaEditarComentarios.addView(card);

        db.collection("usuariologin")
                .document(comentario.getUidUsuario())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String fotoActual = userDoc.getString("fotoUrl");
                    String nombreActual = userDoc.getString("nombre");
                    String apellidosActual = userDoc.getString("apellidos");

                    if (nombreActual != null) {
                        nombre.setText(
                                (nombreActual + " " +
                                        (apellidosActual == null ? "" : apellidosActual)).trim()
                        );
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
        linea.setBackgroundColor(Color.parseColor("#CCCCCC"));
        listaEditarComentarios.addView(linea);
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
    private void abrirMapa() {
        seleccionarmapa fragment = new seleccionarmapa();

        Bundle bundle = new Bundle();
        bundle.putString("origen", "editar");

        fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.contenedorFragments, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void escucharMapa() {
        getParentFragmentManager().setFragmentResultListener(
                "ubicacionNegocio",
                this,
                (requestKey, bundle) -> {

                    String origen = bundle.getString("origen", "");

                    if (!origen.equals("editar")) {
                        return;
                    }

                    latitud = bundle.getDouble("latitud");
                    longitud = bundle.getDouble("longitud");

                    txtEditarDireccion.setText(latitud + ", " + longitud);
                }
        );
    }

    private boolean estaAbierto(NegocioServicio ns) {
        if (ns.getHorarios() == null) return false;

        String diaActual = new SimpleDateFormat("EEEE", new Locale("es", "ES"))
                .format(new Date())
                .toLowerCase();

        Map<String, Object> horarioDia =
                (Map<String, Object>) ns.getHorarios().get(diaActual);

        if (horarioDia == null) return false;

        int ahora = convertirMinutos(
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date())
        );

        int inicio = convertirMinutos(String.valueOf(horarioDia.get("inicio")));
        int fin = convertirMinutos(String.valueOf(horarioDia.get("fin")));

        return ahora >= inicio && ahora <= fin;
    }

    private int convertirMinutos(String hora) {
        String[] partes = hora.split(":");
        return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
    }

    private String obtenerCategoriasServicio(NegocioServicio ns) {
        String texto = "";

        if (ns.getEspecialidades() == null) return "";

        for (Object value : ns.getEspecialidades().values()) {
            Map<String, Object> esp = (Map<String, Object>) value;
            texto += esp.get("nombre") + ", ";
        }

        if (texto.endsWith(", ")) {
            texto = texto.substring(0, texto.length() - 2);
        }

        return texto;
    }

    private String formatearFecha(long fecha) {
        return new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date(fecha));
    }

    private void cargarHorasFinEditarDesde(int posicionInicio) {
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
        spinnerEditarHoraFin.setAdapter(adapterFin);
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private void configurarLimitesCampos() {
        txtEditarRuc.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(11)
        });

        txtEditarDni.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(8)
        });

        txtEditarTelefono.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(9)
        });

        txtEditarWhatsapp.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(9)
        });
    }

    private void cargarPublicaciones() {

        listaEditarPublicaciones.removeAllViews();

        db.collection("negocios")
                .document(idDocumento)
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
                        listaEditarPublicaciones.addView(vacio);
                        return;
                    }

                    for (var doc : query.getDocuments()) {

                        Publicacion p = doc.toObject(Publicacion.class);

                        if (p == null) continue;

                        crearPublicacion(
                                p.getTitulo(),
                                p.getDescripcion(),
                                p.getImagenUrl(),
                                doc.getId()
                        );
                    }
                });
    }
    private void crearPublicacion(
            String titulo,
            String descripcion,
            String imagenUrl,
            String idPublicacion
    ) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(0,0,0,20);

        ImageView imagen = new ImageView(requireContext());

        imagen.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(130)
                )
        );

        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(requireContext())
                .load(imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imagen);

        card.addView(imagen);

        LinearLayout info = new LinearLayout(requireContext());

        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setBackgroundColor(Color.parseColor("#D9D9D9"));
        info.setPadding(10, 6, 10, 6);

        LinearLayout textos = new LinearLayout(requireContext());

        textos.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams paramsTextos =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        paramsTextos.setMargins(0, 0, dp(8), 0);

        textos.setLayoutParams(paramsTextos);

        TextView txtTitulo = new TextView(requireContext());
        txtTitulo.setText(titulo);
        txtTitulo.setTypeface(null, Typeface.BOLD);
        txtTitulo.setTextColor(Color.BLACK);

        TextView txtDescripcion = new TextView(requireContext());
        txtDescripcion.setText(descripcion);
        txtDescripcion.setTextColor(Color.BLACK);
        txtDescripcion.setTextSize(11);
        txtDescripcion.setMaxLines(3);
        txtDescripcion.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        textos.addView(txtTitulo);
        textos.addView(txtDescripcion);

        Button btnEliminar = new Button(requireContext());

        LinearLayout.LayoutParams paramsEliminar =
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(38)
                );

        btnEliminar.setLayoutParams(paramsEliminar);

        btnEliminar.setText("🗑");

        btnEliminar.setBackgroundColor(
                Color.parseColor("#D32F2F")
        );

        btnEliminar.setTextColor(Color.WHITE);

        btnEliminar.setOnClickListener(v -> {

            db.collection("negocios")
                    .document(idDocumento)
                    .collection("publicaciones")
                    .document(idPublicacion)
                    .delete()
                    .addOnSuccessListener(unused -> {

                        listaEditarPublicaciones.removeView(card);

                        if (listaEditarPublicaciones.getChildCount() == 0) {
                            TextView vacio = new TextView(requireContext());
                            vacio.setText("Aún no hay publicaciones");
                            vacio.setTextColor(Color.GRAY);
                            vacio.setGravity(Gravity.CENTER);
                            vacio.setPadding(0, 20, 0, 20);
                            listaEditarPublicaciones.addView(vacio);
                        }

                        Toast.makeText(
                                requireContext(),
                                "Publicación eliminada",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });

        info.addView(textos);
        info.addView(btnEliminar);

        card.addView(info);

        listaEditarPublicaciones.addView(card);
    }
}