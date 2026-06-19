package com.example.proy_moviles;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class comentarios extends Fragment {

    LinearLayout layoutComentariosUsuario;
    FirebaseFirestore db;

    ArrayList<ItemComentario> comentariosNegocios = new ArrayList<>();
    ArrayList<ItemComentario> comentariosServicios = new ArrayList<>();

    private static class ItemComentario {
        Comentario comentario;
        String nombrePadre;
        String tipo;
        DocumentReference refComentario;

        ItemComentario(Comentario comentario, String nombrePadre, String tipo, DocumentReference refComentario) {
            this.comentario = comentario;
            this.nombrePadre = nombrePadre;
            this.tipo = tipo;
            this.refComentario = refComentario;
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View vista = inflater.inflate(
                R.layout.fragment_comentarios,
                container,
                false
        );

        layoutComentariosUsuario = vista.findViewById(R.id.layoutComentariosUsuario);
        db = FirebaseFirestore.getInstance();

        cargarComentarios();

        return vista;
    }

    private void cargarComentarios() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mostrarMensajeVacio();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        layoutComentariosUsuario.removeAllViews();
        comentariosNegocios.clear();
        comentariosServicios.clear();

        db.collection("negocios")
                .get()
                .addOnSuccessListener(negocios -> {

                    if (negocios.isEmpty()) {
                        mostrarMensajeVacio();
                        return;
                    }

                    final int[] pendientes = {negocios.size()};

                    for (DocumentSnapshot negocioDoc : negocios.getDocuments()) {

                        NegocioServicio negocioServicio =
                                negocioDoc.toObject(NegocioServicio.class);

                        if (negocioServicio == null) {
                            pendientes[0]--;
                            if (pendientes[0] == 0) {
                                pintarComentarios();
                            }
                            continue;
                        }

                        negocioDoc.getReference()
                                .collection("comentarios")
                                .whereEqualTo("uidUsuario", uid)
                                .get()
                                .addOnSuccessListener(comentarios -> {

                                    for (DocumentSnapshot comentarioDoc : comentarios.getDocuments()) {

                                        Comentario comentario =
                                                comentarioDoc.toObject(Comentario.class);

                                        if (comentario == null) {
                                            continue;
                                        }

                                        String tipo = negocioServicio.getTipo();

                                        if (tipo == null) {
                                            tipo = "negocio";
                                        }

                                        String nombrePadre;

                                        if (tipo.equalsIgnoreCase("servicio")) {
                                            nombrePadre =
                                                    textoSeguro(negocioServicio.getNombreDueno()) + " " +
                                                            textoSeguro(negocioServicio.getApellidosDueno());
                                        } else {
                                            nombrePadre = textoSeguro(negocioServicio.getRazonSocial());
                                        }

                                        ItemComentario item =
                                                new ItemComentario(
                                                        comentario,
                                                        nombrePadre.trim(),
                                                        tipo,
                                                        comentarioDoc.getReference()
                                                );

                                        if (tipo.equalsIgnoreCase("servicio")) {
                                            comentariosServicios.add(item);
                                        } else {
                                            comentariosNegocios.add(item);
                                        }
                                    }

                                    pendientes[0]--;

                                    if (pendientes[0] == 0) {
                                        pintarComentarios();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    pendientes[0]--;

                                    if (pendientes[0] == 0) {
                                        pintarComentarios();
                                    }
                                });
                    }
                });
    }

    private void pintarComentarios() {

        layoutComentariosUsuario.removeAllViews();

        if (comentariosNegocios.isEmpty() && comentariosServicios.isEmpty()) {
            mostrarMensajeVacio();
            return;
        }

        ordenarPorFecha(comentariosNegocios);
        ordenarPorFecha(comentariosServicios);

        agregarTituloSeccion("Negocios");

        if (comentariosNegocios.isEmpty()) {
            mostrarMensajeSeccion("Aún no se han comentado negocios");
        } else {
            for (ItemComentario item : comentariosNegocios) {
                crearCardComentario(item);
            }
        }

        agregarTituloSeccion("Servicios");

        if (comentariosServicios.isEmpty()) {
            mostrarMensajeSeccion("Aún no se han comentado servicios");
        } else {
            for (ItemComentario item : comentariosServicios) {
                crearCardComentario(item);
            }
        }
    }

    private void ordenarPorFecha(ArrayList<ItemComentario> lista) {
        Collections.sort(lista, (a, b) ->
                Long.compare(b.comentario.getFecha(), a.comentario.getFecha())
        );
    }

    private void agregarTituloSeccion(String titulo) {

        TextView txtTitulo = new TextView(requireContext());

        txtTitulo.setText(titulo);
        txtTitulo.setTextColor(Color.parseColor("#5E35B1"));
        txtTitulo.setTextSize(17);
        txtTitulo.setTypeface(null, Typeface.BOLD);

        txtTitulo.setPadding(
                10,
                6,
                10,
                6
        );

        txtTitulo.setBackgroundColor(
                Color.parseColor("#EDE7F6")
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                16,
                0,
                8
        );

        txtTitulo.setLayoutParams(params);

        layoutComentariosUsuario.addView(txtTitulo);
    }

    private void crearCardComentario(ItemComentario item) {

        TextView txtPadre = new TextView(requireContext());
        txtPadre.setText(item.nombrePadre);
        txtPadre.setTextColor(Color.parseColor("#2E7D32"));
        txtPadre.setTextSize(14);
        txtPadre.setTypeface(null, Typeface.BOLD);
        txtPadre.setPadding(10, 6, 10, 6);
        txtPadre.setBackgroundColor(Color.parseColor("#E8F5E9"));

        layoutComentariosUsuario.addView(txtPadre);

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(8, 8, 8, 8);
        card.setBackgroundColor(Color.WHITE);

        ImageView img = new ImageView(requireContext());
        LinearLayout.LayoutParams imgParams =
                new LinearLayout.LayoutParams(dp(38), dp(38));
        img.setLayoutParams(imgParams);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageResource(R.drawable.perfilusuario);

        if (item.comentario.getFotoUrl() != null &&
                !item.comentario.getFotoUrl().trim().isEmpty()) {

            Glide.with(requireContext())
                    .load(item.comentario.getFotoUrl())
                    .placeholder(R.drawable.perfilusuario)
                    .error(R.drawable.perfilusuario)
                    .into(img);
        }

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

        TextView txtNombre = new TextView(requireContext());
        txtNombre.setText(item.comentario.getNombreUsuario());
        txtNombre.setTextColor(Color.BLACK);
        txtNombre.setTypeface(null, Typeface.BOLD);
        txtNombre.setTextSize(14);

        TextView txtComentario = new TextView(requireContext());
        txtComentario.setText(item.comentario.getComentario());
        txtComentario.setTextColor(Color.BLACK);
        txtComentario.setTextSize(13);

        TextView txtFecha = new TextView(requireContext());
        txtFecha.setText(formatearFecha(item.comentario.getFecha()));
        txtFecha.setTextColor(Color.GRAY);
        txtFecha.setTextSize(11);

        TextView txtLike = new TextView(requireContext());
        txtLike.setMinHeight(dp(24));
        txtLike.setText("👍 Me gusta: " + item.comentario.getLikes()+" ");
        txtLike.setTextSize(11);
        txtLike.setTextColor(Color.BLACK);
        txtLike.setGravity(Gravity.CENTER);
        txtLike.setPadding(12, 4, 12, 4);

        LinearLayout.LayoutParams paramsLike =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        paramsLike.setMargins(0, 5, 0, 0);
        txtLike.setLayoutParams(paramsLike);

        android.graphics.drawable.GradientDrawable fondoLike =
                new android.graphics.drawable.GradientDrawable();

        fondoLike.setColor(Color.parseColor("#E0E0E0"));
        fondoLike.setStroke(2, Color.parseColor("#9E9E9E"));
        fondoLike.setCornerRadius(4);

        txtLike.setBackground(fondoLike);

        TextView btnEliminar = new TextView(requireContext());
        btnEliminar.setMinHeight(dp(24));
        btnEliminar.setText("🗑 Eliminar ");
        btnEliminar.setTextSize(11);
        btnEliminar.setTextColor(Color.WHITE);
        btnEliminar.setGravity(Gravity.CENTER);
        btnEliminar.setPadding(12, 4, 12, 4);

        android.graphics.drawable.GradientDrawable fondoEliminar =
                new android.graphics.drawable.GradientDrawable();

        fondoEliminar.setColor(Color.parseColor("#D32F2F"));
        fondoEliminar.setCornerRadius(4);

        btnEliminar.setBackground(fondoEliminar);

        LinearLayout filaBotones =
                new LinearLayout(requireContext());

        filaBotones.setOrientation(
                LinearLayout.HORIZONTAL
        );
        filaBotones.setGravity(Gravity.CENTER_VERTICAL);

        filaBotones.addView(txtLike);

        if (FirebaseAuth.getInstance().getCurrentUser() != null
                && FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid()
                .equals(item.comentario.getUidUsuario())) {

            LinearLayout.LayoutParams paramsEliminar =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            paramsEliminar.setMargins(8, 5, 0, 0);

            btnEliminar.setLayoutParams(paramsEliminar);

            filaBotones.addView(btnEliminar);

            btnEliminar.setOnClickListener(v -> {

                item.refComentario.delete()
                        .addOnSuccessListener(unused -> {

                            comentariosNegocios.remove(item);
                            comentariosServicios.remove(item);

                            Toast.makeText(
                                    requireContext(),
                                    "Comentario eliminado",
                                    Toast.LENGTH_SHORT
                            ).show();

                            pintarComentarios();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(
                                    requireContext(),
                                    "Error al eliminar",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
            });
        }

        contenido.addView(txtNombre);
        contenido.addView(txtComentario);
        contenido.addView(txtFecha);
        contenido.addView(filaBotones);
        card.addView(img);
        card.addView(contenido);

        layoutComentariosUsuario.addView(card);

        View separador = new View(requireContext());

        LinearLayout.LayoutParams paramsLinea =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                );

        paramsLinea.setMargins(
                0,
                12,
                0,
                12
        );

        separador.setLayoutParams(paramsLinea);

        separador.setBackgroundColor(
                Color.parseColor("#D6D6D6")
        );

        layoutComentariosUsuario.addView(separador);
    }

    private void mostrarMensajeVacio() {

        layoutComentariosUsuario.removeAllViews();

        TextView txt = new TextView(requireContext());
        txt.setText("Aún no se ha comentado ningún negocio o servicio");
        txt.setGravity(Gravity.CENTER);
        txt.setTextSize(16);
        txt.setTextColor(Color.GRAY);
        txt.setPadding(0, 20, 0, 0);

        layoutComentariosUsuario.addView(txt);
    }

    private String formatearFecha(long fecha) {
        return new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date(fecha));
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
    private void mostrarMensajeSeccion(String mensaje) {

        TextView txt = new TextView(requireContext());
        txt.setText(mensaje);
        txt.setTextColor(Color.GRAY);
        txt.setTextSize(14);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(0, 12, 0, 12);

        layoutComentariosUsuario.addView(txt);
    }
}