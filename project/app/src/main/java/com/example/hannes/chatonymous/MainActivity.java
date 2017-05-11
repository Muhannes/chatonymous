package com.example.hannes.chatonymous;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    static final String LOG_TAG = "MainActivity";
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location = getLocation();

    }

    public void startChatting(View view) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("LOCATION", new double[]{
                location.getAltitude(),
                location.getLatitude()
        });
        startActivity(chatIntent);
    }

    public void openMaps(View view) {
        Intent mapsIntent = new Intent(this, MapsActivity.class);
        mapsIntent.putExtra("LOCATION", new double[]{
                location.getAltitude(),
                location.getLatitude()
        });
        startActivity(mapsIntent);
    }

    private Location getLocation(){
        Location location = null;

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(LOG_TAG, "Altitude: " + location.getAltitude());
                Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
            }else if (isNetworkEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d(LOG_TAG, "Altitude: " + location.getAltitude());
                Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
            }
        }else {
            Log.d(LOG_TAG, "Location permission denied...");
            requestPermissions(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
            location = getLocation();
        }

        return location;
    }

}

