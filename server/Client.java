
public class Client{
  String ip;
  double latitude;
  double longitude;

  public Client(String ip, double latitude, double longitude){
      this.ip = ip;
      this.latitude = latitude;
      this.longitude = longitude;
  }

  public Client(String ip){ //only for disconnecting
    this.ip = ip;
  }

  public String getIP(){
    return ip;
  }


  public boolean equals(Object o){
    Client c = (Client) o;
    if (ip.equals(c.getIP())) {
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
