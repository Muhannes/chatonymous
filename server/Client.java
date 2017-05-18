import java.net.*; // need this for InetAddress, Socket, ServerSocket
import java.io.BufferedReader;

public class Client{
  Socket socket;
  BufferedReader in;
  double latitude;
  double longitude;
  //Changed!!!
  public Client(Socket s, BufferedReader in, double latitude, double longitude){
      this.socket = s;
      this.in = in;
      this.latitude = latitude;
      this.longitude = longitude;
  }

  public Client(String remoteIP){ //only for disconnecting
    //this.remoteIP = remoteIP;
  }

  public BufferedReader getInStream(){
    return in;
  }

  public Socket getSocket(){
    return socket;
  }
  //Changed!!!
  public boolean equals(Object o){
    Client c = (Client) o;
    if (socket.equals(c.getSocket())) {
      return true;
    }else {
      return false;
    }
  }

  /*
  returns the distance
  */
  public double distance(Client c){
    double lat1 = c.latitude;
    double lon1 = c.longitude;
    double lat2 = latitude;
    double lon2 = longitude;
    double p = 0.017453292519943295;
    double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;
    return 12742 * Math.asin(Math.sqrt(a)); //2*R*asin
  }
}
