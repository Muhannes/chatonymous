package com.example.hannes.chatonymous;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startChatting(View view){
        Intent chatIntent = new Intent(this, ChatActivity.class);
        startActivity(chatIntent);
    }

    public void openMaps(View view){
        Intent mapsIntent = new Intent(this, MapsActivity.class);
        startActivity(mapsIntent);
    }

}

