package com.ck.taxoteam.taxoclient.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ck.taxoteam.taxoclient.R;
import com.ck.taxoteam.taxoclient.api.ApiInterface;
import com.ck.taxoteam.taxoclient.api.RouteApiClient;
import com.ck.taxoteam.taxoclient.model.Driver;
import com.ck.taxoteam.taxoclient.model.Order;
import com.ck.taxoteam.taxoclient.model.RouteResponse;
import com.ck.taxoteam.taxoclient.reciver.NetworkStateReceiver;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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

public class DetailOrderActivity extends AppCompatActivity implements OnMapReadyCallback, NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String DRIVER_CHILD = "drivers";
    private static final String ORDER_CHILD = "orders";
    private static final int STATUS_WAITING = 11;
    private static final int STATUS_READY = 12;
    private static final int STATUS_COMPLETE = 13;
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
    private Marker driverMarker;
    private Polyline polyline;
    private String orderKey;
    private NotificationManager notificationManager;
    private RelativeLayout parent;
    private NetworkStateReceiver networkStateReceiver;
    private Snackbar networkStateSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        parent = (RelativeLayout) findViewById(R.id.details_activity_parent);

        driverName = (TextView) findViewById(R.id.driver_name);
        driverPhoneNumber = (TextView) findViewById(R.id.driver_phone_number);
        carModel = (TextView) findViewById(R.id.car_model);
        carNumber = (TextView) findViewById(R.id.car_number);
        priceTotal = (TextView) findViewById(R.id.price_total);
        orderStatus = (TextView) findViewById(R.id.order_status);
        carColor = (TextView) findViewById(R.id.car_color);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        routeApiInterface = RouteApiClient.getClient().create(ApiInterface.class);

        orderKey = getIntent().getStringExtra("orderKey");

        driverPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = driverPhoneNumber.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        databaseReference.child(ORDER_CHILD).child(orderKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String driverId;
                Order order = dataSnapshot.getValue(Order.class);
                startPosition = new LatLng(order.getFromCoords().get(getString(R.string.latitude)),
                        order.getFromCoords().get(getString(R.string.longitude)));
                try {
                    driverPosition = new LatLng(order.getDriverPos().get(getString(R.string.latitude)),
                            order.getDriverPos().get(getString(R.string.longitude)));
                }
                catch (Exception e){

                }
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

                            try {
                                addStartMarker(startPosition);

                                if (driverMarker != null)
                                    driverMarker.remove();
                                driverMarker = addEndMarker(driverPosition);

                                switch (status) {
                                    case "arrived":
                                        changeOrderStatus(STATUS_READY);
                                        break;
                                    case "accepted":
                                        changeOrderStatus(STATUS_WAITING);
                                        getRoute();
                                        break;
                                    case "completed":
                                        changeOrderStatus(STATUS_COMPLETE);
                                        break;
                                }
                            }
                            catch (Exception e) {
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
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        networkStateSnackbar = Snackbar.make(parent, getResources().getString(R.string.network_down_snackbar_text),Snackbar.LENGTH_INDEFINITE);
        networkStateSnackbar.setAction(getResources().getText(R.string.action_open_wifi), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

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
            case (STATUS_COMPLETE):
                complete();
        }
    }

    private void complete() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dialog_order_completed_title))
                .setMessage(getResources().getString(R.string.dialog_order_completed_message))
                .setPositiveButton(getResources().getString(R.string.new_order), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(DetailOrderActivity.this, MainActivity.class);
                        notificationManager.cancelAll();
                        DetailOrderActivity.this.startActivity(intent);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DetailOrderActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        notificationManager.cancelAll();
                        DetailOrderActivity.this.startActivity(intent);
                    }
                }).show();
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
                    try {
                        RouteResponse routeResponse = response.body();
                        routePoints = PolyUtil.decode(routeResponse.getPoints());
                        drawRoute();
                    }
                    catch (IndexOutOfBoundsException e){

                    }
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

        if (polyline != null)
            polyline.remove();
        polyline = googleMap.addPolyline(polylineOptions);

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

    private Marker addEndMarker(LatLng position) {
        IconGenerator iconFactory = new IconGenerator(DetailOrderActivity.this);
        iconFactory.setTextAppearance(R.style.markers_text_style);
        iconFactory.setColor(ContextCompat.getColor(DetailOrderActivity.this, R.color.markers_red_background));
        return googleMap.addMarker(markerOptions.position(position)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(iconFactory
                                .makeIcon(getResources().getString(R.string.markers_driver_position)))));
    }

    private void getNotification(String status) {
        Intent intent = new Intent(this, DetailOrderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("orderKey", orderKey);

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

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void networkAvailable() {
        if(networkStateSnackbar.isShown()) {
            networkStateSnackbar.dismiss();
        }
    }

    @Override
    public void networkUnavailable() {
        networkStateSnackbar.show();
    }
}
