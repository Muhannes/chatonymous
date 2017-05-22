package com.example.hannes.chatonymous;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class ChatActivity extends Activity {
    static final String LOG_TAG = "ChatActivity";
    static final int COLOR_THIS = Color.CYAN;
    static final int COLOR_STRANGER = Color.GRAY;
    static final int COLOR_CPU = Color.YELLOW;
    Socket clientSocket;
    Handler messageHandler;
    static final int PORT = 10001;
    static final String SERVER_IP = "10.0.0.6"; //"130.240.156.21"; //"10.0.0.6"; // "34.223.250.25"; <--- for AWS server
    LinearLayout messageBoard;
    double latitude1;
    double longitude1;
    double latitude2;
    double longitude2;
    int distance; // distance in km.
    boolean writeEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageBoard = (LinearLayout) findViewById(R.id.message_board);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    /**
     * Listens for messages from "stranger", until connection is broken
     * writes them on messageboard
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
                while(!stop) {
                    String readyMsg = in.readLine();
                    Log.d(LOG_TAG, readyMsg);
                    if (readyMsg.equals("ready")) {
                        stop = true;
                        messageHandler.post(new UpdateUIMessage("Stranger found, start chatting!", COLOR_CPU));
                        writeEnabled = true;
                        while ((read = in.readLine()) != null) {
                            messageHandler.post(new UpdateUIMessage(read, COLOR_STRANGER));
                        }
                        writeEnabled = false;
                    }else {
                        out.println("Still here");
                    }
                }
            }catch (IOException e){
                writeEnabled = false;
                Log.d(LOG_TAG, "Error when reading.");
                e.printStackTrace();
            }
            messageHandler.post(new UpdateUIMessage("Conversation finished.", COLOR_CPU));
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
        new Thread(new sendMessageThread()).start();
    }

    /**
     * Sends a message to the currently connected clientSocket.
     * The message is taken from the textedit.
     */
    class sendMessageThread implements Runnable {
        @Override
        public void run() {
            if (writeEnabled) { //If it is connected to another phone.
                EditText et = (EditText) findViewById(R.id.message_text);
                String msg = et.getText().toString();

                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                    out.println(msg);
                    messageHandler.post(new UpdateUIMessage(msg, COLOR_THIS));
                } catch (IOException e) {
                    messageHandler.post(new UpdateUIMessage("Message could not be sent!", COLOR_CPU));
                    Log.d(LOG_TAG, "Error when sending message!");
                    e.printStackTrace();
                }
                messageHandler.post(new ClearEditText());
            }
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

    private TextView createMessageView(String msg, int backgroundColor){
        TextView tV = new TextView(this);
        tV.setBackgroundResource(R.drawable.message_box);

        GradientDrawable drawable = (GradientDrawable) tV.getBackground();
        drawable.setColor(backgroundColor);
        tV.setText(msg);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (backgroundColor == COLOR_THIS){
            llp.setMargins(100, 4,4,4);
        }else if (backgroundColor == COLOR_STRANGER){
            llp.setMargins(4,4,100,4);
        }else {
            llp.setMargins(4,4,4,4);
        }
        tV.setLayoutParams(llp);
        return tV;
    }



}


