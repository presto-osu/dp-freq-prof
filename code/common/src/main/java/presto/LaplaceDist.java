package presto;

public interface LaplaceDist {
  default double getLaplace(double scale) {
    double e1 = -scale * Math.log(1 - Math.random());
    double e2 = -scale * Math.log(1 - Math.random());
    return e1 - e2;
  }

  static void main(String[] args) {
    double scale = 1 / Math.log(9);
    class A implements LaplaceDist {
    }

    for (int i = 0; i < 1000; i++) {
      System.out.println(new A().getLaplace(scale));
    }
  }
}
