package com.example.sveta.taxo.activity;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.example.sveta.taxo.adapter.AddressLineAdapter;
import com.example.sveta.taxo.adapter.OnFocusItemListener;
import com.example.sveta.taxo.R;
import com.example.sveta.taxo.utility.SwipeHelper;
import com.example.sveta.taxo.model.ModelAddressLine;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ORDERS_CHILD = "orders";
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 13;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LatLng geoPosition;
    private MarkerOptions markerOptions;
    private boolean mapReady = false;

    private ImageView targetLocation;
    private Button buttonOrder;
    private ArrayList<ModelAddressLine> modelAddressLines;
    private AddressLineAdapter.EditTypeViewHolder viewHolder;
    public static HashMap<AddressLineAdapter.EditTypeViewHolder, Marker> markers;

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
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        markers = new HashMap<>();

        modelAddressLines = new ArrayList<>();
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.EDIT_TYPE, "Звідки"));
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.EDIT_TYPE, "Куди"));
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.TEXT_TYPE, "Додати точку маршруту"));

        final AddressLineAdapter adapter = new AddressLineAdapter(modelAddressLines);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_address);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnFocusItemListener(new OnFocusItemListener() {
            @Override
            public void onItemFocus(int position) {
                viewHolder = (AddressLineAdapter.EditTypeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            }
        });

        ItemTouchHelper.Callback callback = new SwipeHelper(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        targetLocation = (ImageView) findViewById(R.id.get_my_location);
        targetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady) {
                    geoPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    Marker marker = googleMap.addMarker(markerOptions.position(geoPosition).title("Ви знаходитесь тут"));

                    CameraPosition camera = CameraPosition.builder().target(geoPosition).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

                    if (viewHolder.getAdapterPosition() == 0) {
                        getAddress(geoPosition);
                        deleteOldMarker(marker);
                    }
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

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = googleMap.addMarker(markerOptions.position(latLng));
                getAddress(latLng);
                deleteOldMarker(marker);
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

    private void getAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {}
        String city = addresses.get(0).getAddressLine(1);
        String street = addresses.get(0).getAddressLine(0);
        viewHolder.editText.setText(city + ", " + street);
    }

    public void deleteOldMarker(Marker marker) {
        if (markers.get(viewHolder) == null)
            markers.put(viewHolder, marker);
        else {
            markers.get(viewHolder).remove();
            markers.put(viewHolder, marker);
        }
    }
}
