package com.example.hannes.chatonymous;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private LatLng userPoint;
    private MarkerOptions userMarker;
    private LatLng savePoint;
    private String markerText;

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
                        markerText = country;
                        MarkerOptions marker = new MarkerOptions().position(
                                new LatLng(point.latitude, point.longitude)).title(markerText);
                        mMap.clear();
                        mMap.addMarker(marker);
                        mMap.addMarker(userMarker);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                        savePoint = point;
                        comfirm();


                    }
                } catch(Exception e) {
                    //Toast.makeText(this, "No Location Name Found", 600).show();
                }

            }
        });

    }

    public void comfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Location Chosen:")
                .setMessage("Are you sure you want to use "+markerText+" for your chat location?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("reqPos", new double[]{
                                savePoint.latitude,
                                savePoint.longitude
                        });
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


}
