public class Test {
  static public void main(String args[]) {
    Client c1 = new Client("lol", 12, 10);
    Client c2 = new Client("lol2", 10, 12);
    double d = c1.distance(c2);
    System.out.println(String.valueOf(d));
  }

}
