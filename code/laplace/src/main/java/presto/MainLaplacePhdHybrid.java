package presto;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

import org.apache.commons.math3.util.Pair;

import net.openhft.chronicle.map.*;
import net.openhft.chronicle.core.values.*;
import net.openhft.chronicle.values.Values;

public class MainLaplacePhdHybrid extends MainLaplacePhd {
  protected double optInUsersPercent;
  // protected List<Profile> optInUsers;
  // protected List<Profile> regularUsers;
  // protected Map<String, Double> diffMap;
  protected ConstraintsGraph constraintsGraph;
  protected double[] hybridTaus;
  protected double[] privateTaus;
  List<Pair<String, String>> lePairs;
  ChronicleMap<IntValue, DoubleValue> persistentDifficultyMap;

  MainLaplacePhdHybrid(final String[] args) {
    super(args);
  }

  public static void main(final String[] args) {
    new MainLaplacePhdHybrid(args).run();
  }

  @Override
  void parseParams(final String[] a) {
    super.parseParams(a);
    if (a.length > 8 && !a[8].isEmpty())
      optInUsersPercent = Double.parseDouble(a[8]);
    System.out.println("optin:\t" + optInUsersPercent);
  }

  @Override
  void init() {
    super.init();
    hybridTaus = new double[trials];
    privateTaus = new double[trials];
    // diffMap = readDifficultyMap(funcs.size(), constraintsCSVFile);
    lePairs = Lists.newArrayList(LPUtils.readLEPairs(app, parser));
    System.out.println("num_lepairs:\t" + lePairs.size());
    constraintsGraph = ConstraintsGraph.genConstraintsGraph(funcs, lePairs);

    try {
      Path datFile = Paths.get(getIntermediateDirStoringDifficulty() + "_" + app + ".dat");
      System.out.println("persist_map:\t" + datFile.toAbsolutePath());
      persistentDifficultyMap = ChronicleMap.of(IntValue.class, DoubleValue.class).name("difficulty")
          .entries(funcs.size() * profiles.size()).createPersistedTo(datFile.toFile());
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }

  @Override
  void stats() {
    super.stats();
    System.out.println("hybrid_tau:\t" + Arrays.stream(hybridTaus).parallel().average().getAsDouble());
    // System.out.println("private_tau:\t" + Arrays.stream(privateTaus).parallel().average().getAsDouble());
    Statistics stau = new Statistics(privateTaus);
    System.out.println("mean_tau:\t" + stau.getMean());
    System.out.println("error_tau:\t" + stau.getConfidenceInterval95());
  }

  @Override
  double getScale(int trial_num, Map<String, Double> U) {
    final List<Profile> allUsers = Lists.newArrayList(profiles);
    Collections.shuffle(allUsers);
    final int sampleSize = (int) (optInUsersPercent * profiles.size());
    List<Profile> optInUsers = allUsers.subList(0, sampleSize);
    // regularUsers = allUsers.subList(sampleSize, allUsers.size());

    final double hybridTau = Math.min(threshold, getTau(U, optInUsers));
    // System.out.println("hybrid_tau:\t" + hybridTau);
    hybridTaus[trial_num] = hybridTau;

    double scale = threshold / epsilon;
    final double priv_tau = Utils.clip(hybridTau + getLaplace(scale), 0, threshold);
    // System.out.println("priv_tau:\t" + priv_tau);
    privateTaus[trial_num] = priv_tau;

    scale = 2 * priv_tau / epsilon;
    // System.out.println("hybrid_laplace_scale:\t" + scale);
    return scale;
  }

  double getTau(Map<String, Double> U, List<Profile> optInUsers) {
    // final double[] difficulties = new double[funcs.size()];
    for (final String e : funcs) {
      double max_diff = -1;
      for (final Profile f : optInUsers) {
        if (f.get(e) > 0) {
          double diff_v = getDifficulty(f, e);
          // double diff_v = computeDifficultyInternal(f.funcProfiles, e);
          if (diff_v > max_diff)
            max_diff = diff_v;
        }
      }
      // difficulties[i++] = max_diff;
      if (max_diff > 0) {
        U.put(e, max_diff);
      }
    }

    final double[] difficulties = Doubles.toArray(U.values());

    Arrays.sort(difficulties);
    // for (double d : difficulties) {
    // System.out.println("difficulty:\t" + d);
    // }
    final int idx = (int) (difficulties.length * getProtectionPercentile());
    // System.out.println("idx:\t" + idx);
    return difficulties[Math.min(idx, difficulties.length - 1)];
  }

  String getIntermediateDirStoringDifficulty() {
    Map<String, String> env = System. getenv();
    String intermediateDir = env.get("FAST_STORAGE");
    if (intermediateDir == null) intermediateDir = ".";
    return intermediateDir + "/hybrid-le-difficulty";
  }

  double computeDifficultyInternal(Map<String, Long> m, String e) {
    System.out.print("*\b");
    return LPUtils.computeDifficulty(m, e, constraintsGraph);
  }

  double getDifficulty(Profile f, String e) {
    double diff_v = -1;
    StringBuilder sb = new StringBuilder(app).append("-")
        .append(f.jf.getName().substring(0, f.jf.getName().length() - 5)).append("-").append(e.hashCode());
    int id = sb.toString().hashCode();
    IntValue key = Values.newHeapInstance(IntValue.class);
    key.setValue(id);
    try {
      diff_v = persistentDifficultyMap.get(key).getValue();
    } catch (NullPointerException e1) {
      // throw new RuntimeException(e1);
      diff_v = computeDifficultyInternal(f.funcProfiles, e);
      DoubleValue val = Values.newHeapInstance(DoubleValue.class);
      val.setValue(diff_v);
      persistentDifficultyMap.put(key, val);
    }
    return diff_v;
  }

  // @Override
  // void run() {
  // int[] e_idx = new int[1];
  // funcs.parallelStream().forEach(e -> {
  // e_idx[0] += 1;
  // int[] idx = new int[1];
  // profiles.stream().forEach(f -> {
  // String data = "\033[2K\rComputing... " + anim.charAt(idx[0]++ %
  // anim.length()) + " " + e_idx[0] + ":" + idx[0];
  // System.err.print(data);
  // if (f.get(e) > 0) {
  // StringBuilder sb = new StringBuilder(app).append("-")
  // .append(f.jf.getName().substring(0, f.jf.getName().length() -
  // 5)).append("-").append(e.hashCode());
  // int id = sb.toString().hashCode();
  // IntValue key = Values.newHeapInstance(IntValue.class);
  // key.setValue(id);
  // try {
  // // diff_v = persistentDifficultyMap.get(key).getValue();
  // double diff_v = computeDifficultyInternal(f.funcProfiles, e);
  // DoubleValue val = Values.newHeapInstance(DoubleValue.class);
  // val.setValue(diff_v);
  // persistentDifficultyMap.put(key, val);
  // } catch (NullPointerException e1) {
  // throw new RuntimeException();
  // // diff_v = computeDifficultyInternal(f.funcProfiles, e);
  // // DoubleValue val = Values.newHeapInstance(DoubleValue.class);
  // // val.setValue(diff_v);
  // // persistentDifficultyMap.put(key, val);
  // }
  // }
  // });
  // });
  // }
}
