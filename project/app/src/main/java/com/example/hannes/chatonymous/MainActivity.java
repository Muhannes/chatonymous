package com.example.hannes.chatonymous;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback{
    static final String LOG_TAG = "MainActivity";
    private Location location;
    private static final int REQUESTCODE_LOCATION = 1;
    private static final int USER_REQUESTED_LOCATION = 1;

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
        startActivityForResult(mapsIntent, USER_REQUESTED_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_REQUESTED_LOCATION) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                double[] reqPos = data.getDoubleArrayExtra("reqPos");
                Log.d("MAPS RETURN LAT", Double.toString(reqPos[0]));
                Log.d("MAPS RETURN LON", Double.toString(reqPos[1]));
            }
        }
    }

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
            }, REQUESTCODE_LOCATION);

        }

        return location;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(LOG_TAG, "callback Reached!");
        if (requestCode == REQUESTCODE_LOCATION){
            Log.d(LOG_TAG, "in if statement!");
            location = getLocation();
        }
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

