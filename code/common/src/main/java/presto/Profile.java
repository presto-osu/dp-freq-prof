package presto;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Profile {
  Map<String, Long> funcProfiles;
  File jf;

  public Profile(Set<FuncProfile> funcProfiles, File jf) {
    this.jf = jf;
    this.funcProfiles = Maps.newConcurrentMap();
    for (FuncProfile fp : funcProfiles) {
      this.funcProfiles.put(fp.sig, fp.count);
    }
  }

  public Profile() {
    funcProfiles = new ConcurrentHashMap<>();
  }

  public long getTotalFreq() {
    return funcProfiles.values().parallelStream().reduce(Long::sum).get();
  }

  public synchronized void add(String sig, long freq) {
    funcProfiles.put(sig, funcProfiles.getOrDefault(sig, 0L) + freq);
  }

  public synchronized void put(String sig, long freq) {
    funcProfiles.put(sig, freq);
  }

  public long get(String sig) {
    return getOrDefault(sig, 0L);
  }

  public long getOrDefault(String sig, long def) {
    return funcProfiles.getOrDefault(sig, def);
  }

  public Set<String> keySet() {
    return funcProfiles.keySet();
  }

  public List<String> topKFrequent(long k) {
    List<String> res = Lists.newLinkedList();
    if (k < 1) return res;
    //    Random rand = new Random();
    PriorityQueue<Map.Entry<String, Long>> pq =
        new PriorityQueue<>((int) k, (a, b) -> (int) (a.getValue() - b.getValue()));
    //                a.getValue().equals(b.getValue())
    //                    ? 2 * rand.nextInt(2) - 1 //b.getKey().compareTo(a.getKey())
    //                    : (int) (a.getValue() - b.getValue()));
    for (Map.Entry<String, Long> e : Utils.sortByKey(funcProfiles)) {
      pq.add(e);
      if (pq.size() > k) pq.poll();
    }
    assert pq.peek() != null;
    long leastFreq = pq.peek().getValue();
    //    System.out.println("least freq: " + leastFreq);

    while (!pq.isEmpty()) {
      if (pq.peek().getValue() > leastFreq) leastFreq = pq.peek().getValue();
      Map.Entry<String, Long> e = pq.poll();
      //      if (e.getValue() != 0)
      res.add(0, e.getKey());
    }
    //    System.out.println("most freq: " + leastFreq);

    //    for (Map.Entry<String, Long> e : funcProfiles.entrySet())
    //      if (e.getValue() == leastFreq) res.add(e.getKey());
    return res;
  }

  public Set<Map.Entry<String, Long>> hot(double threshold, Set<String> funcs) {
    //    Set<Map.Entry<String, Long>> ret = Sets.newHashSet();
    //    double max = threshold *
    // funcProfiles.values().parallelStream().max(Long::compareTo).get();
    //    for (Map.Entry<String, Long> e : funcProfiles.entrySet()) if (e.getValue() >= max)
    // ret.add(e);
    //    return ret;
    Set<Map.Entry<String, Long>> ret = Sets.newHashSet();
    Set<Map.Entry<String, Long>> valid =
        funcProfiles
            .entrySet()
            .parallelStream()
            .filter(e -> funcs.contains(e.getKey()))
            .collect(Collectors.toSet());
    double max =
        threshold * valid.parallelStream().max(Map.Entry.comparingByValue()).get().getValue();
    valid.forEach(
        e -> {
          if (e.getValue() >= max) ret.add(e);
        });
    return ret;
  }
}
