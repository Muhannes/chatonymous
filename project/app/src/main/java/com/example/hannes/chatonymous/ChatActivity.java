package com.example.hannes.chatonymous;

import android.app.Activity;
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

/**
 * Created by hannes on 5/7/17.
 */

public class ChatActivity extends Activity {
    static final String LOG_TAG = "ChatActivity";
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter out;
    Handler messageHandler;
    int serverPort;
    TextView messageBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageBoard = (TextView) findViewById(R.id.message_board);
        messageHandler = new Handler();
        new ServerThread().start();

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

    class ServerThread extends Thread{
        @Override
        public void run() {
            super.run();
            serverPort = 10001;
            try {
                serverSocket = new ServerSocket(serverPort);
            }catch (IOException e){
                e.printStackTrace();
            }
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new CommunicationThread(socket).start();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "could not connect to server!");
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread extends Thread{
        Socket socket;
        BufferedReader in;

        public CommunicationThread(Socket socket){
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        @Override
        public void run() {
            super.run();
            String connectionMessage = "Connected with " + socket.getInetAddress().getHostAddress() + "\n";
            messageHandler.post(new UpdateUIMessage(connectionMessage));
            String read;
            try {
                while ((read = in.readLine()) != null){

                    messageHandler.post(new UpdateUIMessage("stranger says: " + read + "\n"));


                }
            }catch (IOException e){
                Log.d(LOG_TAG, "Error when reading.");
                e.printStackTrace();
            }
            messageHandler.post(new UpdateUIMessage("Stranger left..."));
        }
    }

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
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIP);
                clientSocket = new Socket(serverAddr, serverPort);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(View view){
        new Thread(new sendMessageThread()).start();
    }

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

    public void connectToHost(View view){
        EditText et = (EditText) findViewById(R.id.ip_edit);
        String ip = et.getText().toString();
        new ClientThread(ip, 10001).start();
    }
}


