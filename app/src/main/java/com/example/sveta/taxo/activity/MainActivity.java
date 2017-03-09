package com.example.sveta.taxo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.example.sveta.taxo.ApiInterface;
import com.example.sveta.taxo.R;
import com.example.sveta.taxo.RouteApiClient;
import com.example.sveta.taxo.adapter.AddressArrayAdapter;
import com.example.sveta.taxo.adapter.AddressLineAdapter;
import com.example.sveta.taxo.adapter.OnFocusItemListener;
import com.example.sveta.taxo.model.ModelAddressLine;
import com.example.sveta.taxo.model.Order;
import com.example.sveta.taxo.model.RouteResponse;
import com.example.sveta.taxo.utility.SwipeHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String ORDER_CHILD = "orders";
    private static final String PRICE_PER_KILOMETRES_KEY = "price_per_kilometres";
    private static final String STARTING_PRICE_KEY = "starting_price";
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 13;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LatLng geoPosition;
    private MarkerOptions markerOptions;
    private boolean mapReady = false;

    private ApiInterface routeApiInterface;
    private Call<RouteResponse> routeModelCall;
    private PolylineOptions polylineOptions;

    private DatabaseReference databaseReference;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    private ImageView targetLocation;
    private Button buttonOrder;
    private Button buttonPlus;
    private TextView total;
    private EditText additionalComment;

    private AutocompleteFilter autocompleteFilter;
    private static final LatLngBounds CHERCASSY = new LatLngBounds(
            new LatLng(49.364583, 31.9578749), new LatLng(49.49797, 32.140585));
    private AddressArrayAdapter addressArrayAdapter;
    private ArrayList<ModelAddressLine> modelAddressLines;
    private AddressLineAdapter.EditTypeViewHolder viewHolder;
    public static HashMap<AddressLineAdapter.EditTypeViewHolder, Marker> markers = new HashMap<>();
    private HashMap<String, Double> startPosition = new HashMap<>();
    private HashMap<String, HashMap<String, Double>> destinationPositions = new HashMap<>();
    private List<LatLng> routePoints = new ArrayList<>();
    private int totalPrice;

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
                    .build();
        }

        routeApiInterface = RouteApiClient.getClient().create(ApiInterface.class);
        polylineOptions = new PolylineOptions();

        total = (TextView) findViewById(R.id.total);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.default_remote_parametrs);
        firebaseRemoteConfig.fetch(1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    firebaseRemoteConfig.activateFetched();
                displayTotalPrice();
            }
        });

        modelAddressLines = new ArrayList<>();
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.EDIT_TYPE, getString(R.string.start_address)));
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.EDIT_TYPE, getString(R.string.destination_address)));
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.TEXT_TYPE, getString(R.string.add_address)));

        final AddressLineAdapter adapter = new AddressLineAdapter(modelAddressLines, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_address);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        addressArrayAdapter = new AddressArrayAdapter(this, android.R.layout.simple_list_item_1, CHERCASSY, autocompleteFilter);

        adapter.setOnFocusItemListener(new OnFocusItemListener() {
            @Override
            public void onItemFocus(int position) {
                viewHolder = (AddressLineAdapter.EditTypeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                viewHolder.editText.setThreshold(3);
                viewHolder.editText.setAdapter(addressArrayAdapter);
                // TODO: fixed this
//                LatLng latLng = new LatLng(getLocationFromAddress(viewHolder.editText.getText().toString()).getLatitude(), getLocationFromAddress(viewHolder.editText.getText().toString()).getLongitude());
//                addAddressesToHashMap(latLng);
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
                    Marker marker = googleMap.addMarker(markerOptions.position(geoPosition).title(getString(R.string.your_location)));

                    CameraPosition camera = CameraPosition.builder().target(geoPosition).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

                    if (viewHolder != null && viewHolder.getAdapterPosition() == 0) {
                        getAddressFromLocation(geoPosition);
                        addAddressesToHashMap(geoPosition);
                        deleteOldMarker(marker);
                        drawRoute();
                    }
                }
            }
        });

        additionalComment = (EditText) findViewById(R.id.additionalComment);
        additionalComment.setText("");

        buttonPlus = (Button) findViewById(R.id.button_plus);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalPrice += 5;
                String result = totalPrice + "";
                total.setText(result);
            }
        });

        buttonOrder = (Button) findViewById(R.id.order);
        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isEmpty = false;
                for (int i = 0; i < adapter.getItemCount() - 1; i++) {
                    AddressLineAdapter.EditTypeViewHolder holder = (AddressLineAdapter.EditTypeViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    if (holder.editText == null || (holder.editText.getText().toString()).equals(""))
                        isEmpty = true;
                }
                if (!isEmpty) {
                    Order order = new Order(
                            startPosition,
                            destinationPositions,
                            Integer.parseInt(total.getText().toString()),
                            additionalComment.getText().toString());
                    databaseReference = databaseReference.child(ORDER_CHILD).push();
                    String orderKey = databaseReference.getKey();
                    databaseReference.setValue(order);
                    Intent intent = new Intent(getApplicationContext(), DetailOrderActivity.class);
                    intent.putExtra("orderKey", orderKey);
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, "Ви ввели не всі поля з адресами", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void drawRoute() {
        polylineOptions.width(4);
        polylineOptions.color(Color.BLUE);
        getRoute();
        for(LatLng point : routePoints){
            polylineOptions.add(point);
        }
        googleMap.addPolyline(polylineOptions);
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
                getAddressFromLocation(latLng);
                addAddressesToHashMap(latLng);
                deleteOldMarker(marker);
                drawRoute();
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        addressArrayAdapter.setGoogleApiClient(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        addressArrayAdapter.setGoogleApiClient(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {}
        String address = addresses.get(0).getAddressLine(0);
        viewHolder.editText.setText(address);
    }

//    private Address getLocationFromAddress(String address) {
//        Geocoder geocoder = new Geocoder(MainActivity.this);
//        try {
//            List<Address> location = geocoder.getFromLocationName(address, 5);
//            return location.get(0);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private void addAddressesToHashMap(LatLng latLng) {
        if (viewHolder.getAdapterPosition() == 0) {
            startPosition.put(getString(R.string.latitude), latLng.latitude);
            startPosition.put(getString(R.string.longitude), latLng.longitude);
        } else {
            HashMap<String, Double> destinationPositionCoords = new HashMap<>();
            destinationPositionCoords.put(getString(R.string.latitude), latLng.latitude);
            destinationPositionCoords.put(getString(R.string.longitude), latLng.longitude);
            destinationPositions.put(viewHolder.getAdapterPosition() - 1 + "", destinationPositionCoords);
        }
    }

    public void deleteOldMarker(Marker marker) {
        if (markers.get(viewHolder) == null)
            markers.put(viewHolder, marker);
        else {
            markers.get(viewHolder).remove();
            markers.put(viewHolder, marker);
        }
    }

    private void displayTotalPrice() {
        String pricePerKilometres = firebaseRemoteConfig.getString(PRICE_PER_KILOMETRES_KEY);
        String startingPrice = firebaseRemoteConfig.getString(STARTING_PRICE_KEY);
        totalPrice = Integer.parseInt(pricePerKilometres) + Integer.parseInt(startingPrice);
        String result = totalPrice + "";
        total.setText(result);
    }

    private void getRoute() {
        if (!startPosition.isEmpty() || !destinationPositions.isEmpty()) {
            Double startLat = startPosition.get(getString(R.string.latitude));
            Double startLng = startPosition.get(getString(R.string.longitude));

            Double destinationLat = destinationPositions.get("0").get(getString(R.string.latitude));
            Double destinationLng = destinationPositions.get("0").get(getString(R.string.longitude));

            routeModelCall = routeApiInterface.getRoute(startLat + "," + startLng,
                    destinationLat + "," + destinationLng);

            if (routeModelCall != null) {
                routeModelCall.enqueue(new Callback<RouteResponse>() {
                    @Override
                    public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                        RouteResponse routeResponse = response.body();
                        routePoints = PolyUtil.decode(routeResponse.getPoints());
                    }

                    @Override
                    public void onFailure(Call<RouteResponse> call, Throwable t) {

                    }
                });

            }
        }
    }
}
