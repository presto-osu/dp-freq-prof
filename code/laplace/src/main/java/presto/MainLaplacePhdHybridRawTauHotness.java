package presto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Doubles;

public class MainLaplacePhdHybridRawTauHotness extends MainLaplacePhdHybridRawTau {
  long eta;
  double h;

  MainLaplacePhdHybridRawTauHotness(final String[] args) {
    super(args);
  }

  public static void main(final String[] args) {
    new MainLaplacePhdHybridRawTauHotness(args).run();
  }

  @Override
  void parseParams(String[] a) {
    super.parseParams(a);
    double alpha = Double.parseDouble(a[9]);
    eta = (long) Math.floor(alpha * 5);
    System.out.println("eta:\t" + eta);
    h = Integer.parseInt(a[10]) / 100.0;
    System.out.println("h:\t" + h);
  }

  @Override
  String getIntermediateDirStoringDifficulty() {
    Map<String, String> env = System. getenv();
    String intermediateDir = env.get("FAST_STORAGE");
    if (intermediateDir == null) intermediateDir = ".";
    return intermediateDir + "/hybrid-le-difficulty-hotness-eta" + eta;
  }

  @Override
  double getTau(Map<String, Double> U, List<Profile> optInUsers) {
    for (final String e : funcs) {
      double max_diff = -1;
      for (final Profile f : optInUsers) {
        if (f.get(e) > eta) {
          double diff_v = getDifficulty(f, e);
          // double diff_v = computeDifficultyInternal(f.funcProfiles, e);
          if (diff_v > max_diff)
            max_diff = diff_v;
        }
      }
      if (max_diff > 0) {
        U.put(e, max_diff);
      }
    }
    final double[] difficulties = Doubles.toArray(U.values());
    Arrays.sort(difficulties);
    // for (double d : difficulties) {
    // System.out.println("difficulty:\t" + d);
    // }
    // System.out.println("max_diff:\t" + difficulties[ difficulties.length - 1]);
    final int idx = (int) (difficulties.length * getProtectionPercentile());
    // System.out.println("selected_tau:\t" + difficulties[Math.min(idx, difficulties.length - 1)]);
    return difficulties[Math.min(idx, difficulties.length - 1)];
  }

  @Override
  double computeDifficultyInternal(Map<String, Long> m, String e) {
    System.out.print("+\b");
    return LPUtils.computeDifficulty(m, e, constraintsGraph, eta);
  }

  @Override
  double getProtectionPercentile() {
    return h;
  }
}
