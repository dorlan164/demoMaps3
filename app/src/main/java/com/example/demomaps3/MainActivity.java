package com.example.demomaps3;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ListView lvClientes;
    private ClienteAdapter adapter;
    private int selectedPos = 0;  // Cliente 1 seleccionado al inicio

    // Coordenadas (3 clientes × 2 puntos cada uno)
    private final LatLng[][] coords = {
            { new LatLng(19.432608, -99.133209), new LatLng(20.659698, -103.349609) },
            { new LatLng(21.161907, -86.851528),  new LatLng(25.686614, -100.316113) },
            { new LatLng(19.041297, -98.206200),  new LatLng(20.967370, -89.592586) }
    };

    // Direcciones de texto (una por cliente)
    private final String[] direccionesTexto = {
            "Av. Insurgentes 1602, Ciudad de México",
            "Calle 8 123, Playa del Carmen, QR",
            "Calle 14 56, Mérida, Yucatán"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvClientes = findViewById(R.id.listViewClientes);
        // Habilita el modo single choice y hace selector transparente
        lvClientes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvClientes.setSelector(android.R.color.transparent);

        // Prepara los textos para cada ítem (bullet + 3 líneas)
        String bullet = "\u2022 ";
        List<String> listaItems = new ArrayList<>();
        for (int i = 0; i < coords.length; i++) {
            String item = "Cliente " + (i + 1) + ":\n"
                    + bullet + coords[i][0].latitude + ", " + coords[i][0].longitude + "\n"
                    + bullet + coords[i][1].latitude + ", " + coords[i][1].longitude + "\n"
                    + bullet + direccionesTexto[i];
            listaItems.add(item);
        }

        // Crea el adaptador personalizado y lo asigna
        adapter = new ClienteAdapter(listaItems);
        lvClientes.setAdapter(adapter);

        // Marca el primer ítem como seleccionado (gris)
        lvClientes.setItemChecked(selectedPos, true);

        // Listener para cambio de selección
        lvClientes.setOnItemClickListener((parent, view, pos, id) -> {
            selectedPos = pos;
            lvClientes.setItemChecked(pos, true);
            adapter.notifyDataSetChanged();
            if (map != null) {
                mostrarPines(pos);
            }
        });

        // Inicializa el SupportMapFragment
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Clic en marcador → muestra diálogo con opciones
        map.setOnMarkerClickListener(marker -> {
            final LatLng pos = marker.getPosition();
            CharSequence[] opciones = {"Ir a ruta", "Street View"};
            new AlertDialog.Builder(this)
                    .setTitle(marker.getTitle())
                    .setItems(opciones, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                Uri uriNav = Uri.parse(
                                        "google.navigation:q=" + pos.latitude + "," + pos.longitude
                                );
                                Intent nav = new Intent(Intent.ACTION_VIEW, uriNav);
                                nav.setPackage("com.google.android.apps.maps");
                                if (nav.resolveActivity(getPackageManager()) != null) {
                                    startActivity(nav);
                                }
                                break;
                            case 1:
                                Uri uriStreet = Uri.parse(
                                        "google.streetview:cbll=" + pos.latitude + "," + pos.longitude
                                );
                                Intent street = new Intent(Intent.ACTION_VIEW, uriStreet);
                                street.setPackage("com.google.android.apps.maps");
                                if (street.resolveActivity(getPackageManager()) != null) {
                                    startActivity(street);
                                }
                                break;
                        }
                    })
                    .show();
            return true;
        });

        // Zoom inicial
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(20.0, -100.0), 4f));

        // Dibuja los pines del Cliente 1 al iniciar
        mostrarPines(selectedPos);
    }

    /** Dibuja los 3 pines de un cliente y ajusta la cámara. */
    private void mostrarPines(int idx) {
        map.clear();
        LatLng p1 = coords[idx][0];
        LatLng p2 = coords[idx][1];

        map.addMarker(new MarkerOptions()
                .position(p1)
                .title("Dirección 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        map.addMarker(new MarkerOptions()
                .position(p2)
                .title("Dirección 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Zoom preliminar
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(p1, 8f));

        // Tercer pin (geocoded) en hilo background
        new Thread(() -> {
            try {
                List<Address> res = new Geocoder(this)
                        .getFromLocationName(direccionesTexto[idx], 1);
                if (res != null && !res.isEmpty()) {
                    LatLng p3 = new LatLng(
                            res.get(0).getLatitude(),
                            res.get(0).getLongitude()
                    );
                    runOnUiThread(() -> {
                        map.addMarker(new MarkerOptions()
                                .position(p3)
                                .title("Dirección 3")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        // Ajusta bounds para mostrar los tres
                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(p1)
                                .include(p2)
                                .include(p3)
                                .build();
                        map.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100)
                        );
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** Adaptador que pinta de gris la fila seleccionada. */
    private class ClienteAdapter extends ArrayAdapter<String> {
        ClienteAdapter(List<String> items) {
            super(MainActivity.this, R.layout.list_item_cliente, R.id.textoCliente, items);
        }

        @NonNull @Override
        public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
            View v = super.getView(pos, convertView, parent);
            // El root tiene selector_fila como fondo y `state_activated`
            v.findViewById(R.id.root).setActivated(pos == selectedPos);
            return v;
        }
    }
}
