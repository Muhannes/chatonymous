package com.example.hannes.chatonymous;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by hannes on 5/7/17.
 */

public class ChatActivity extends Activity {
    static final String LOG_TAG = "ChatActivity";
    Socket clientSocket;
    Handler messageHandler;
    static final int PORT = 10001;
    static final String SERVER_IP = "10.0.0.6"; //"130.240.156.21"; //"10.0.0.6"; // "34.223.250.25"; <--- for AWS server
    TextView messageBoard;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageBoard = (TextView) findViewById(R.id.message_board);
        messageHandler = new Handler();
        double[] loc = getIntent().getDoubleArrayExtra("LOCATION");
        latitude = loc[0];
        longitude = loc[1];
        Log.d(LOG_TAG, latitude + " ... " + longitude);
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
            String connectionMessage = "Connected with " + clientSocket.getInetAddress().getHostAddress() + "\n";
            messageHandler.post(new UpdateUIMessage(connectionMessage));
            String read;
            try {
                String readyMsg = in.readLine();
                Log.d(LOG_TAG, readyMsg);
                if(readyMsg.equals("ready")) {
                    while ((read = in.readLine()) != null) {
                        messageHandler.post(new UpdateUIMessage("Stranger says: " + read + "\n"));
                    }
                }
            }catch (IOException e){
                Log.d(LOG_TAG, "Error when reading.");
                e.printStackTrace();
            }
            messageHandler.post(new UpdateUIMessage("Conversation finished.\n"));
        }
    }

    /**
     * updates the gui with a message on the messageboard.
     */
    class UpdateUIMessage implements Runnable {
        String msg;
        public UpdateUIMessage(String msg){
            this.msg = msg;
        }
        @Override
        public void run() {
            messageBoard.append(msg);
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
                out.println("connect " + latitude + " " + longitude);
            }catch (Exception e){
                e.printStackTrace();
                messageHandler.post(new UpdateUIMessage("could not connect!"));
            }

            new CommunicationThread().start();
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
            EditText et = (EditText) findViewById(R.id.message_text);
            String msg = et.getText().toString();
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                out.println(msg);
            }catch (IOException e){
                messageHandler.post(new UpdateUIMessage("Message could not be sent!"));
                Log.d(LOG_TAG, "Error when sending message!");
                e.printStackTrace();
            }
        }
    }

    /**
     * returns a socket connected with the
     * given ip and port.(Null if no connection could be made)
     * @param ip
     * @param port
     * @return
     */
    private Socket connectToSocket(String ip, int port){
        Socket socket = null;
        try {
            InetAddress serverAddr = InetAddress.getByName(ip);
            socket = new Socket(serverAddr, port);
        }catch (Exception e){
            e.printStackTrace();
            messageHandler.post(new UpdateUIMessage("could not connect!"));
        }
        return socket;
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
        Runnable r = new Runnable() {
            @Override
            public void run() {
                closeChat();
                new ConnectionThread().start();
            }
        };
        new Thread(r).start();

    }

    private void closeChat(){
        if (clientSocket != null && !clientSocket.isClosed()){
            try {
                clientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
                Log.d(LOG_TAG, "error when closing socket.");
            }
        }

    }



}


