import java.net.*; // need this for InetAddress, Socket, ServerSocket
import java.io.*; // need this for I/O stuff
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Iterator;

public class ChatonymousServer {
  int port;
  LinkedList<String> ips = new LinkedList<String>(); //TODO: Should be synchronized somehow...
  LinkedList<Client> clients = new LinkedList<Client>();
  // main starts things rolling
  static public void main(String args[]) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Must specify a port!");
    }
    int port = Integer.parseInt(args[0]);
    new ChatonymousServer(port).socketListener();
  }

  public ChatonymousServer(int port){
    this.port = port;
  }

  private void socketListener(){
    try { // Create Server Socket (passive socket)
      ServerSocket ss = new ServerSocket(port);
      while (true) {
        Socket s = ss.accept();
        new ClientHandler(s).start();
      }
    } catch (IOException e) {
      System.out.println("Fatal I/O Error !");
      System.exit(0);
    }
  }

  class ClientHandler extends Thread {
    Socket s;
    public ClientHandler(Socket s){
      this.s = s;
    }

    @Override
    public void run(){
      try {
        handleClient(s);
      }catch(IOException e){
        e.printStackTrace();
      }
    }
    // this method handles one client // declared as throwing IOException - this means it throws // up to the calling method (who must handle it!)
    // try taking out the "throws IOException" and compiling, // the compiler will tell us we need to deal with this!
    private void handleClient(Socket s) throws IOException {
      // print out client's address
      String remoteIP = s.getInetAddress().getHostAddress();
      String remotePort = Integer.toString(s.getPort());
      System.out.println("Connection from " + remoteIP + "/ " + remotePort);

      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
      String read = in.readLine(); //now assuming only one line!!
      System.out.println("Read: " + read);
      //parsedRead = [action, latitude, longitude] (ex: [connect, 1.2141, 4.13141])
      String[] parsedRead = read.split(" ");

      //if (parsedRead[0].equals("connect")) {
      Client c = new Client(s, in, Double.parseDouble(parsedRead[1]), Double.parseDouble(parsedRead[2]));
      boolean cont = true;

      while(cont){
        cont = false;
        Client cMatch = findMatch(c);
        if (cMatch != null) { //Match is found, send back ip to which it should connect to.
          clients.remove(cMatch);
          Socket s2 = cMatch.getSocket();
          if (s2.isClosed()) {
            System.out.println("Found disconnected socket...");
            cont = true;
          }else {
            new CommunicationThread(c, cMatch).start();
            new CommunicationThread(cMatch, c).start();

            System.out.println("found match!");
          }

        }else { // No match is found, add self to queue.
          clients.add(c);
          System.out.println("Found no match, adding to queue...");
        }
      }
      /*}else if (parsedRead[0].equals("disconnect")) { // Leave queue.
        if(clients.remove(new Client(remoteIP))){
          System.out.println(remoteIP + " was removed from queue.");
        }
      }*/

      /*if (parsedRead[0].equals("connect")) {
        Client c = new Client(remoteIP, remotePort, Double.parseDouble(parsedRead[1]), Double.parseDouble(parsedRead[2]));
        Client cMatch = findMatch(c);
        if (cMatch != null) { //Match is found, send back ip to which it should connect to.
          clients.remove(cMatch);
          out.println(cMatch.getRemoteIP() + " " + cMatch.getRemotePort());
          System.out.println("found match!");
        }else { // No match is found, add self to queue.
          clients.add(c); //s.getInetAddress().getHostAddress()
          System.out.println("Found no match, adding to queue...");
        }
      }else if (parsedRead[0].equals("disconnect")) { // Leave queue.
        if(clients.remove(new Client(remoteIP))){
          System.out.println(remoteIP + " was removed from queue.");
        }
      }*/

      System.out.println("strangers in queue: " + clients.size());
      //s.close();
    }
  }

  private Client findMatch(Client client){
    for (Client c: clients ) {
      if (c.distance(client) <= 10) { //TODO: Change to dynamic value
        return c;
      }
    }
    return null;
  }

  /*class CommunicationThread extends Thread {
      PrintWriter out;
      BufferedReader in;
      public CommunicationThread(PrintWriter out, BufferedReader in){
        this.out = out;
        this.in = in;
      }
      @Override
      public void run(){
        String read;
        try {
          while(read = in.readLine() != null){
            out.println(read);
          }
        }catch(IOException e){
          e.printStackTrace();
        }

      }
  }*/

  class CommunicationThread extends Thread {
      Socket sOut;
      Socket sIn;
      PrintWriter out;
      BufferedReader in;
      public CommunicationThread(Client cOut, Client cIn){
        this.sOut = cOut.getSocket();
        this.sIn = cIn.getSocket();
        try {
          this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sOut.getOutputStream())), true);
          this.in = cIn.getInStream();//new BufferedReader(new InputStreamReader(sIn.getInputStream()));
          this.out.println("ready");
        }catch(IOException e){
          e.printStackTrace();
        }
      }
      @Override
      public void run(){
        String read;
        try {
          while((read = in.readLine()) != null){
            out.println(read);
          }
        }catch(IOException e){
          e.printStackTrace();
        }
        try {
          sOut.close();
          sIn.close();
        }catch(IOException e){
          e.printStackTrace();
        }

      }
  }

}
