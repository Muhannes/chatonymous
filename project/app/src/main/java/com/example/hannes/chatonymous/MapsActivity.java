package com.example.hannes.chatonymous;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng userPoint;
    MarkerOptions userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        double[] userPos = getIntent().getDoubleArrayExtra("LOCATION");
        Log.d("USER LAT: ", Double.toString(userPos[0]));
        Log.d("USER LON:", Double.toString(userPos[1]));
        userPoint = new LatLng(userPos[0], userPos[1]);
        userMarker = new MarkerOptions().position(userPoint).title("USER LOCATION");

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(userMarker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userPoint));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                try {
                    Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(point.latitude, point.longitude, 1);

                    if (addresses.size() > 0) {
                        String region = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();
                        Log.d("REGION",region);
                        Log.d("Country",country);
                        String markerText = country;
                        MarkerOptions marker = new MarkerOptions().position(
                                new LatLng(point.latitude, point.longitude)).title(markerText);
                        mMap.clear();
                        mMap.addMarker(marker);
                        mMap.addMarker(userMarker);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

                        Intent intent = new Intent();
                        intent.putExtra("reqPos", new double[]{
                                point.latitude,
                                point.longitude
                        });
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch(Exception e) {
                    //Toast.makeText(this, "No Location Name Found", 600).show();
                }

            }
        });
    }
}
