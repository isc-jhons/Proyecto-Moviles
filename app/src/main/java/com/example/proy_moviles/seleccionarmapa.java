package com.example.proy_moviles;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

public class seleccionarmapa extends Fragment implements OnMapReadyCallback {

    GoogleMap mMap;
    Button btnConfirmarUbicacion;
    LatLng ubicacionSeleccionada;
    FusedLocationProviderClient fusedLocationClient;

    public seleccionarmapa() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_seleccionarmapa, container, false);

        btnConfirmarUbicacion = vista.findViewById(R.id.btnConfirmarUbicacion);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.mapSeleccion);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmarUbicacion.setOnClickListener(v -> confirmarUbicacion());

        return vista;
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

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        activarUbicacion();

        mMap.setOnMapClickListener(latLng -> {
            ubicacionSeleccionada = latLng;

            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Ubicación del negocio"));
        });
    }

    private void activarUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    200
            );
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng miUbicacion = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        ubicacionSeleccionada = miUbicacion;

                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(miUbicacion, 17)
                        );

                        mMap.addMarker(new MarkerOptions()
                                .position(miUbicacion)
                                .title("Ubicación del negocio"));
                    }
                });
    }

    private void confirmarUbicacion() {
        if (ubicacionSeleccionada == null) {
            Toast.makeText(requireContext(), "Seleccione una ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        String origen = "negocio";

        if (getArguments() != null) {
            origen = getArguments().getString("origen", "negocio");
        }

        Bundle bundle = new Bundle();
        bundle.putDouble("latitud", ubicacionSeleccionada.latitude);
        bundle.putDouble("longitud", ubicacionSeleccionada.longitude);
        bundle.putString("origen", origen);

        requireActivity()
                .getSupportFragmentManager()
                .setFragmentResult("ubicacionNegocio", bundle);

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}