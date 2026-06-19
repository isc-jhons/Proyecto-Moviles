package com.example.proy_moviles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class registarusuario extends Fragment {

    TextView txtIrLogin;

    EditText txtNombreRegistro;
    EditText txtApellidosRegistro;
    EditText txtNickRegistro;
    EditText txtCorreoRegistro;
    EditText txtTelefonoRegistro;
    EditText txtPasswordRegistro;
    EditText txtConfirmarPasswordRegistro;

    Button btnCrearCuenta;

    FirebaseAuth auth;
    FirebaseFirestore db;

    boolean visiblePassword = false;
    boolean visibleConfirmar = false;

    public registarusuario() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_registarusuario, container, false);

        txtIrLogin = vista.findViewById(R.id.txtIrLogin);

        txtNombreRegistro = vista.findViewById(R.id.txtNombreRegistro);
        txtApellidosRegistro = vista.findViewById(R.id.txtApellidosRegistro);
        txtNickRegistro = vista.findViewById(R.id.txtNickRegistro);
        txtCorreoRegistro = vista.findViewById(R.id.txtCorreoRegistro);
        txtTelefonoRegistro = vista.findViewById(R.id.txtTelefonoRegistro);
        txtPasswordRegistro = vista.findViewById(R.id.txtPasswordRegistro);
        txtConfirmarPasswordRegistro = vista.findViewById(R.id.txtConfirmarPasswordRegistro);

        btnCrearCuenta = vista.findViewById(R.id.btnCrearCuenta);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtIrLogin.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragments, new login())
                    .commit();
        });

        activarOjoPassword(txtPasswordRegistro, true);
        activarOjoPassword(txtConfirmarPasswordRegistro, false);

        btnCrearCuenta.setOnClickListener(v -> registrarUsuario());

        return vista;
    }

    private void registrarUsuario() {

        String nombre = txtNombreRegistro.getText().toString().trim();
        String apellidos = txtApellidosRegistro.getText().toString().trim();
        String nickname = txtNickRegistro.getText().toString().trim();
        String correo = txtCorreoRegistro.getText().toString().trim();
        String telefono = txtTelefonoRegistro.getText().toString().trim();
        String password = txtPasswordRegistro.getText().toString().trim();
        String confirmarPassword = txtConfirmarPasswordRegistro.getText().toString().trim();

        if (nombre.isEmpty() ||
                apellidos.isEmpty() ||
                nickname.isEmpty() ||
                correo.isEmpty() ||
                telefono.isEmpty() ||
                password.isEmpty() ||
                confirmarPassword.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!telefono.matches("\\d{9}")) {
            Toast.makeText(
                    requireContext(),
                    "El teléfono debe tener 9 números",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (nombre.length() > 40 ||
                apellidos.length() > 40 ||
                nickname.length() > 40 ||
                correo.length() > 40 ||
                password.length() > 40) {

            Toast.makeText(
                    requireContext(),
                    "Los campos no deben superar 40 caracteres",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            Toast.makeText(
                    requireContext(),
                    "Las contraseñas no coinciden",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (password.length() < 6) {
            Toast.makeText(
                    requireContext(),
                    "La contraseña debe tener mínimo 6 caracteres",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("usuariologin")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryNickname -> {

                    if (!queryNickname.isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                "El nickname ya existe",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    db.collection("usuariologin")
                            .whereEqualTo("correo", correo)
                            .get()
                            .addOnSuccessListener(queryCorreo -> {

                                if (!queryCorreo.isEmpty()) {
                                    Toast.makeText(
                                            requireContext(),
                                            "El correo ya existe",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                registrarFirebase(
                                        nombre,
                                        apellidos,
                                        nickname,
                                        correo,
                                        telefono,
                                        password
                                );
                            });
                });
        return;
    }

    private void registrarFirebase(
            String nombre,
            String apellidos,
            String nickname,
            String correo,
            String telefono,
            String password
    ) {

        auth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    UsuarioLogin usuario = new UsuarioLogin(
                            uid,
                            nombre,
                            apellidos,
                            nickname,
                            correo,
                            telefono,
                            "usuario",
                            "",
                            System.currentTimeMillis(),
                            true
                    );

                    db.collection("usuariologin")
                            .document(uid)
                            .set(usuario)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        requireContext(),
                                        "Usuario registrado correctamente",
                                        Toast.LENGTH_SHORT
                                ).show();

                                requireActivity()
                                        .getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.contenedorFragments, new login())
                                        .commit();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        requireContext(),
                                        "Error al guardar usuario: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            requireContext(),
                            "Error al registrar: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void activarOjoPassword(EditText editText, boolean esPasswordPrincipal) {

        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableEnd = 2;

                if (editText.getCompoundDrawables()[drawableEnd] != null &&
                        event.getRawX() >= editText.getRight()
                                - editText.getCompoundDrawables()[drawableEnd].getBounds().width()) {

                    if (esPasswordPrincipal) {
                        visiblePassword = !visiblePassword;
                        cambiarVisibilidad(editText, visiblePassword);
                    } else {
                        visibleConfirmar = !visibleConfirmar;
                        cambiarVisibilidad(editText, visibleConfirmar);
                    }

                    v.performClick();
                    return true;
                }
            }

            return false;
        });
    }

    private void cambiarVisibilidad(EditText editText, boolean visible) {

        if (visible) {
            editText.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );
        } else {
            editText.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );
        }

        editText.setSelection(editText.getText().length());
    }
}