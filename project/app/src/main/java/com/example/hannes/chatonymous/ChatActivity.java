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
    Handler messageHandler;
    static final int PORT = 10001;
    static final String SERVER_IP = "10.0.0.6";
    TextView messageBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageBoard = (TextView) findViewById(R.id.message_board);
        messageHandler = new Handler();
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
                out.println(InetAddress.getLocalHost().getHostAddress());
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
                Log.e(LOG_TAG, "could not connect to server!");
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

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
            clientSocket = connectToSocket(serverIP, serverPort);

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

}


