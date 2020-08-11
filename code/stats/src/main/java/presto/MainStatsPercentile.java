package presto;

public class MainStatsPercentile extends MainStats {
  private double percentile;

  MainStatsPercentile(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new MainStatsPercentile(args).run();
  }

  @Override
  void parseParams(String[] a) {
    super.parseParams(a);
    percentile = threshold;
    System.out.println("percentile:\t" + percentile);
  }

  @Override
  String intermediateResFile(int trial_num) {
    return String.format(
      "%s/%s-%.2f-%.2f-%d-after-%s.csv",
      csvDir, app, epsilon, percentile, trial_num, after);
  }
}
