package presto;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

abstract class BaseMain extends ProfilePartition {
  String dir;
  String app;
  String funcListPath;
  Collection<Profile> profiles;
  Set<String> funcs;

  Profile realProfile;
  double epsilon;
  int trials;
  double threshold = 0.;
  String csvDir;

  Timer globalTimer = new Timer();

  double[] all_randomization_time_cost_per_user;

  String anim = "|/-\\";

  /**
   * @param trial_num
   * @return average cost per user
   */
  abstract double runTrial(int trial_num);

  BaseMain(String[] args) {
    parseParams(args);
    init();
    String partition = System.getenv("PARTITION");
    if (partition != null && Integer.parseInt(partition) != 0) {
      int size = Integer.parseInt(partition);
      System.out.println("Partitioning... Size: " + size);

      Set<Profile> highSimCluster = createCluster(profiles, funcs, true, Math.abs(size));
      Set<Profile> lowSimCluster = createCluster(profiles, funcs, false, Math.abs(size));

      System.out.println("high_sim_size\t" + highSimCluster.size());
      System.out.println("low_sim_size\t" + lowSimCluster.size());
      System.out.println("high_sim_avg_dist\t" + averageClusterDistance(highSimCluster, funcs));
      System.out.println("low_sim_avg_dist\t" + averageClusterDistance(lowSimCluster, funcs));

      long numProfInHi = profiles.parallelStream().filter(p -> highSimCluster.contains(p)).count();
      long numProfInLo = profiles.parallelStream().filter(p -> lowSimCluster.contains(p)).count();
      System.out.println("num_prof_in_high_sim\t" + numProfInHi);
      System.out.println("num_prof_in_low_sim\t" + numProfInLo);

      profiles = size < 0 ? lowSimCluster : highSimCluster;
      realProfile = aggregate(profiles.parallelStream());
    }
  }

  void parseParams(String[] a) {
    dir = a[0];
    app = dir.split(File.separator)[dir.split(File.separator).length - 1];
    funcListPath = a[1];
    epsilon = Double.parseDouble(a[2]);
    trials = 1;
    if (a.length > 3 && !a[3].isEmpty())
      trials = Integer.parseInt(a[3]);
    if (a.length > 4 && !a[4].isEmpty())
      threshold = Double.parseDouble(a[4]);
    if (a.length > 5 && !a[5].isEmpty())
      csvDir = a[5];
  }

  CSVParser parser = new CSVParserBuilder().withSeparator('\t').withQuoteChar(' ').build();

  void dumpProfile(Profile p, String fileName) {
    try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(fileName)).withParser(parser).build()) {
      for (Map.Entry<String, Long> e : p.funcProfiles.entrySet()) {
        String[] line = new String[] { e.getKey(), e.getValue().toString() };
        writer.writeNext(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  Profile readProfile(String fileName) {
    Profile profile = new Profile();
    try (CSVReader reader = new CSVReaderBuilder(new FileReader(fileName)).withCSVParser(parser).build()) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        profile.put(nextLine[0].trim(), Long.parseLong(nextLine[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return profile;
  }

  void init() {
    funcs = new HashSet<>();
    for (Func f : Utils.readFunctionsFromJson(funcListPath))
      funcs.add(f.sig);
    profiles = Utils.readProfilesFromJson(dir, funcs);
    realProfile = aggregate(profiles.parallelStream());

    if (threshold < 0)
      threshold = funcs.size() * 5.;
    else
      threshold = Math.min(threshold, funcs.size() * 5.);

    // dump ground truth
    // dumpProfile(realProfile, String.format("csv/%s-groundtruth.csv", app));
    // System.exit(0);

    // sanity check
    // long limit = 5 * funcs.size();
    // for (Profile p : profiles) {
    // if (p.getTotalFreq() < limit) {
    // System.out.println(
    // "\t" + dir + " exceed: " + p.jf.getName() + " " + p.getTotalFreq() + " < " +
    // limit);
    // }
    // }
    // System.exit(0);
  }

  void run() {
    System.out.print("Running " + trials + " trials with epsilon=");
    System.out.printf("%.3f", epsilon);
    System.out.print(" and threshold=" + threshold);
    System.out.println();

    all_randomization_time_cost_per_user = new double[trials];

    globalTimer.reset();
    IntStream.range(0, trials).parallel()
        // .sequential()
        .forEach(new IntConsumer() {
          @Override
          public void accept(int trailNum) {
            System.out.print(".");
            all_randomization_time_cost_per_user[trailNum] = runTrial(trailNum);
          }
        });
    stats();
  }

  void stats() {
    System.out.println("\nApp: " + dir);
    System.out.println("V:\t" + funcs.size());
    // double[] countArr =
    // profiles.parallelStream().mapToDouble(Profile::getTotalFreq).toArray();
    // Statistics sCount = new Statistics(countArr);
    // System.out.println(
    // "count: " + sCount.getMin() + " " + sCount.getMax() + " " +
    // sCount.getMean());

    System.out.println("Duration:\t" + (globalTimer.duration() / 1000.0) + " sec");

    System.out.println(
        "time(ms):\t" + Arrays.stream(all_randomization_time_cost_per_user).parallel().average().getAsDouble());
  }

  Profile aggregate(Stream<Profile> profiles) {
    Profile ret = new Profile();
    for (String sig : funcs)
      ret.add(sig, 0L);
    profiles.forEach(p -> p.funcProfiles.forEach(ret::add));
    System.out.println(ret.getTotalFreq() + " events aggregated in real profiles.");
    return ret;
  }
}
