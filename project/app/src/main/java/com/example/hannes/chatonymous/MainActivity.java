package com.example.hannes.chatonymous;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
                location.getLatitude(),
                location.getLongitude()
        });
        startActivity(chatIntent);
    }

    public void openMaps(View view) {
        Intent mapsIntent = new Intent(this, MapsActivity.class);
        mapsIntent.putExtra("LOCATION", new double[]{
                location.getLatitude(),
                location.getLongitude()
        });
        startActivity(mapsIntent);
    }

    /*private Location getLocation(){
        Location location = null;
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled){
            try {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(LOG_TAG, "Altitude: " + location.getLongitude());
                Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
            }catch (SecurityException e){
                Log.e(LOG_TAG, "no permission GPS_PROVIDER.");
                e.printStackTrace();
            }

        }else if (isNetworkEnabled){
            try {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d(LOG_TAG, "Altitude: " + location.getLongitude());
                Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
            }catch (SecurityException e){
                Log.e(LOG_TAG, "no permission NETWORK_PROVIDER.");
                e.printStackTrace();
            }
        }
        return location;
    }*/

    private Location getLocation(){
        Location location = null;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
                Log.d(LOG_TAG, "Longitude: " + location.getLongitude());
            }else if (isNetworkEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
                Log.d(LOG_TAG, "Longitude: " + location.getLongitude());
            }
        }else {
            Log.d(LOG_TAG, "Location permission denied...");
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);

            location = getLocation();
        }

        return location;
    }

    // For api min 23
    /*private Location getLocation(){
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
    }*/

}

