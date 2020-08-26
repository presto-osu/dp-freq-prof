package presto;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import com.google.common.collect.Maps;

public abstract class MainRR extends BaseMain {

  MainRR(String[] args) {
    super(args);
  }

  @Override
  double runTrial(int trial_num) {
    Profile servProfile = new Profile();
    long[] time_cost = new long[profiles.size()];

    Map<String, Double> U = Maps.newHashMap();
    double z = getZ(trial_num, U);
    double p = z / (1 + z);

    if (!Paths.get(intermediateResFile(trial_num) + "-after-randomization.csv").toFile().exists()) {
      AtomicInteger idx = new AtomicInteger();
      profiles.parallelStream().forEach(profile -> {
        time_cost[idx.getAndIncrement()] = timedRandomizedResponse(profile, servProfile, p);
      });
      dumpProfile(servProfile, intermediateResFile(trial_num) + "-after-randomization.csv");
    }

    postProcessing(trial_num, servProfile, z);

    return Arrays.stream(time_cost).parallel().average().getAsDouble();
  }

  double getZ(int trial_num, Map<String, Double> U) {
    return Math.exp(epsilon / (threshold * 2));
  }

  Profile postProcessing(int trial_num, Profile servProfile, double z) {
    Profile adjProfile = new Profile();

    if (!Paths.get(intermediateResFile(trial_num) + "-after-postprocessing.csv").toFile().exists()) {
      servProfile = readProfile(intermediateResFile(trial_num) + "-after-randomization.csv");
      long totalFreq = realProfile.getTotalFreq();
      // System.out.println("total frequency of servProfile: " + totalFreq);
      if (totalFreq != 5 * funcs.size() * profiles.size()) {
        throw new RuntimeException("BAD in PostProcessing!!!!!!!!!");
      }

      for (String mmm : funcs) {
        double x = servProfile.get(mmm);
        long estimate = Math.round(((z + 1) * x - totalFreq) / (z - 1));
        // System.out.println("serv: " + x + ", adj: " + estimate);
        // if (estimate < 0) estimate = 0;
        // if (estimate > totalFreq) estimate = totalFreq;
        adjProfile.add(mmm, estimate);
      }
      dumpProfile(adjProfile, intermediateResFile(trial_num) + "-after-postprocessing.csv");
    }
    return adjProfile;
  }

  String intermediateResFile(int trial_num) {
    return String.format("%s/%s-%.2f-%d-%d", csvDir, app, epsilon, (int) threshold, trial_num);
  }

  long timedRandomizedResponse(Profile profile, Profile servProfile, double p) {
    Timer timer = new Timer();
    timer.reset();
    randomizedResponse(profile, servProfile, p);
    return timer.duration();
  }

  abstract void randomizedResponse(Profile profile, Profile servProfile, double p);
}
