package com.example.sveta.taxo.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sveta.taxo.R;
import com.example.sveta.taxo.api.ApiInterface;
import com.example.sveta.taxo.api.RouteApiClient;
import com.example.sveta.taxo.model.Driver;
import com.example.sveta.taxo.model.Order;
import com.example.sveta.taxo.model.RouteResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String DRIVER_CHILD = "drivers";
    private static final String ORDER_CHILD = "orders";
    private static final int STATUS_WAITING = 11;
    private static final int STATUS_READY = 12;
    private DatabaseReference databaseReference;
    private TextView driverName;
    private TextView driverPhoneNumber;
    private TextView carModel;
    private TextView carNumber;
    private TextView priceTotal;
    private TextView orderStatus;
    private TextView carColor;
    private MarkerOptions markerOptions;
    private GoogleMap googleMap;
    private LatLng driverPosition;
    private ApiInterface routeApiInterface;
    private List<LatLng> routePoints;
    private Call<RouteResponse> routeModelCall;
    private LatLng startPosition;
    private PolylineOptions polylineOptions;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        driverName = (TextView) findViewById(R.id.driver_name);
        driverPhoneNumber = (TextView) findViewById(R.id.driver_phone_number);
        carModel = (TextView) findViewById(R.id.car_model);
        carNumber = (TextView) findViewById(R.id.car_number);
        priceTotal = (TextView) findViewById(R.id.price_total);
        orderStatus = (TextView) findViewById(R.id.order_status);
        carColor = (TextView) findViewById(R.id.car_color);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        routeApiInterface = RouteApiClient.getClient().create(ApiInterface.class);

        String orderKey = getIntent().getStringExtra("orderKey");

        databaseReference.child(ORDER_CHILD).child(orderKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String driverId;
                Order order = dataSnapshot.getValue(Order.class);
                startPosition = new LatLng(order.getFromCoords().get(getString(R.string.latitude)),
                        order.getFromCoords().get(getString(R.string.longitude)));
                driverPosition = new LatLng(order.getDriverPos().get(getString(R.string.latitude)),
                        order.getDriverPos().get(getString(R.string.longitude)));
                status = order.getStatus();
                String price = order.getPrice() + " грн";
                priceTotal.setText(price);
                if(!(driverId = (String) dataSnapshot.child("driverId").getValue()).equals(""))
                    databaseReference.child(DRIVER_CHILD).child(driverId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Driver driver = dataSnapshot.getValue(Driver.class);
                            driverName.setText(driver.getName());
                            driverPhoneNumber.setText(driver.getPhoneNumber());
                            carModel.setText(driver.getCarModel());
                            carNumber.setText(driver.getCarNumber());
                            carColor.setText(driver.getCarColor());

                            addStartMarker(startPosition);
                            addEndMarker(driverPosition);

                            if (status.equals("arrived"))
                                changeOrderStatus(STATUS_READY);
                            else if (status.equals("accepted")) {
                                changeOrderStatus(STATUS_WAITING);
                                getRoute();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void changeOrderStatus(int status) {
        switch (status) {
            case (STATUS_WAITING):
                orderStatus.setText(R.string.waiting_for_car);
                getNotification(getString(R.string.waiting_for_car));
                break;
            case (STATUS_READY):
                orderStatus.setText(R.string.car_ready);
                getNotification(getString(R.string.car_ready));
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        markerOptions = new MarkerOptions();
    }

    private void getRoute() {
        routeModelCall = routeApiInterface.getRoute(driverPosition.latitude + "," + driverPosition.longitude,
                startPosition.latitude + "," + startPosition.longitude);

        if (routeModelCall != null) {
            routeModelCall.enqueue(new Callback<RouteResponse>() {
                @Override
                public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                    RouteResponse routeResponse = response.body();
                    routePoints = PolyUtil.decode(routeResponse.getPoints());
                    drawRoute();
                }

                @Override
                public void onFailure(Call<RouteResponse> call, Throwable t) {

                }
            });
        }
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
        googleMap.addPolyline(polylineOptions);
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        googleMap.moveCamera(track);
    }

    private void addStartMarker(LatLng position) {
        IconGenerator iconFactory = new IconGenerator(DetailOrderActivity.this);
        iconFactory.setTextAppearance(R.style.markers_text_style);
        iconFactory.setColor(ContextCompat.getColor(DetailOrderActivity.this, R.color.markers_green_background));
        googleMap.addMarker(markerOptions.position(position)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(iconFactory
                                .makeIcon(getResources().getString(R.string.markers_start_label)))));
    }

    private void addEndMarker(LatLng position) {
        IconGenerator iconFactory = new IconGenerator(DetailOrderActivity.this);
        iconFactory.setTextAppearance(R.style.markers_text_style);
        iconFactory.setColor(ContextCompat.getColor(DetailOrderActivity.this, R.color.markers_red_background));
        googleMap.addMarker(markerOptions.position(position)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(iconFactory
                                .makeIcon(getResources().getString(R.string.markers_driver_position)))));
    }

    private void getNotification(String status) {
        Intent intent = new Intent(this, DetailOrderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle("Taxo")
                .setContentText(status)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
