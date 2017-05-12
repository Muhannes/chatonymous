package com.example.hannes.chatonymous;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    private int rangeValue;
    private int defaultRange = 10;
    private int userRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        double[] userSettings = getIntent().getDoubleArrayExtra("userSettings");
        userRange = (int) userSettings[0];
        Log.d("USER Range: ", Double.toString(userSettings[0]));
        if(Double.toString(userRange) == "") {
            rangeValue=defaultRange;
        } else {
            rangeValue=userRange;
        }
        SeekBar seekBar = (SeekBar)findViewById(R.id.range_seekBar);
        seekBar.setProgress(rangeValue);
        final TextView seekBarValue = (TextView)findViewById(R.id.seekbarvalue);
        seekBarValue.setText(String.valueOf(rangeValue));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText(String.valueOf(progress));
                rangeValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    protected void saveAndExit(View view) {
        Intent intent = new Intent();
        intent.putExtra("settingsData", new double[]{
                rangeValue
        });
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override //toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }
    @Override //toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

}
