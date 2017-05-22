import java.net.*; // need this for InetAddress, Socket, ServerSocket
import java.io.BufferedReader;

public class Client{
  Socket socket;
  BufferedReader in;
  double latitude1;
  double longitude1;
  double latitude2;
  double longitude2;
  double acceptedDistance = 10;

  public Client(Socket s, BufferedReader in, double latitude, double longitude){
      this.socket = s;
      this.in = in;
      this.latitude1 = latitude;
      this.longitude1 = longitude;
  }

  public Client(Socket s, BufferedReader in, double latitude1, double longitude1, double latitude2, double longitude2){
      this.socket = s;
      this.in = in;
      this.latitude1 = latitude1;
      this.longitude1 = longitude1;
      this.latitude2 = latitude2;
      this.longitude2 = longitude2;
  }

  public BufferedReader getInStream(){
    return in;
  }

  public Socket getSocket(){
    return socket;
  }

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
    double lat1 = c.latitude1;
    double lon1 = c.longitude1;
    double lat2 = latitude2;
    double lon2 = longitude2;
    double p = 0.017453292519943295;
    double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;
    double distance = 12742 * Math.asin(Math.sqrt(a));
    return distance;
    /*lat1 = c.latitude1;
    lon1 = c.longitude1;
    lat2 = latitude2;
    lon2 = longitude2;
    a = 0.5 - Math.cos((lat2 - lat1) * p)/2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;
    double distance2 = 12742 * Math.asin(Math.sqrt(a));
    return (distance1 < distance2 ? distance1 : distance2); */
  }

  public boolean isMatch(Client c){
    if (this.distance(c) < this.acceptedDistance) {
      return true;
    }else if (c.distance(this) <= c.acceptedDistance) {
      return true;
    }else {
      return false;
    }
  }
}
