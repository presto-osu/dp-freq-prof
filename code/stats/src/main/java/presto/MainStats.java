package presto;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainStats extends BaseMain {
  protected String after;

  MainStats(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new MainStats(args).run();
  }

  double[] all_total_freq_est;
  double[] all_total_freq_est_diff;
  double[] all_linf;
  double[] all_rel_linf;
  double[] all_rel_linf_miss;
  double[] all_rel_diffs;
  double[] all_wjac;
  double[] all_l1;
  double[] all_ne;
  double[] all_real_l1;
  double[] all_norm_l1;
  double[][] all_toptau_precision;
  double[][] all_toptau_recall;
  double[][] all_toptau_f_score;
  double[][] all_hot_node_coverage;
  double[] all_mean_squared_error;
  double[][] all_hot_total_freq_real;
  double[][] all_hot_total_freq_est;
  double[][] all_hot_total_freq_diff;
  double[][] all_non_hot_total_freq_real;
  double[][] all_non_hot_total_freq_est;
  double[][] all_non_hot_total_freq_diff;
  double[][] all_hot_num_real;
  double[][] all_hot_num_est;
  double[][] all_hot_num_diff;
  double[][] all_hot_norm_l1;
  double[][] all_hot_l1;
  Map<String, double[]> all_mtd_freq_dist;

  @Override
  void parseParams(String[] a) {
    super.parseParams(a);
    after = a[6];
    System.out.printf("Reading from '%s/*-after-%s.csv'\n", csvDir, after);
  }

  @Override
  void init() {
    super.init();
    all_total_freq_est = new double[trials];
    all_total_freq_est_diff = new double[trials];
    all_linf = new double[trials];
    all_rel_linf = new double[trials];
    all_rel_linf_miss = new double[trials];
    all_rel_diffs = new double[trials];
    all_wjac = new double[trials];
    all_l1 = new double[trials];
    all_ne = new double[trials];
    all_real_l1 = new double[trials];
    all_norm_l1 = new double[trials];
    all_toptau_precision = new double[5][trials];
    all_toptau_recall = new double[5][trials];
    all_toptau_f_score = new double[5][trials];
    all_hot_node_coverage = new double[5][trials];
    all_mean_squared_error = new double[trials];
    all_hot_total_freq_real = new double[5][trials];
    all_hot_total_freq_est = new double[5][trials];
    all_hot_total_freq_diff = new double[5][trials];
    all_non_hot_total_freq_real = new double[5][trials];
    all_non_hot_total_freq_est = new double[5][trials];
    all_non_hot_total_freq_diff = new double[5][trials];
    all_hot_num_real = new double[5][trials];
    all_hot_num_est = new double[5][trials];
    all_hot_num_diff = new double[5][trials];
    all_hot_norm_l1 = new double[5][trials];
    all_hot_l1 = new double[5][trials];
    all_mtd_freq_dist = Maps.newConcurrentMap();
    for (String m : funcs) all_mtd_freq_dist.put(m, new double[trials]);
  }

  @Override
  double runTrial(int trial_num) {
    Map<String, Double> adjProfile = Maps.newHashMap();
    try (CSVReader reader =
        new CSVReaderBuilder(
                new FileReader(
                  intermediateResFile(trial_num)))
            .withCSVParser(parser)
            .build()) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        adjProfile.put(nextLine[0].trim(), Double.parseDouble(nextLine[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    //    if (after.equals("randomization")) adjProfile = postProcessing(adjProfile);
    computeAccuracy(trial_num, adjProfile);
    return 0.;
  }

  String intermediateResFile(int trial_num) {
    return String.format(
      "%s/%s-%.2f-%d-%d-after-%s.csv",
      csvDir, app, epsilon, (int) threshold, trial_num, after);
  }

  //  Map<String, Double> postProcessing(Map<String, Double> servProfile) {
  //    Map<String, Double> adjProfile = Maps.newHashMap();
  //
  //    long totalFreq = realProfile.getTotalFreq();
  //    //    System.out.println("total frequency of servProfile: " + totalFreq);
  //    if (totalFreq != 5 * funcs.size() * 1000) {
  //      throw new RuntimeException("BAD in PostProcessing!!!!!!!!!");
  //    }
  //    double z = Math.exp(epsilon / (threshold * 2));
  //    for (String mmm : funcs) {
  //      double x = servProfile.get(mmm);
  //      double estimate = Math.round(((z + 1) * x - totalFreq) / (z - 1));
  //      //      System.out.println("serv: " + x + ", adj: " + estimate);
  //      //      if (estimate < 0) estimate = 0;
  //      //      if (estimate > totalFreq) estimate = totalFreq;
  //      adjProfile.put(mmm, estimate);
  //    }
  //    return adjProfile;
  //  }

  @Override
  void stats() {
    super.stats();

    // Statistics stotalfreq = new Statistics(all_total_freq_est);
    // System.out.println("mean_total_freq_est:\t" + stotalfreq.getMean());
    // System.out.println("error_total_freq_est:\t" + stotalfreq.getConfidenceInterval95());
    // Statistics stotalfreqdiff = new Statistics(all_total_freq_est_diff);
    // System.out.println("mean_total_freq_est_diff:\t" + stotalfreqdiff.getMean());
    // System.out.println("error_total_freq_est_diff:\t" + stotalfreqdiff.getConfidenceInterval95());

    Statistics slinf = new Statistics(all_linf);
    Statistics srellinf = new Statistics(all_rel_linf);
    Statistics srellinfmiss = new Statistics(all_rel_linf_miss);
    Statistics swjac = new Statistics(all_wjac);
    Statistics sMSE = new Statistics(all_mean_squared_error);
    Statistics sl1 = new Statistics(all_l1);
    Statistics sne = new Statistics(all_ne);
    Statistics sreall1 = new Statistics(all_real_l1);
    Statistics snorml1 = new Statistics(all_norm_l1);

    // System.out.println("mean_linf:\t" + slinf.getMean());
    // System.out.println("error_linf:\t" + slinf.getConfidenceInterval95());
    // System.out.println("mean_rel_linf:\t" + srellinf.getMean());
    // System.out.println("error_rel_linf:\t" + srellinf.getConfidenceInterval95());
    // System.out.println("mean_rel_linf_miss:\t" + srellinfmiss.getMean());
    // System.out.println("error_rel_linf_miss:\t" + srellinfmiss.getConfidenceInterval95());
    // System.out.println("mean_l1:\t" + sl1.getMean());
    // System.out.println("error_l1:\t" + sl1.getConfidenceInterval95());
    System.out.println("mean_ne:\t" + sne.getMean());
    System.out.println("error_ne:\t" + sne.getConfidenceInterval95());
    // System.out.println("mean_real_l1:\t" + sreall1.getMean());
    // System.out.println("error_real_l1:\t" + sreall1.getConfidenceInterval95());
    // System.out.println("mean_norm_l1:\t" + snorml1.getMean());
    // System.out.println("error_norm_l1:\t" + snorml1.getConfidenceInterval95());
    // System.out.println("mean_wjac:\t" + swjac.getMean());
    // System.out.println("error_wjac:\t" + swjac.getConfidenceInterval95());
    // System.out.println("mean_mse:\t" + sMSE.getMean());
    // System.out.println("error_mse:\t" + sMSE.getConfidenceInterval95());

    // Statistics sreldiff = new Statistics(all_rel_diffs);
    // System.out.println("mean_rel_diff_mean:\t" + sreldiff.getMean());
    // System.out.println("error_rel_diff_mean:\t" + sreldiff.getConfidenceInterval95());

    // statsHot(all_hot_node_coverage, "hotcov");
    // statsHot(all_hot_num_est, "hot_num_est");
    // statsHot(all_hot_num_real, "hot_num_real");
    // statsHot(all_hot_num_diff, "hot_num_diff");
    // statsHot(all_hot_total_freq_real, "hot_total_freq_real");
    // statsHot(all_hot_total_freq_est, "hot_total_freq_est");
    // statsHot(all_hot_total_freq_diff, "hot_total_freq_diff");
    // statsHot(all_non_hot_total_freq_real, "non_hot_total_freq_real");
    // statsHot(all_non_hot_total_freq_est, "non_hot_total_freq_est");
    // statsHot(all_non_hot_total_freq_diff, "non_hot_total_freq_diff");
    // statsHot(all_hot_norm_l1, "hot_norm_l1");
    // statsHot(all_hot_l1, "hot_l1");

    //    meanSB = new StringBuffer("mean_precision:");
    //    errorSB = new StringBuffer("error_precision:");
    //    for (int idx = 0; idx < 5; idx += 1) {
    //      Statistics sPrecision = new Statistics(all_toptau_precision[idx]);
    //      //    Statistics sRecall = new Statistics(all_toptau_recall);
    //      //    Statistics sFScore = new Statistics(all_toptau_f_score);
    //      meanSB.append("\t").append(sPrecision.getMean());
    //      errorSB.append("\t").append(sPrecision.getConfidenceInterval95());
    //    }
    //    System.out.println(meanSB);
    //    System.out.println(errorSB);

    Map.Entry<String, Long> e =
        realProfile
            .funcProfiles
            .entrySet()
            .parallelStream()
            .max(Map.Entry.comparingByValue())
            .get();
    System.out.println(
        "max_freq:\t"
            + e.getValue().doubleValue() / realProfile.getTotalFreq()
            + "\t"
            + realProfile.getTotalFreq()
            + "\t"
            + realProfile.funcProfiles.entrySet().stream()
                .filter(entry -> funcs.contains(entry.getKey()))
                .mapToLong(entry -> entry.getValue().longValue())
                .sum());
    {
      double numP = 0;
      for (Profile p : profiles) for (String s : funcs) if (p.get(s) <= threshold) numP += 1;
      double numMtd = numP / profiles.size();
      //      for (String s : funcs) if (realProfile.get(s) <= threshold) numMtd += 1;
      System.out.println("threshold:\t" + threshold);
      System.out.println("mtd_pre_abs:\t" + numMtd / funcs.size() + "\t" + numMtd);
    }
    {
      String[] funcs = this.funcs.toArray(new String[0]);
      Arrays.sort(funcs);
      double sum_freq = realProfile.getTotalFreq();
      StringBuilder sb = new StringBuilder();
      for (String m : funcs) sb.append(realProfile.get(m) / sum_freq).append(",");
//      System.out.println("mtd_dist:\t" + sb);
    }
    {
      String[] funcs = this.funcs.toArray(new String[0]);
      Arrays.sort(funcs);
      StringBuilder sb = new StringBuilder();
      for (String m : funcs)
        sb.append(Arrays.stream(all_mtd_freq_dist.get(m)).average().getAsDouble()).append(",");
//      System.out.println("mtd_dist_est:\t" + sb);
      //
      //      sb = new StringBuilder();
      //      for (String m : funcs)
      //        sb.append(
      //                Math.abs(
      //                    realProfile.get(m)
      //                        - Math.round(
      //                            Arrays.stream(all_mtd_freq_dist.get(m)).average().getAsDouble()
      //                                * sum_freq)))
      //            .append(",");
      //      System.out.println("mtd_diff_dist:\t" + sb);
    }

    //    double[] percentages = new double[] {0.25, 0.5, 0.75};
    //    for (double j : percentages) {
    //      long c =
    //          realProfile
    //              .funcProfiles
    //              .entrySet()
    //              .parallelStream()
    //              .filter(e1 -> e1.getValue().doubleValue() > j * realProfile.getTotalFreq())
    //              .count();
    //      System.out.printf("freq>%.2f: %f\n", j, (double) c / funcs.size());
    //    }
  }

  void statsHot(double[][] data, String tag) {
    StringBuffer meanSB = new StringBuffer("mean_").append(tag).append(":");
    StringBuffer errorSB = new StringBuffer("error_").append(tag).append(":");
    for (int idx = data.length - 1; idx >= 0; idx -= 1) {
      Statistics sHot = new Statistics(data[idx]);
      meanSB.append("\t").append(sHot.getMean());
      errorSB.append("\t").append(sHot.getConfidenceInterval95());
    }
    System.out.println(meanSB);
    System.out.println(errorSB);
  }

  double rel(long x, long y) {
    return ((double) x) / ((double) y);
  }

  double rel(double x, long y) {
    return x / ((double) y);
  }

  double rel(double x, double y) {
    return x / y;
  }

  void computeAccuracy(int trial_num, Map<String, Double> adjProfile) {
    all_total_freq_est[trial_num] = adjProfile.values().stream().reduce(Double::sum).get();
    all_total_freq_est_diff[trial_num] =
        Math.abs(adjProfile.values().stream().reduce(Double::sum).get() - 1000 * 5 * funcs.size());
    for (int idx = 0; idx < 5; idx += 1) {
      double tau = idx * 0.25;
      //      Set<String> trueTopTau =
      //          Sets.newHashSet(realProfile.topKFrequent(Math.round(tau * funcs.size())));
      //      Set<String> predTopTau =
      //          Sets.newHashSet(adjProfile.topKFrequent(Math.round(tau * funcs.size())));
      //      //    System.out.println(trueTopTau.size());
      //      //    System.out.println(predTopTau.size());
      //      int tp = 0;
      //      int fp = 0;
      //      int fn = 0;
      //      for (String v : predTopTau)
      //        if (trueTopTau.contains(v)) tp += 1;
      //        else fp += 1;
      //      for (String v : trueTopTau) if (!predTopTau.contains(v)) fn += 1;
      //      //    System.out.println("tp=" + tp + " fp=" + fp + " fn=" + fn);
      //      double precision = (double) tp / (tp + fp);
      //      double recall = (double) tp / (tp + fn);
      //      all_toptau_precision[idx][trial_num] = precision;
      //      all_toptau_recall[idx][trial_num] = recall;
      //      all_toptau_f_score[idx][trial_num] = 2 * precision * recall / (precision + recall);

      // hot-node coverage
      Set<Map.Entry<String, Long>> real_hot = realProfile.hot(tau, funcs);
      Set<Map.Entry<String, Double>> adj_hot = Sets.newHashSet();
      double max = tau * adjProfile.values().parallelStream().max(Double::compareTo).get();
      for (Map.Entry<String, Double> e : adjProfile.entrySet())
        if (e.getValue() >= max) adj_hot.add(e);

      Set<String> real_hot_mtd =
          real_hot.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
      Set<String> adj_hot_mtd = adj_hot.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
      Sets.SetView<String> intersect = Sets.intersection(real_hot_mtd, adj_hot_mtd);
      all_hot_node_coverage[idx][trial_num] = (double) intersect.size() / real_hot_mtd.size();

      all_hot_num_real[idx][trial_num] = real_hot_mtd.size();
      all_hot_num_est[idx][trial_num] = adj_hot_mtd.size();
      all_hot_num_diff[idx][trial_num] = Math.abs(real_hot_mtd.size() - adj_hot_mtd.size());

      all_hot_total_freq_est[idx][trial_num] =
          adj_hot.stream().mapToDouble(Map.Entry::getValue).sum();
      all_hot_total_freq_real[idx][trial_num] =
          adj_hot_mtd.stream().mapToDouble(m -> realProfile.get(m)).sum();
      all_hot_total_freq_diff[idx][trial_num] =
          adj_hot.stream()
              .mapToDouble(e -> Math.abs(e.getValue() - realProfile.get(e.getKey())))
              .sum();

      Set<String> mtds = Sets.newHashSet(funcs);
      Sets.SetView<String> nonHotMtds = Sets.difference(mtds, adj_hot_mtd);
      all_non_hot_total_freq_diff[idx][trial_num] =
          nonHotMtds.stream()
              .mapToDouble(m -> Math.abs(realProfile.get(m) - adjProfile.get(m)))
              .sum();
      all_non_hot_total_freq_real[idx][trial_num] =
          nonHotMtds.stream().mapToDouble(m -> realProfile.get(m)).sum();
      all_non_hot_total_freq_est[idx][trial_num] =
          nonHotMtds.stream().mapToDouble(m -> adjProfile.get(m)).sum();

      //    System.out.println("------         real: " + real_hot.size());
      //    System.out.println("------          adj: " + adj_hot.size());
      //    System.out.println("------ intersection: " + intersect.size());
      //    for (Map.Entry<String, Long> e : real_hot) System.out.println("--- real: " + e);
      //    for (Map.Entry<String, Long> e : adj_hot) System.out.println("+++  adj: " + e);
      //    for (String m : intersect) System.out.println("***  int: " + m);

      long real_sum = realProfile.getTotalFreq();
      double l1 = 0.;
      double x_sum = 0.;
      for (String mmm : real_hot_mtd) {
        // double x = rel(realProfile.get(mmm), real_sum);
        double x = realProfile.get(mmm);
        x_sum += x;
        // double y = Utils.clip(rel(adjProfile.get(mmm), real_sum), 0, 1);
        double y = Utils.clip(adjProfile.get(mmm), 0, real_sum);
        double diff = Math.abs(x - y);
        l1 += diff;
      }
      //      System.out.println("!!!! " + tau + " ::::: " + x_sum);
      all_hot_norm_l1[idx][trial_num] = l1 / real_hot_mtd.size() / x_sum;
      all_hot_l1[idx][trial_num] = l1 / x_sum;
    }

    //    for (String m : funcs)
    //      System.out.println("--- " + m + " real:" + realProfile.get(m) + " adj:" +
    // adjProfile.get(m) + " diff:" + (realProfile.get(m) - adjProfile.get(m)));

    // accuracy: L infinity
    long real_sum = realProfile.getTotalFreq();
    double adj_sum = adjProfile.values().stream().reduce(Double::sum).get();
    Map<String, Double> clippedAdjProfile = Maps.newHashMap();
    for (String mmm : funcs) {
      double y = Utils.clip(rel(adjProfile.get(mmm), real_sum), 0, 1);
      clippedAdjProfile.put(mmm, y);
    }
    double clippedAdjSum = clippedAdjProfile.values().stream().reduce(Double::sum).get();
    //         System.out.println("\nReal sum: " + real_sum + ", adj sum: " + adj_sum);
    double linf = 0.;
    double rel_linf = 0.;
    double l1 = 0.;
    double real_l1 = 0.;
    double mse = 0.;
    double wjac1 = 0.;
    double wjac2 = 0.;
    int count = 0;
    double sum_x = 0.;
    double sum_y = 0.;
    List<Double> rel_diffs = Lists.newArrayList();
    for (String mmm : funcs) {
      double x = rel(realProfile.get(mmm), real_sum);
      sum_x += x;
      //      double y = Utils.clip(rel(adjProfile.get(mmm), real_sum), 0, 1);
      // double y = rel(clippedAdjProfile.get(mmm), clippedAdjSum);
      double y = rel(adjProfile.get(mmm), adj_sum);
      sum_y += y;
      //      double y = rel(adjProfile.get(mmm), real_sum);
      all_mtd_freq_dist.get(mmm)[trial_num] = y;
      // System.out.print(mmm + ": ");
      // System.out.printf("%.3f, %.3f\n", x, y);
      double diff = Math.abs(x - y);
      double realDiff = Math.abs(realProfile.get(mmm) - adjProfile.get(mmm));
      if (linf < diff) linf = diff;
      if (x > 0) {
        double rel_diff = diff / x;
        rel_diffs.add(rel_diff);
        if (rel_diff > rel_linf) {
          rel_linf = rel_diff;
        }
        //        if (rel_diff > 10)
        //          System.out.printf("\trel_linf_details:\t%.8f %.8f %.8f\n", rel_diff, x, y);
      } else if (y > 0) {
        count += 1;
      }
      l1 += diff;
      real_l1 += realDiff;
      mse += diff * diff;
      wjac1 += Math.min(x, y);
      wjac2 += Math.max(x, y);
    }
    all_linf[trial_num] = linf;
    all_rel_linf[trial_num] = rel_linf;
    all_rel_linf_miss[trial_num] = count;
    all_wjac[trial_num] = 1 - wjac1 / wjac2;
    all_l1[trial_num] = l1; // / sum_x;
    all_ne[trial_num] = l1 / 2;
    all_real_l1[trial_num] = real_l1;
    all_norm_l1[trial_num] = l1 / funcs.size(); // / sum_x;
    all_mean_squared_error[trial_num] = mse / funcs.size();
//    System.out.println("!!!   y:" + sum_y + "   x:" + sum_x);

    double[] tempArray = new double[rel_diffs.size()];
    int i = 0;
    for (Double d : rel_diffs) {
      tempArray[i] = d;
      i++;
    }
    all_rel_diffs[trial_num] = new Statistics(tempArray).getMean();
  }
}
