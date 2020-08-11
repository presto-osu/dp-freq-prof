package presto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class MainLaplacePhdHybridRawTau extends MainLaplacePhdHybrid {

  MainLaplacePhdHybridRawTau(final String[] args) {
    super(args);
  }

  public static void main(final String[] args) {
    new MainLaplacePhdHybridRawTau(args).run();
  }

  @Override
  double getScale(int trial_num, Map<String, Double> U) {
    final List<Profile> allUsers = Lists.newArrayList(profiles);
    Collections.shuffle(allUsers);
    final int sampleSize = (int) (optInUsersPercent * profiles.size());
    List<Profile> optInUsers = allUsers.subList(0, sampleSize);

    final double hybridTau = Math.min(threshold, getTau(U, optInUsers));
    // System.out.println("hybrid_tau:\t" + hybridTau);
    hybridTaus[trial_num] = hybridTau;
    privateTaus[trial_num] = hybridTau;

    double scale = 2 * hybridTau / epsilon;
    // System.out.println("hybrid_laplace_scale:\t" + scale);
    return scale;
  }
}
