package com.example.hannes.chatonymous;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import java.util.jar.Manifest;

/**
 * Created by hannes on 5/7/17.
 */

public class ChatActivity extends Activity {
    static final String LOG_TAG = "ChatActivity";
    ServerSocket serverSocket;
    Socket clientSocket;
    Handler messageHandler;
    static final int PORT = 10001;
    static final String SERVER_IP = "192.168.1.30"; //"130.240.156.21"; //"10.0.0.6";
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
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }



    /**
     * Creates a connection to the server.
     * If server found a match to chat with, start connection to them.
     * otherwise, start serverThread and wait for match.
     */
    class ConnectionThread extends Thread {
        Socket socket;
        @Override
        public void run() {
            super.run();
            String ip = null;
            socket = connectToSocket(SERVER_IP, PORT);

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("connect " + latitude + " " + longitude);
                ip = in.readLine();
            } catch (IOException e){
                e.printStackTrace();
                Log.e(LOG_TAG, "error in writing/reading.");
            }
            if (ip != null){
                Log.d(LOG_TAG, "got connection address!");
                new ClientThread(ip, PORT).start();
            }else {
                new ServerThread().start();
                Log.d(LOG_TAG, "Added to queue, wating for connection...");
            }
        }
    }

    /**
     * Listens for a connection.
     * When found start a communicationThread.
     */
    class ServerThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                serverSocket = new ServerSocket(PORT);
            }catch (IOException e){
                e.printStackTrace();
            }

            try {
                clientSocket = serverSocket.accept();
                new CommunicationThread().start();

            } catch (IOException e) {
                Log.d(LOG_TAG, "Waiting interrupted. " + e.getMessage());
            }
            try {
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

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
                while ((read = in.readLine()) != null){
                    messageHandler.post(new UpdateUIMessage("Stranger says: " + read + "\n"));
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
    class ClientThread extends Thread {
        String serverIP;
        int serverPort;
        public ClientThread(String serverIP, int serverPort){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
        }
        @Override
        public void run() {
            super.run();
            clientSocket = connectToSocket(serverIP, serverPort);

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
                Thread t = closeChat(); //TODO: Does not finish before new chat is opened...
                if (t != null){
                    try {
                        t.join();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                new ConnectionThread().start();
            }
        };
        new Thread(r).start();

    }

    private Thread closeChat(){
        if (clientSocket != null && !clientSocket.isClosed()){
            try {
                clientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
                Log.d(LOG_TAG, "error when closing socket.");
            }
        }else if (serverSocket != null && !serverSocket.isClosed()){
            try {
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
                Log.d(LOG_TAG, "error when closing socket.");
            }
            Thread t = new Thread(new RemoveFromQueue());
            t.start();
            return t;

        }
        return null;

    }



    class RemoveFromQueue implements Runnable {
        @Override
        public void run() {
            Socket socket = connectToSocket(SERVER_IP, PORT);

            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("disconnect");
            } catch (IOException e){
                e.printStackTrace();
                Log.e(LOG_TAG, "error in leaving queue.");
            }
        }
    }

}


