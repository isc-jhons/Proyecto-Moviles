package com.example.proy_moviles;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    TextView btnMenu;
    TextView btnInicio;
    TextView btnBusquedaServicio;
    TextView btnIniciarSesion;
    TextView btnRegistrarServicio;
    TextView btnMiNegocio;
    TextView btnMiServicio;
    TextView btnComentarios;
    TextView btnMiPerfil;
    TextView btnCerrarSesion;
    View lineaUsuario;
    View lineaUsuario2;
    TextView btnAdministrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        btnInicio = findViewById(R.id.btnInicio);
        btnMiPerfil = findViewById(R.id.btnMiPerfil);
        btnAdministrar = findViewById(R.id.btnAdministrar);
        btnBusquedaServicio = findViewById(R.id.btnBusquedaServicio);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        lineaUsuario = findViewById(R.id.lineaUsuario);
        btnRegistrarServicio = findViewById(R.id.btnRegistrarServicio);
        btnMiNegocio = findViewById(R.id.btnMiNegocio);
        btnMiServicio = findViewById(R.id.btnMiServicio);
        lineaUsuario2 = findViewById(R.id.lineaUsuario2);
        btnComentarios = findViewById(R.id.btnComentarios);


        btnMiPerfil.setOnClickListener(v -> {
            cargarFragment(new perfilusuario());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnCerrarSesion =
                findViewById(R.id.btnCerrarSesion);
        btnMenu.setOnClickListener(v -> {
            actualizarMenu();
            drawerLayout.openDrawer(Gravity.LEFT);
        });

        btnInicio.setOnClickListener(v -> {
            cargarFragment(new inicio());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnBusquedaServicio.setOnClickListener(v -> {
            cargarFragment(new mapaglobal());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnRegistrarServicio.setOnClickListener(v -> {
            cargarFragment(new registrarsn());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnMiNegocio.setOnClickListener(v -> {
            abrirEditarNegocioServicio("negocio");
        });

        btnMiServicio.setOnClickListener(v -> {
            abrirEditarNegocioServicio("servicio");
        });
        btnIniciarSesion.setOnClickListener(v -> {
            cargarFragment(new login());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnAdministrar.setOnClickListener(v -> {
            cargarFragment(new administrar());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });
        btnComentarios.setOnClickListener(v -> {
            cargarFragment(new comentarios());
            drawerLayout.closeDrawer(Gravity.LEFT);
        });

        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            actualizarMenu();
            cargarFragment(new inicio());
            drawerLayout.closeDrawer(Gravity.LEFT);
            Toast.makeText(
                    this,
                    "Sesión cerrada",
                    Toast.LENGTH_SHORT
            ).show();
        });
        FirebaseAuth.getInstance().signOut();
        actualizarMenu();
        cargarFragment(new inicio());
    }

    public void actualizarMenu() {

        boolean sesionIniciada =
                FirebaseAuth.getInstance()
                        .getCurrentUser() != null;

        if (!sesionIniciada) {

            btnIniciarSesion.setVisibility(View.VISIBLE);

            lineaUsuario.setVisibility(View.GONE);
            btnRegistrarServicio.setVisibility(View.GONE);
            btnMiNegocio.setVisibility(View.GONE);
            btnMiServicio.setVisibility(View.GONE);
            lineaUsuario2.setVisibility(View.GONE);
            btnComentarios.setVisibility(View.GONE);
            btnMiPerfil.setVisibility(View.GONE);
            btnCerrarSesion.setVisibility(View.GONE);

            btnAdministrar.setVisibility(View.GONE);

            return;
        }

        btnIniciarSesion.setVisibility(View.GONE);

        lineaUsuario.setVisibility(View.VISIBLE);
        btnRegistrarServicio.setVisibility(View.VISIBLE);

        String uidActual = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        com.google.firebase.firestore.FirebaseFirestore
                .getInstance()
                .collection("negocios")
                .whereEqualTo("uidDueno", uidActual)
                .whereEqualTo("estado", "Aprobado")
                .get()
                .addOnSuccessListener(query -> {

                    btnMiNegocio.setVisibility(View.GONE);
                    btnMiServicio.setVisibility(View.GONE);

                    for (var documento : query) {

                        NegocioServicio negocioServicio =
                                documento.toObject(NegocioServicio.class);

                        if (negocioServicio.getTipo() == null) {
                            continue;
                        }

                        if (negocioServicio.getTipo().equalsIgnoreCase("negocio")) {
                            btnMiNegocio.setVisibility(View.VISIBLE);
                        }

                        if (negocioServicio.getTipo().equalsIgnoreCase("servicio")) {
                            btnMiServicio.setVisibility(View.VISIBLE);
                        }
                    }
                });
        lineaUsuario2.setVisibility(View.VISIBLE);
        btnComentarios.setVisibility(View.VISIBLE);
        btnMiPerfil.setVisibility(View.VISIBLE);
        btnCerrarSesion.setVisibility(View.VISIBLE);

        com.google.firebase.firestore.FirebaseFirestore
                .getInstance()
                .collection("usuariologin")
                .document(
                        FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getUid()
                )
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        btnAdministrar.setVisibility(View.GONE);
                        return;
                    }

                    UsuarioLogin usuario =
                            documentSnapshot.toObject(UsuarioLogin.class);

                    if (usuario == null) {
                        btnAdministrar.setVisibility(View.GONE);
                        return;
                    }

                    if (usuario.getRol() != null &&
                            usuario.getRol().equalsIgnoreCase("admin")) {

                        btnAdministrar.setVisibility(View.VISIBLE);

                    } else {

                        btnAdministrar.setVisibility(View.GONE);
                    }
                });
    }
    private void abrirEditarNegocioServicio(String tipo) {

        editarnegocioservicio fragment = new editarnegocioservicio();

        Bundle bundle = new Bundle();
        bundle.putString("tipo", tipo);

        fragment.setArguments(bundle);

        cargarFragment(fragment);
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    private void cargarFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragment)
                .commit();
    }
}