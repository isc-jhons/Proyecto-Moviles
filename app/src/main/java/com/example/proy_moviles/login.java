package com.example.proy_moviles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends Fragment {

    EditText txtUsuarioLogin;
    EditText txtPasswordLogin;

    Button btnLogin;

    TextView txtRegistrate;

    FirebaseAuth auth;
    FirebaseFirestore db;

    public login() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_login, container, false);

        txtUsuarioLogin = vista.findViewById(R.id.txtUsuarioLogin);
        txtPasswordLogin = vista.findViewById(R.id.txtPasswordLogin);
        btnLogin = vista.findViewById(R.id.btnLogin);
        txtRegistrate = vista.findViewById(R.id.txtRegistrate);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> iniciarSesion());

        txtRegistrate.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new registarusuario())
                    .commit();
        });

        return vista;
    }

    private void iniciarSesion() {

        String nickname = txtUsuarioLogin.getText().toString().trim();
        String password = txtPasswordLogin.getText().toString().trim();

        if (nickname.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Ingrese usuario y contraseña",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        db.collection("usuariologin")
                .whereEqualTo("nickname", nickname)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                "Usuario no encontrado",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    UsuarioLogin usuario =
                            query.getDocuments()
                                    .get(0)
                                    .toObject(UsuarioLogin.class);

                    if (usuario == null) {
                        Toast.makeText(
                                requireContext(),
                                "Error al leer usuario",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    auth.signInWithEmailAndPassword(
                            usuario.getCorreo(),
                            password
                    ).addOnSuccessListener(authResult -> {

                        Toast.makeText(
                                requireContext(),
                                "Bienvenido " + usuario.getNombre(),
                                Toast.LENGTH_SHORT
                        ).show();

                        ((MainActivity) requireActivity()).actualizarMenu();

                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.contenedorFragments, new inicio())
                                .commit();

                    }).addOnFailureListener(e -> {
                        Toast.makeText(
                                requireContext(),
                                "Contraseña incorrecta",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            requireContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}