package presto;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.apache.commons.math3.util.Pair;

public class LPUtils {
  static Set<Pair<String, String>> readLEPairs(String app, CSVParser parser) {
    Set<Pair<String, String>> pairs = Sets.newHashSet();
    String lePairCSVName = Paths.get("le-pairs-csv", app + ".csv").toAbsolutePath().toString();
    try (CSVReader reader = new CSVReaderBuilder(new FileReader(lePairCSVName)).withCSVParser(parser).build()) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        pairs.add(new Pair<>(nextLine[0].trim(), nextLine[1].trim()));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return pairs;
  }

  /**
   * For simple constraints graph.
   */
  static long computeDifficulty(Map<String, Long> f, String e, ConstraintsGraph constraintsGraph) {
    // System.err.println("------- simple --------");
    // long diff_v = 0L;
    // int numNodes = 0;
    // for (String n : constraintsGraph.reachable(e)) {
    //   long freq = f.getOrDefault(n, 0L);
    //   if (freq > 0) {
    //     diff_v += freq - 0;
    //     numNodes += 1;
    //   }
    // }
    long diff_v = constraintsGraph.reachable(e).stream().mapToLong(v -> f.getOrDefault(v, 0L)).sum();

    // Set<String> reachableNodes = constraintsGraph.bfs(e, constraintsGraph.edges, Sets.newHashSet(), null);
    // StringBuilder sb = new StringBuilder("{");
    // for (String v : reachableNodes) {
    //   sb.append((v.hashCode() & 0xfffffff) + "(" + f.getOrDefault(v, 0L) + ")").append(", ");
    // }
    // sb.append("}");
    // System.out.println("simple_reachable:\t" + sb + "\t" + reachableNodes.size());
    // System.out.println("simple_diff_v:\t" + diff_v);

    // try {
    //   constraintsGraph.dump("simple.dot", e, reachableNodes, f);
    // } catch (IOException e1) {
    //   // TODO Auto-generated catch block
    //   e1.printStackTrace();
    // }
    return diff_v;
  }

  static long computeDifficulty(Map<String, Long> f, String e, ConstraintsGraph constraintsGraph, long eta) {
    // long diff_v = 0L;
    // int numNodes = 0;
    // for (String n : constraintsGraph.reachable(e)) {
    //   long freq = f.getOrDefault(n, 0L);
    //   if (freq > eta) {
    //     diff_v += freq - eta;
    //     numNodes += 1;
    //   }
    // }
    // return diff_v;
    long diff_v = constraintsGraph.reachable(e).stream().mapToLong(v -> {
      long freq = f.getOrDefault(v, 0L);
      if (freq > eta) return freq - eta;
      return 0L;
    }).sum();
    // System.out.println("freq:\t" + e + "\t" + diff_v);
    return diff_v;
  }

  /**
   * Compute difficulty using LP.
   * Very expensive.
   */
  static double computeDifficulty(final Map<String, Long> f, final String v, final List<List<String>> ieqs,
      final List<String> events, final long k) {
    // System.err.print("\nevent:\t" + v);

    // x and s
    // 1st half corresponds to x, 2nd half is for s
    MipVarArray.DataModel data = new MipVarArray.DataModel(2 * events.size());

    // constraints
    data.numConstraints = ieqs.size() + 2 + 2 + data.numVars;
    data.constraintCoeffs = new double[data.numConstraints][data.numVars];
    data.bounds = new double[data.numConstraints];
    int i = 0;
    while (i < ieqs.size()) {
      // x(a) - x(b) - x(c) - x(d) ... <= 0
      List<String> vars = ieqs.get(i);
      int j = 0;
      data.constraintCoeffs[i][events.indexOf(vars.get(j++))] = 1;
      while (j < vars.size()) {
        data.constraintCoeffs[i][events.indexOf(vars.get(j++))] = -1;
      }
      data.bounds[i] = 0;
      i += 1;
    }

    // sum(x) = k
    // 1. sum(x) <= k
    for (int j = 0; j < data.numVars / 2; j++)
      data.constraintCoeffs[i][j] = 1;
    data.bounds[i] = k;
    i += 1;
    // 2. -sum(x) <= -k
    for (int j = 0; j < data.numVars / 2; j++)
      data.constraintCoeffs[i][j] = -1;
    data.bounds[i] = -k;
    i += 1;

    // x(v) = 0
    // 1. -x(v) <= 0
    data.constraintCoeffs[i][events.indexOf(v)] = -1;
    data.bounds[i] = 0;
    i += 1;
    // 2. x(v) <= 0
    data.constraintCoeffs[i][events.indexOf(v)] = 1;
    data.bounds[i] = 0;
    i += 1;

    // x - f <= s === x - s <= f
    for (int j = 0, jj = data.numVars / 2; jj < data.numVars; j++, jj++, i++) {
      data.constraintCoeffs[i][j] = 1; // x(.)
      data.constraintCoeffs[i][jj] = -1; // -s(.)
      data.bounds[i] = f.getOrDefault(events.get(j), 0L); // f(.)
    }

    // x - f >= -s === -x - s <= -f
    for (int j = 0, jj = data.numVars / 2; jj < data.numVars; j++, jj++, i++) {
      data.constraintCoeffs[i][j] = -1; // -x(.)
      data.constraintCoeffs[i][jj] = -1; // -s(.)
      data.bounds[i] = 0 - f.getOrDefault(events.get(j), 0L); // -f(.)
    }

    // objective: 0.5 * s
    data.objCoeffs = new double[data.numVars];
    for (int j = 0, jj = data.numVars / 2; jj < data.numVars; j++, jj++) {
      data.objCoeffs[j] = 0;
      data.objCoeffs[jj] = 0.5;
    }

    try {
      return MipVarArray.v().solve(data, false); // minimize
    } finally {
      System.gc();
      System.gc();
      System.gc();
    }
  }
}