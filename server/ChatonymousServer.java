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
      String phoneIP = s.getInetAddress().getHostAddress();
      System.out.println("Connection from " + phoneIP); // Set up streams

      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
      PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
      String read = in.readLine(); //now assuming only one line!!
      System.out.println("Read: " + read);
      //parsedRead = [phoneIP, action] (ex: [0.0.0.0, connect])
      String[] parsedRead = read.split(" ");

      if (parsedRead[0].equals("connect")) {
        if (ips.peekFirst() != null) {
          String ip = ips.removeFirst(); //Should be changed to take based on location
          out.println(ip);
          System.out.println("found match!");
        }else {
          ips.add(phoneIP); //s.getInetAddress().getHostAddress()
          System.out.println("Found no match, adding to queue...");
        }
      }else if (parsedRead[0].equals("disconnect")) {
        if(ips.remove(phoneIP)){
          System.out.println(phoneIP + " was removed from queue.");
        }
      }

      System.out.println("strangers in queue: " + ips.size());
      System.out.println("Queue: " + ips.toString());
      s.close();
    }
  }

}
