package com.example.hannes.chatonymous;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    static final String LOG_TAG = "MainActivity";
    private Location location;
    private double[] userSettings;
    private static final int REQUESTCODE_LOCATION = 1;
    private static final int USER_REQUESTED_LOCATION = 1;
    private static final int USER_REQUESTED_RANGE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        location = getLocation();
        userSettings = new double[1];
        userSettings[0] = 15;

    }

    @Override //toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override //toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("userSettings", userSettings);
                startActivityForResult(settingsIntent, USER_REQUESTED_RANGE);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void startChatting(View view) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        setLocation(chatIntent);

        startActivity(chatIntent);
    }

    public void openMaps(View view) {
        Intent mapsIntent = new Intent(this, MapsActivity.class);
        setLocation(mapsIntent);
        startActivityForResult(mapsIntent, USER_REQUESTED_LOCATION);
    }

    private void setLocation(Intent intent){
        if (location != null){
            intent.putExtra("LOCATION", new double[]{
                    location.getLatitude(),
                    location.getLongitude()
            });
        }else { //default professorsvägen
            intent.putExtra("LOCATION", new double[]{
                    65.61954811,
                    22.1518592
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_REQUESTED_LOCATION && resultCode == RESULT_OK) {
            // Make sure the request was successful
            double[] reqPos = data.getDoubleArrayExtra("reqPos");
            Log.d("MAPS RETURN LAT", Double.toString(reqPos[0]));
            Log.d("MAPS RETURN LON", Double.toString(reqPos[1]));
        }
        else if (requestCode == USER_REQUESTED_RANGE && resultCode == RESULT_OK) {
            double[] settingsData = data.getDoubleArrayExtra("settingsData");
            Log.d("Settings Return", Double.toString(settingsData[0]));
        }
    }

    private Location getLocation(){
        Location location = null;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            try {
                if (isGPSEnabled){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
                    Log.d(LOG_TAG, "Longitude: " + location.getLongitude());
                }else if (isNetworkEnabled){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Log.d(LOG_TAG, "Latitude: " + location.getLatitude());
                    Log.d(LOG_TAG, "Longitude: " + location.getLongitude());
                }
            }catch (NullPointerException e){
                Toast t = Toast.makeText(this, "GPS is disabled.\nEnable in settings.", Toast.LENGTH_LONG);
                t.show();
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

