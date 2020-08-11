package presto;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;

public class MainLaplace extends BaseMain implements LaplaceDist, SimplexProjection {
  MainLaplace(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new MainLaplace(args).run();
  }

  @Override
  double runTrial(int trial_num) {
    Profile servProfile = new Profile();
    long[] time_cost = new long[getProfiles().size()];

    String intFile = intermediateResFile(trial_num) + "-after-randomization.csv";
    if (!Paths.get(intFile).toFile().exists()) {
      Map<String, Double> U = Maps.newHashMap();
      AtomicInteger idx = new AtomicInteger();
      double scale = getScale(trial_num, U);
      getProfiles().parallelStream().forEach(profile -> {
        time_cost[idx.getAndIncrement()] = timedLaplace(profile, servProfile, scale);
      });
      dumpProfile(servProfile, intermediateResFile(trial_num) + "-after-randomization.csv");
    }
    
    postProcessing(trial_num, servProfile);

    return Arrays.stream(time_cost).average().getAsDouble();
  }

  double getScale(int trial_num, Map<String, Double> U) {
    return 2 * threshold / epsilon;
  }

  Collection<Profile> getProfiles() {
    return profiles;
  }

  Profile postProcessing(int trial_num, Profile servProfile) {
    String toCSV = intermediateResFile(trial_num) + "-after-qp.csv";
    if (Paths.get(toCSV).toFile().exists())
      return null;
    String fromCSV = intermediateResFile(trial_num) + "-after-randomization.csv";
    projectToSimplexCC20(app, trial_num, null, realProfile.getTotalFreq(), fromCSV, toCSV);
    return null;
  }

  String intermediateResFile(int trial_num) {
    return String.format("%s/%s-%.2f-%d-%d", csvDir, app, epsilon, (int) threshold, trial_num);
  }

  long timedLaplace(Profile profile, Profile servProfile, double scale) {
    Timer timer = new Timer();
    timer.reset();
    funcs.stream().forEach(m -> servProfile.add(m, Math.round(profile.get(m) + getLaplace(scale))));
    return timer.duration();
  }
}
