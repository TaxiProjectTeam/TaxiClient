package com.example.sveta.taxo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sveta.taxo.activity.DetailOrderActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Sveta on 02.03.2017.
 */

public class MainViewImpl extends AppCompatActivity implements MainView, View.OnClickListener, AdapterView.OnItemClickListener, OnMapReadyCallback {

    private RecyclerView recyclerView;
    private SupportMapFragment mapFragment;
    private TextView totalOrder;
    private Button plusFiveBtn;
    private Button orderBtn;
    private EditText additionalComment;

    private MainPresenterImpl presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_address);
        totalOrder = (TextView) findViewById(R.id.price_total);
        plusFiveBtn = (Button) findViewById(R.id.button_plus);
        orderBtn = (Button) findViewById(R.id.order);
        additionalComment = (EditText) findViewById(R.id.additional_comment);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        plusFiveBtn.setOnClickListener(this);
        orderBtn.setOnClickListener(this);
        mapFragment.getMapAsync(this);

        presenter = new MainPresenterImpl(this);
    }

    @Override
    public void navigateToDetailOrder() {
        startActivity(new Intent(this, DetailOrderActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.price_total:
                presenter.order();
                break;
            case R.id.button_plus:
                presenter.plusFiveToOrder();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
