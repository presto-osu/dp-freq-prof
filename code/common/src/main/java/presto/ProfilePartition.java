package presto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class ProfilePartition {
  Set<Profile> createCluster(final Collection<Profile> profiles, final Set<String> funcs, final boolean similar,
      final int size) {
    final int coeff = similar ? 1 : -1;

    Profile start1 = null, start2 = null;
    final double dist = similar ? Double.MAX_VALUE : Double.MIN_VALUE;

    BiFunction<Double, Double, Boolean> compare = (x, y) -> similar ? x <= y : x >= y;

    Object[] obj = profiles.parallelStream()
        .map(p1 -> profiles.stream().filter(p2 -> p1 != p2)
            .map(p2 -> new Object[] { p1, p2, coeff * distance(p1, p2, funcs) })
            .min((o1, o2) -> Double.compare((double) o1[2], (double) o2[2])))
        .min((o1, o2) -> Double.compare((double) o1.get()[2], (double) o2.get()[2])).get().get();
    start1 = (Profile) obj[0];
    start2 = (Profile) obj[1];
    // for (final Profile p1 : profiles) {
    //   for (final Profile p2 : profiles) {
    //     if (p1 == p2)
    //       continue;
    //     final double d = distance(p1, p2, funcs);
    //     if (compare.apply(d, dist)) {
    //       start1 = p1;
    //       start2 = p2;
    //     }
    //   }
    // }

    final Set<Profile> cluster = Sets.newHashSet();
    assert (start1 != null && start2 != null);
    cluster.add(start1);
    cluster.add(start2);
    // System.out.println("!!!!! cluster=" + cluster);

    assert (size <= profiles.size());
    while (cluster.size() < size) {
      Profile closest = (Profile) profiles.parallelStream().filter(p -> !cluster.contains(p))
          .map(p -> new Object[] { p, coeff * averageDistance(p, cluster, funcs) })
          .min((o1, o2) -> Double.compare((double) o1[1], (double) o2[1])).get()[0];
      cluster.add(closest);
    }

    return cluster;
  }

  double averageDistance(final Profile x, final Set<Profile> cluster, final Set<String> funcs) {
    return cluster.stream().mapToDouble(y -> distance(x, y, funcs)).average().getAsDouble();
  }

  Map<Profile, Map<Profile, Double>> distMap = Maps.newConcurrentMap();

  double distance(final Profile p1, final Profile p2, final Set<String> funcs) {
    Map<Profile, Double> map = distMap.getOrDefault(p1, Maps.newConcurrentMap());
    double dist = map.getOrDefault(p2, -1.);
    if (dist >= 0.)
      return dist;
    long l1 = 0;
    for (final String func : funcs) {
      l1 += Math.abs(p1.get(func) - p2.get(func));
    }
    dist = 0.5 * l1;
    map.put(p2, dist);
    distMap.put(p1, map);
    return dist;
  }

  double averageClusterDistance(final Set<Profile> cluster, final Set<String> funcs) {
    int count = 0;
    double dist = 0.;
    for (final Profile p1 : cluster) {
      for (final Profile p2 : cluster) {
        if (p1 == p2)
          continue;
        count += 1;
        dist += distance(p1, p2, funcs);
      }
    }
    return dist / count;
  }
}
