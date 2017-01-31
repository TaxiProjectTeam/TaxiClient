package com.example.sveta.taxo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ORDERS_CHILD = "orders";
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 13;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LatLng marker;
    private MarkerOptions markerOptions;
    private boolean mapReady = false;

    private EditText startingAddress;
    private EditText destinationAddress;
    private TextView addingAddress;
    private EditText addedAddress;
    private ImageView targetLocation;
    private Button buttonOrder;

    private Marker olderStartingMarker;
    private Marker olderDestinationMarker;
    private Marker olderAddingMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        startingAddress = (EditText) findViewById(R.id.starting_address);
        destinationAddress = (EditText) findViewById(R.id.destination_address);

        /*
            Реалізація функціоналу додавання проміжної адреси маршруту
         */
        addingAddress = (TextView) findViewById(R.id.adding_address);
        addingAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Додавання рядка вводу адреси
                final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.addresses_pool);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.adding_address_item, null);

                final RelativeLayout addedAddressLine = (RelativeLayout) view.findViewById(R.id.added_address_line);
                addedAddress = (EditText) view.findViewById(R.id.added_address);
                linearLayout.addView(addedAddressLine, linearLayout.getChildCount() - 2);

                // Видалення доданого рядка вводу адреси
                ImageView deleteIcon = (ImageView) view.findViewById(R.id.delete_icon);
                deleteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linearLayout.removeView(addedAddressLine);
                    }
                });
            }
        });

        /*
            Реалізація функціоналу визначення геолокації:
            якщо мапу завантажено, ставиться маркер на місці перебування пристрою
            і заноситься до рядкя вводу адреси
         */
        targetLocation = (ImageView) findViewById(R.id.get_my_location);
        targetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady) {
                    marker = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    Marker olderMarker = googleMap.addMarker(markerOptions.position(marker).title("Ви знаходитесь тут"));

                    CameraPosition camera = CameraPosition.builder().target(marker).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

                    LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    getAddress(latLng, startingAddress, olderStartingMarker);
                    olderStartingMarker = olderMarker;
                }
            }
        });

        buttonOrder = (Button) findViewById(R.id.order);
        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DetailOrderActivity.class);
                startActivity(intent);
            }
        });

        /*
            Завантаження мапи
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mapReady = true;
        this.googleMap = googleMap;
        markerOptions = new MarkerOptions();
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker olderMarker = googleMap.addMarker(markerOptions.position(latLng));
                if (startingAddress.isFocused()) {
                    getAddress(latLng, startingAddress, olderStartingMarker);
                    olderStartingMarker = olderMarker;
                }
                else if (destinationAddress.isFocused()) {
                    getAddress(latLng, destinationAddress, olderDestinationMarker);
                    olderDestinationMarker = olderMarker;
                }
                else if (addedAddress.isFocused()) {
                    getAddress(latLng, addedAddress, olderAddingMarker);
                    olderAddingMarker = olderMarker;
                }
            }
        });

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Парсинг геолокації в адресу
    private void getAddress(LatLng latLng, EditText editText, Marker olderMarker) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {}
        if (olderMarker != null)
            olderMarker.remove();
        String city = addresses.get(0).getAddressLine(1);
        String street = addresses.get(0).getAddressLine(0);
        editText.setText(city + ", " + street);
    }
}
