package com.example.sveta.taxo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sveta.taxo.R;
import com.example.sveta.taxo.model.Driver;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        databaseReference = FirebaseDatabase.getInstance().getReference();

        String orderKey = getIntent().getStringExtra("orderKey");

        databaseReference.child(ORDER_CHILD).child(orderKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String driverId;
                if(!(driverId = (String) dataSnapshot.child("driverId").getValue()).equals(""))
                    databaseReference.child(DRIVER_CHILD).child(driverId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Driver driver = dataSnapshot.getValue(Driver.class);
                            driverName.setText(driver.getName());
                            driverPhoneNumber.setText(driver.getPhoneNumber());
                            carModel.setText(driver.getCarModel());
                            carNumber.setText(driver.getCarNumber());
                            changeOrderStatus(STATUS_WAITING);
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
                break;
            case (STATUS_READY):
                orderStatus.setText(R.string.car_ready);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
