package presto;

import com.google.common.collect.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

class ConstraintsGraph {
  Collection<String> nodes;
  Multimap<String, String> edges; // a -> b means f(b) <= f(a)
  Multimap<String, String> revEdges;

  static ConstraintsGraph genConstraintsGraph(Collection<String> nodes, Collection<Pair<String, String>> lePairs) {
    ConstraintsGraph g = new ConstraintsGraph();
    g.nodes = nodes;
    g.edges = HashMultimap.create();
    g.revEdges = HashMultimap.create();
    for (Pair<String, String> p : lePairs) {
      g.edges.put(p.getSecond(), p.getFirst());
      g.revEdges.put(p.getFirst(), p.getSecond());
    }
    return g;
  }

  Multimap<String, String> reachableNodes = Multimaps.synchronizedMultimap(HashMultimap.create());
  
  Collection<String> reachable(String node) {
    if (!reachableNodes.containsKey(node)) {
      reachableNodes.putAll(node, bfs(node, edges, Sets.newHashSet()));
    }
    return reachableNodes.get(node);
  }

  Set<String> backwardReachable(String node) {
    return bfs(node, revEdges, Sets.newHashSet());
  }

  protected Set<String> bfs(String start, Multimap<String, String> edges, Set<String> visited) {
    return bfs(start, edges, visited, null);
  }

  protected Set<String> bfs(String start, Multimap<String, String> edges, Set<String> visited, Visitor visitor) {
    List<String> worklist = Lists.newArrayList(start);
    while (!worklist.isEmpty()) {
      String e = worklist.remove(0);
      if (visited.contains(e))
        continue;
      visited.add(e);
      if (null != visitor)
        visitor.visit(e);
      for (String child : edges.get(e)) {
        worklist.add(child);
      }
    }
    return visited;
  }

  static interface Visitor {
    void visit(String n);
  }

  Set<String> weaklyConnectedSubgraphNodes(String start) {
    Set<String> visited = Sets.newHashSet();
    List<String> worklist = Lists.newArrayList(start);
    while (!worklist.isEmpty()) {
      String e = worklist.remove(0);
      if (visited.contains(e))
        continue;
      visited.add(e);
      for (String child : edges.get(e)) {
        worklist.add(child);
      }
      for (String child : revEdges.get(e)) {
        worklist.add(child);
      }
    }
    return visited;
  }

  void dump(String dotFile, String start, Set<String> stop, Map<String, Long> f) throws IOException {
    try {
      FileWriter output = new FileWriter(dotFile);
      BufferedWriter writer = new BufferedWriter(output);

      writer.write("digraph G {");
      writer.write("\n rankdir=LR;");
      writer.write("\n node[shape=box];");
      // draw window nodes
      Set<String> interestingNodes = Sets.newHashSet(start);

      // add interesting nodes
      for (String node : edges.get(start)) {
        writeNodes(writer, bfs(node, edges, Sets.newHashSet(stop)), f);
        interestingNodes.add(node);
      }

      // forward traversal to write edges
      Set<Pair<String, String>> es = Sets.newHashSet();
      for (String node : interestingNodes) {
        writeSucc(writer, node, es);
      }
      for (String node : interestingNodes) {
        writePred(writer, node, es);
      }
      for (Pair<String, String> e : es) {
        writer.write(
            "\n n" + (e.getFirst().hashCode() & 0xfffffff) + " -> n" + (e.getSecond().hashCode() & 0xfffffff) + ";");
      }

      // end of .dot file
      writer.write("\n}");
      writer.close();
      System.out.println("flow graph dump to file: " + dotFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeNodes(BufferedWriter writer, Set<String> insterestingNode, Map<String, Long> f) throws IOException {
    for (String reach : insterestingNode) {
      int label = reach.hashCode() & 0xfffffff;
      String tag = String.valueOf(label);
      writer.write("\n n" + label + " [label=\"");
      writer.write(tag.replace('"', '\'') + ": " + f.getOrDefault(reach, 0L) + "\"");
      writer.write("];");
    }
  }

  private void writeSucc(BufferedWriter writer, String root, Set<Pair<String, String>> written) throws IOException {
    for (String succ : edges.get(root)) {
      Pair<String, String> e = new Pair<>(root, succ);
      if (written.contains(e))
        continue;
      written.add(e);
      writeSucc(writer, succ, written);
    }
  }

  private void writePred(BufferedWriter writer, String root, Set<Pair<String, String>> written) throws IOException {
    for (String pred : revEdges.get(root)) {
      Pair<String, String> e = new Pair<>(pred, root);
      if (written.contains(e))
        continue;
      written.add(e);
      writePred(writer, pred, written);
    }
  }
}