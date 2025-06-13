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
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private ListView lvClientes;
    private ClienteAdapter adapter;
    private int selectedPos = 0;

    // Coordenadas de ejemplo
    private final LatLng[][] coords = {
            { new LatLng(19.432608, -99.133209), new LatLng(20.659698, -103.349609) },
            { new LatLng(21.161907, -86.851528),  new LatLng(25.686614, -100.316113) },
            { new LatLng(19.041297, -98.206200),  new LatLng(20.967370, -89.592586) }
    };
    private final String[] direccionesTexto = {
            "Av. Insurgentes 1602, Ciudad de México",
            "Calle 8 123, Playa del Carmen, QR",
            "Calle 14 56, Mérida, Yucatán"
    };
    // Imágenes de ejemplo para el carrusel
    private final int[] carouselImages = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configuro la lista de clientes
        lvClientes = findViewById(R.id.listViewClientes);
        lvClientes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvClientes.setSelector(android.R.color.transparent);

        List<String> listaItems = new ArrayList<>();
        String bullet = "\u2022 ";
        for (int i = 0; i < coords.length; i++) {
            listaItems.add(
                    "Cliente " + (i+1) + ":\n"
                            + bullet + coords[i][0].latitude + ", " + coords[i][0].longitude + "\n"
                            + bullet + coords[i][1].latitude + ", " + coords[i][1].longitude + "\n"
                            + bullet + direccionesTexto[i]
            );
        }
        adapter = new ClienteAdapter(listaItems);
        lvClientes.setAdapter(adapter);
        lvClientes.setItemChecked(selectedPos, true);

        // Listener de selección de cliente
        lvClientes.setOnItemClickListener((parent, view, pos, id) -> {
            selectedPos = pos;
            lvClientes.setItemChecked(pos, true);
            adapter.notifyDataSetChanged();
            if (map != null) mostrarPines(pos);
        });

        // Inicializo el mapa
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(marker -> {
            LatLng pos = marker.getPosition();
            CharSequence[] opts = {"Ir a ruta","Street View"};
            new AlertDialog.Builder(this)
                    .setTitle(marker.getTitle())
                    .setItems(opts, (dialog, which) -> {
                        Intent i;
                        if (which == 0) {
                            i = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q="+pos.latitude+","+pos.longitude));
                        } else {
                            i = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("google.streetview:cbll="+pos.latitude+","+pos.longitude));
                        }
                        i.setPackage("com.google.android.apps.maps");
                        if (i.resolveActivity(getPackageManager()) != null)
                            startActivity(i);
                    }).show();
            return true;
        });

        // Posición inicial
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(20.0,-100.0), 4f));
        mostrarPines(selectedPos);
    }

    // Muestra los tres pines (2 coordenadas + geocoding)
    private void mostrarPines(int idx) {
        map.clear();
        LatLng p1 = coords[idx][0], p2 = coords[idx][1];
        map.addMarker(new MarkerOptions().position(p1)
                .title("Dirección 1")
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED)));
        map.addMarker(new MarkerOptions().position(p2)
                .title("Dirección 2")
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(p1, 8f));

        // Geocoding en background
        new Thread(() -> {
            try {
                List<Address> res = new Geocoder(this)
                        .getFromLocationName(direccionesTexto[idx], 1);
                if (!res.isEmpty()) {
                    LatLng p3 = new LatLng(
                            res.get(0).getLatitude(),
                            res.get(0).getLongitude()
                    );
                    runOnUiThread(() -> {
                        map.addMarker(new MarkerOptions().position(p3)
                                .title("Dirección 3")
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_BLUE)));
                        LatLngBounds b = new LatLngBounds.Builder()
                                .include(p1).include(p2).include(p3).build();
                        map.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(b, 100));
                    });
                }
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    // Adapter personalizado con botón para ver imágenes
    private class ClienteAdapter extends ArrayAdapter<String> {
        ClienteAdapter(List<String> items) {
            super(MainActivity.this,
                    R.layout.list_item_cliente,
                    R.id.textoCliente, items);
        }
        @NonNull @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            View v = super.getView(pos, cv, parent);
            v.findViewById(R.id.root)
                    .setActivated(pos == selectedPos);

            ImageView btn = v.findViewById(R.id.btn_images);
            btn.setOnClickListener(x -> {
                // Lanzar fragment fullscreen con carousel
                FullscreenCarouselFragment f =
                        FullscreenCarouselFragment.newInstance(
                                carouselImages, pos);
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.fragmentContainer, f)
                        .addToBackStack(null)
                        .commit();
            });
            return v;
        }
    }
}
