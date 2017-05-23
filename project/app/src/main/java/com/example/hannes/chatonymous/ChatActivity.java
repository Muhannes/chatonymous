package com.example.hannes.chatonymous;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by hannes on 5/7/17.
 */

public class ChatActivity extends AppCompatActivity {
    static final String LOG_TAG = "ChatActivity";
    static final int COLOR_THIS = Color.CYAN;
    static final int COLOR_STRANGER = Color.GRAY;
    static final int COLOR_CPU = Color.YELLOW;
    Socket clientSocket;
    Handler messageHandler;
    static final int PORT = 10001;
    static final String SERVER_IP = "34.223.250.25"; //<--- for AWS server //"10.0.0.6"; //"130.240.156.21"; //"10.0.0.6"; //
    RelativeLayout messageBoard;
    ScrollView scrollView;
    double latitude1;
    double longitude1;
    double latitude2;
    double longitude2;
    int distance; // distance in km.
    boolean writeEnabled = false;
    TextView lastMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        messageBoard = (RelativeLayout) findViewById(R.id.message_board);
        scrollView = (ScrollView) findViewById(R.id.message_board_scroll);
        messageHandler = new Handler();
        double[] loc = getIntent().getDoubleArrayExtra("LOCATION");
        latitude1 = loc[0];
        longitude1 = loc[1];
        latitude2 = loc[2];
        longitude2 = loc[3];
        Log.d("LAT1", Double.toString(latitude1));
        Log.d("LONG1", Double.toString(longitude1));
        Log.d("LAT2", Double.toString(latitude2));
        Log.d("LONG2", Double.toString(longitude2));

        SharedPreferences sharedpreferences = getSharedPreferences("chatonymousSettings", Context.MODE_PRIVATE);
        distance = sharedpreferences.getInt("userRange", 10);
        new ConnectionThread().start();

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
            case android.R.id.home:
                finish();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Listens for messages from "stranger", until connection is broken
     * calls handler to write them on messageboard
     */
    class CommunicationThread extends Thread{
        BufferedReader in;

        public CommunicationThread(){
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        @Override
        public void run() {
            super.run();
            String connectionMessage = "Connected with server.";
            messageHandler.post(new UpdateUIMessage(connectionMessage, COLOR_CPU));
            String read;
            try {
                boolean stop = false;
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                while(!stop) { // reads until it gets "ready" from server, if it is not "ready", the server wants to know if we still here. answer.
                    String readyMsg = in.readLine();
                    Log.d(LOG_TAG, readyMsg);
                    if (readyMsg.equals("ready")) { //If server writes "ready", we have got a match
                        stop = true;
                        messageHandler.post(new UpdateUIMessage("Stranger found, start chatting!", COLOR_CPU));
                        writeEnabled = true;
                        while ((read = in.readLine()) != null) {
                            messageHandler.post(new UpdateUIMessage(read, COLOR_STRANGER));
                        }
                        writeEnabled = false;
                    }else {
                        out.println("Still here"); //Let the server know we are still here.
                    }
                }
                //if other end close the socket, we are notified they have left
                messageHandler.post(new UpdateUIMessage("Conversation finished.", COLOR_CPU));
            }catch (IOException e){
                writeEnabled = false;
                Log.d(LOG_TAG, "Error when reading.");
                e.printStackTrace();
            }
        }
    }

    /**
     * updates the gui with a message on the messageboard.
     */
    class UpdateUIMessage implements Runnable {
        String msg;
        int bgColor;
        public UpdateUIMessage(String msg, int backgroundColor){
            this.msg = msg;
            this.bgColor = backgroundColor;
        }
        @Override
        public void run() {
            messageBoard.addView(createMessageView(msg, bgColor));
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    }

    class ClearEditText implements Runnable {
        @Override
        public void run() {
            EditText et = (EditText) findViewById(R.id.message_text);
            et.setText("");
        }
    }

    /**
     * Thread that starts a connection to a socket.
     */
    class ConnectionThread extends Thread {
        String ip;
        int port;
        public ConnectionThread(){
            this.ip = SERVER_IP;
            this.port = PORT;
        }
        @Override
        public void run() {
            super.run();
            Log.d(LOG_TAG, "trying to connect to: " + ip + ":" + port);
            try {
                InetAddress addr = InetAddress.getByName(ip);
                clientSocket = new Socket(addr, port);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                out.println(distance + " " + latitude1 + " " + longitude1 + " " + latitude2 + " " + longitude2);
                new CommunicationThread().start();
            }catch (Exception e){
                e.printStackTrace();
                messageHandler.post(new UpdateUIMessage("could not connect!", COLOR_CPU));
            }


        }

    }

    public void sendMessage(View view){
        if(writeEnabled) { //If it is connected to another phone.
            new Thread(new sendMessageThread()).start();
        } else {
            createToast("You need to find a chat-partner first");
        }
    }

    /**
     * Sends a message to the currently connected clientSocket.
     * The message is taken from the textedit.
     */
    class sendMessageThread implements Runnable {
        @Override
        public void run() {
            EditText et = (EditText) findViewById(R.id.message_text);
            String msg = et.getText().toString(); //get text from input
            if(msg.length()>0) {
                try { //write message, and print it on the screen.
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                    out.println(msg);
                    messageHandler.post(new UpdateUIMessage(msg, COLOR_THIS));
                } catch (IOException e) {
                    messageHandler.post(new UpdateUIMessage("Message could not be sent!", COLOR_CPU));
                    Log.d(LOG_TAG, "Error when sending message!");
                    e.printStackTrace();
                }
                messageHandler.post(new ClearEditText());
            } else {
                messageHandler.post(new ToastTread());
            }
        }
    }

    public void createToast(String ttext) {
        Toast t = Toast.makeText(this, ttext, Toast.LENGTH_LONG);
        t.show();
    }

    class ToastTread implements Runnable {
        @Override
        public void run() {
            Toast t = Toast.makeText(getApplicationContext(), "No Empty messages allowed.", Toast.LENGTH_LONG);
            t.show();
        }
    }


    /**
     * returns to MainActivity
     * @param view
     */
    public void stopChatting(View view){
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        closeChat();
    }

    /**
     * closes current chat(if open),
     * starts searching for new one.
     * @param view
     */
    public void findNewMatch(View view){
        messageBoard.removeAllViews();
        lastMessage = null;
        closeChat();
        new ConnectionThread().start();
    }

    private void closeChat(){
        if (clientSocket != null && !clientSocket.isClosed()){
            try {
                clientSocket.close();
                Log.d(LOG_TAG, "Closed socket.");
            }catch (IOException e){
                e.printStackTrace();
                Log.d(LOG_TAG, "error when closing socket.");
            }
        }

    }

    /* Creates and returns the View that will display a given message.
        Sets background color of view to backgroundColor.
        TODO: Set width to wrap_content and float left/right depending on color?
      */
    private TextView createMessageView(String msg, int backgroundColor){
        TextView tV = new TextView(this);
        tV.setBackgroundResource(R.drawable.message_box);

        GradientDrawable drawable = (GradientDrawable) tV.getBackground();
        drawable.setColor(backgroundColor);
        tV.getBackground().setAlpha(128);
        tV.setTextColor(tV.getTextColors().withAlpha(255));
        tV.setText(msg);
        RelativeLayout.LayoutParams lp = null;
        if (backgroundColor == COLOR_THIS){
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }else if (backgroundColor == COLOR_STRANGER){
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }else {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        lp.setMargins(4,4,4,4);
        if (lastMessage != null){
            lp.addRule(RelativeLayout.BELOW, lastMessage.getId());
        }
        tV.setLayoutParams(lp);
        lastMessage = tV;
        lastMessage.setId(View.generateViewId());
        return tV;
    }

}