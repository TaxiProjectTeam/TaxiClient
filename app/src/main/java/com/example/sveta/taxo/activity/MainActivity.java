package com.example.sveta.taxo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.example.sveta.taxo.R;
import com.example.sveta.taxo.adapter.AddressArrayAdapter;
import com.example.sveta.taxo.adapter.AddressLineAdapter;
import com.example.sveta.taxo.adapter.OnFocusItemListener;
import com.example.sveta.taxo.api.ApiInterface;
import com.example.sveta.taxo.api.RouteApiClient;
import com.example.sveta.taxo.model.ModelAddressLine;
import com.example.sveta.taxo.model.Order;
import com.example.sveta.taxo.model.RouteResponse;
import com.example.sveta.taxo.utility.AddressesConverter;
import com.example.sveta.taxo.utility.SwipeHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private AddressLineAdapter adapter;
    private ArrayList<ModelAddressLine> modelAddressLines;
    private AddressLineAdapter.EditTypeViewHolder viewHolder;
    public HashMap<AddressLineAdapter.EditTypeViewHolder, Marker> markers = new HashMap<>();
    public HashMap<AddressLineAdapter.EditTypeViewHolder, Polyline> routes = new HashMap<>();
    private HashMap<String, Double> startPosition = new HashMap<>();
    public ArrayList<HashMap<String, Double>> destinationPositions = new ArrayList<>();

    private List<LatLng> routePoints = new ArrayList<>();
    private int totalPrice;
    private boolean mapClick = false;
    private int distance;
    private int duration;
    private int currentDistance;
    private int currentDuration;
    private boolean isTarget;

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

        autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        addressArrayAdapter = new AddressArrayAdapter(this, android.R.layout.simple_list_item_1, CHERCASSY, autocompleteFilter);

        modelAddressLines = new ArrayList<>();
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.EDIT_TYPE, getString(R.string.start_address)));
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.EDIT_TYPE, getString(R.string.destination_address)));
        modelAddressLines.add(new ModelAddressLine(ModelAddressLine.TEXT_TYPE, getString(R.string.add_address)));

        adapter = new AddressLineAdapter(modelAddressLines, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_address);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnFocusItemListener(new OnFocusItemListener() {
            @Override
            public void onItemFocus(int position) {
                viewHolder = (AddressLineAdapter.EditTypeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                viewHolder.editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String addressString = "Черкаси, Черкаська область, Україна, " + viewHolder.editText.getText().toString();
                        if (validateAddress(addressString) && !mapClick && !isTarget) {
                            LatLng latLng = AddressesConverter.getLocationFromAddress(getApplicationContext(), addressString);
                            Marker marker;
                            if (viewHolder.getAdapterPosition() == 0)
                                marker = addStartMarker(latLng);
                            else
                                marker = addEndMarker(latLng);
                            addAddressesToHashMap(latLng);
                            //deleteOldMarker(marker);
                            getRoute();
                        }
                    }
                });
                isTarget = false;
                viewHolder.editText.setAdapter(addressArrayAdapter);
            }
        });

        ItemTouchHelper.Callback callback = new SwipeHelper(adapter, this);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        targetLocation = (ImageView) findViewById(R.id.get_my_location);
        targetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTarget = true;
                if (mapReady) {
                    geoPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    Marker marker = addStartMarker(geoPosition);

                    CameraPosition camera = CameraPosition.builder().target(geoPosition).zoom(17).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));

                    if (viewHolder != null && viewHolder.getAdapterPosition() == 0) {
                        viewHolder.editText.setText(AddressesConverter.getAddressFromLocation(getApplicationContext(), geoPosition));
                        addAddressesToHashMap(geoPosition);
                        deleteOldMarker(marker);
                        getRoute();
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
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        polylineOptions = new PolylineOptions();
        polylineOptions.width(8f);
        polylineOptions.color(Color.BLUE);
        for(LatLng point : routePoints){
            polylineOptions.add(point);
            latLngBuilder.include(point);
        }
        Polyline polyline = googleMap.addPolyline(polylineOptions);
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        googleMap.moveCamera(track);
        distance += currentDistance;
        duration += currentDuration;
        deleteOldDrawingRoute(polyline);
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
                Marker marker;
                if (viewHolder.getAdapterPosition() == 0)
                    marker = addStartMarker(latLng);
                else
                    marker = addEndMarker(latLng);
                viewHolder.editText.setText(AddressesConverter.getAddressFromLocation(getApplicationContext(), latLng));
                addAddressesToHashMap(latLng);
                deleteOldMarker(marker);
                getRoute();
                mapClick = true;
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
        if (!connectionResult.isSuccess())
            Toast.makeText(this, "Відсутнє з'єднання з інтернетом!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void addAddressesToHashMap(LatLng latLng) {
        if (viewHolder.getAdapterPosition() == 0) {
            startPosition.put(getString(R.string.latitude), latLng.latitude);
            startPosition.put(getString(R.string.longitude), latLng.longitude);
        } else {
            if (adapter.getItemCount() == 3)
                destinationPositions.clear();
            HashMap<String, Double> destinationPositionCoords = new HashMap<>();
            destinationPositionCoords.put(getString(R.string.latitude), latLng.latitude);
            destinationPositionCoords.put(getString(R.string.longitude), latLng.longitude);
            destinationPositions.add(destinationPositionCoords);
        }
    }

    public void deleteOldMarker(Marker marker) {
        if (markers.get(viewHolder) == null)
            markers.put(viewHolder, marker);
        else {
            markers.get(viewHolder).remove();
            distance -= currentDistance;
            duration -= currentDuration;
            markers.put(viewHolder, marker);
        }
    }

    public void deleteOldDrawingRoute(Polyline polyline) {
        if (routes.get(viewHolder) == null)
            routes.put(viewHolder, polyline);
        else {
            routes.get(viewHolder).remove();
            routes.put(viewHolder, polyline);
        }
    }

    private void displayTotalPrice() {
        String pricePerKilometres = firebaseRemoteConfig.getString(PRICE_PER_KILOMETRES_KEY);
        String startingPrice = firebaseRemoteConfig.getString(STARTING_PRICE_KEY);
        totalPrice = Integer.parseInt(pricePerKilometres) * distance / 1000 + Integer.parseInt(startingPrice);
        String result = totalPrice + "";
        total.setText(result);
    }

    public void getRoute() {
        Double startLat = null;
        Double startLng = null;
        Double destinationLat = null;
        Double destinationLng = null;

        if (startPosition.size() != 0 && destinationPositions.size() != 0 && destinationPositions.size() < 2) {
            startLat = startPosition.get(getString(R.string.latitude));
            startLng = startPosition.get(getString(R.string.longitude));
            destinationLat = destinationPositions.get(0).get(getString(R.string.latitude));
            destinationLng = destinationPositions.get(0).get(getString(R.string.longitude));
            if (routes.size() > 0)
                for (Map.Entry<AddressLineAdapter.EditTypeViewHolder, Polyline> pair : routes.entrySet())
                    pair.getValue().remove();
        }
        else if (destinationPositions.size() > 1) {
            startLat = destinationPositions.get(destinationPositions.size() - 2).get(getString(R.string.latitude));
            startLng = destinationPositions.get(destinationPositions.size() - 2).get(getString(R.string.longitude));
            destinationLat = destinationPositions.get(destinationPositions.size() - 1).get(getString(R.string.latitude));
            destinationLng = destinationPositions.get(destinationPositions.size() - 1).get(getString(R.string.longitude));
        }

        if (startLat != null && startLng != null && destinationLat != null && destinationLng != null) {
            routeModelCall = routeApiInterface.getRoute(startLat + "," + startLng,
                    destinationLat + "," + destinationLng);

            if (routeModelCall != null) {
                routeModelCall.enqueue(new Callback<RouteResponse>() {
                    @Override
                    public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                        RouteResponse routeResponse = response.body();
                        routePoints = PolyUtil.decode(routeResponse.getPoints());
                        currentDistance = routeResponse.getDistance();
                        currentDuration = routeResponse.getDuration();
                        drawRoute();
                        displayTotalPrice();
                    }

                    @Override
                    public void onFailure(Call<RouteResponse> call, Throwable t) {

                    }
                });
            }
        }
    }

    private boolean validateAddress (String address) {
        String[] addressArray = address.split(" ");
        String pattern = "(\\d+)(([\\\\]\\d+)?)";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(addressArray[addressArray.length - 1]);
        return matcher.matches();
    }

    private Marker addStartMarker(LatLng position) {
        IconGenerator iconFactory = new IconGenerator(MainActivity.this);
        iconFactory.setTextAppearance(R.style.markers_text_style);
        iconFactory.setColor(ContextCompat.getColor(MainActivity.this, R.color.markers_green_background));
        return googleMap.addMarker(markerOptions.position(position)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(iconFactory
                                .makeIcon(getResources().getString(R.string.markers_start_label)))));
    }

    private Marker addEndMarker(LatLng position) {
        IconGenerator iconFactory = new IconGenerator(MainActivity.this);
        iconFactory.setTextAppearance(R.style.markers_text_style);
        iconFactory.setColor(ContextCompat.getColor(MainActivity.this, R.color.markers_red_background));
        return googleMap.addMarker(markerOptions.position(position)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(iconFactory
                                .makeIcon(getResources().getString(R.string.markers_end_label)))));
    }
}
