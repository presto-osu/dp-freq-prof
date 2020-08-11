package presto;

public class MainLaplacePhd extends MainLaplace implements PercentileDifficulty {
  protected String constraintsCSVFile;
  protected double percentile;

  MainLaplacePhd(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new MainLaplacePhd(args).run();
  }

  @Override
  void parseParams(String[] a) {
    super.parseParams(a);
    if (a.length > 6 && !a[6].isEmpty())
      constraintsCSVFile = a[6];
    if (a.length > 7 && !a[7].isEmpty())
      percentile = Double.parseDouble(a[7]);
    // if (percentile < 0. || percentile > 1.) Logger.err("Percentile has to be
    // between 0 and 1.");
    System.out.println("percentile:\t" + percentile);
  }

  double getProtectionPercentile() {
    return percentile;
  }

  @Override
  void init() {
    super.init();
    System.out.println("limit:\t" + funcs.size() * 5);
    // double[] difficulties = readSortedDifficulties(funcs.size(),
    // constraintsCSVFile);
    // int idx = (int) (difficulties.length * getProtectionPercentile());
    // threshold = difficulties[Math.min(idx, difficulties.length - 1)];
    threshold = funcs.size() * 5.;
    System.out.println("tau:\t" + threshold);
  }

  @Override
  String intermediateResFile(int trial_num) {
    return String.format("%s/%s-%.2f-%.2f-%d", csvDir, app, epsilon, percentile, trial_num);
  }
}
