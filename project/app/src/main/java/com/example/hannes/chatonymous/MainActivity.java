package com.example.hannes.chatonymous;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private int userRange;
    private double[] userRequestedPosition;
    private static final int REQUESTCODE_LOCATION = 1;
    private static final int USER_REQUESTED_LOCATION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        location = getLocation();

        SharedPreferences sharedpreferences = getSharedPreferences("chatonymousSettings", Context.MODE_PRIVATE);
        userRange = sharedpreferences.getInt("userRange", 10);

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
                //settingsIntent.putExtra("userRange", userRange);
                startActivity(settingsIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    starts chat intent.
    for button "Start Chatting"
     */
    public void startChatting(View view) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        setLocation(chatIntent);
        chatIntent.putExtra("USER_SETTINGS", userRange);
        startActivity(chatIntent);
    }

    /*
    Starts Maps intent.
    for button "Choose from map"
     */
    public void openMaps(View view) {
        Intent mapsIntent = new Intent(this, MapsActivity.class);
        setLocation(mapsIntent);
        startActivityForResult(mapsIntent, USER_REQUESTED_LOCATION);
    }

    /*
        Puts two locations extra for intent.
        one for where we are, and one for where we want to chat with someone.
     */
    private void setLocation(Intent intent){
        if (location != null && userRequestedPosition != null) {
            intent.putExtra("LOCATION", new double[]{ // if user has gps, and chosen a preferred location.
                    location.getLatitude(),
                    location.getLongitude(),
                    userRequestedPosition[0],
                    userRequestedPosition[1]
            });
        }else if (location != null) { // if user has gps, but not chosen preferred location.
            intent.putExtra("LOCATION", new double[]{
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getLatitude(),
                    location.getLongitude()
            });
        }else { //default professorsvägen if gps is not enabled.
            intent.putExtra("LOCATION", new double[]{
                    65.61954811,
                    22.1518592,
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
            userRequestedPosition = data.getDoubleArrayExtra("reqPos");
        }
    }

    /*
        Returns the current location.
        Uses GPS, or Network based solutions.
        Requests permissions if needed.
     */
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
        if (requestCode == REQUESTCODE_LOCATION){ // if permission for gps had to be requested.
            location = getLocation();   // try getting location again.
        }
    }



}

