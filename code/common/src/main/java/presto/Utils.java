package presto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Utils {

  public static <K extends Comparable<? super K>, V> List<Map.Entry<K, V>> sortByKey(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Map.Entry.comparingByKey());
    return list;
  }

  static Collection<Func> readFunctionsFromJson(String funcListPath) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Collection<Func> res;
    try {
      Type collectionType = new TypeToken<Set<Func>>() {
      }.getType();
      res = gson.fromJson(new FileReader(funcListPath), collectionType);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    System.out.println(res.size() + " dictionary events read successfully.");
    return res;
  }

  static Collection<Profile> readProfilesFromJson(String dir, Set<String> sigs) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Collection<Profile> res = ConcurrentHashMultiset.create();
    File[] jsonFiles = new File(dir).listFiles((d, name) -> name.endsWith(".json"));
    assert jsonFiles != null;

    Set<String> missingFuncs = Sets.newConcurrentHashSet();
    Arrays.stream(jsonFiles).parallel().forEach(jf -> {
      Type collectionType = new TypeToken<HashSet<FuncProfile>>() {
      }.getType();
      Set<FuncProfile> funcs;
      try {
        funcs = gson.fromJson(new FileReader(jf), collectionType);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      for (FuncProfile fProfile : funcs) {
        if (!sigs.contains(fProfile.sig)) {
          missingFuncs.add(fProfile.sig);
        }
      }
      Profile p = new Profile(funcs, jf);
      res.add(p);
    });

    for (String f : missingFuncs) {
      System.out.println(dir + "\tmissing:\t" + f);
    }

    // try {
    // for (File jf : jsonFiles) {
    // Type collectionType = new TypeToken<HashSet<FuncProfile>>() {}.getType();
    // Set<FuncProfile> funcs = gson.fromJson(new FileReader(jf), collectionType);
    // // funcs.removeIf(p -> !sigs.contains(p.func) || p.freq < 1);
    // // funcs.forEach(
    // // fp -> {
    // // if (!sigs.contains(fp.func))
    // // throw new RuntimeException("No static events found: " + fp.func);
    // // });
    //
    // // try {
    // // File f = new File("src/" + jf.getPath());
    // // f.getParentFile().mkdirs();
    // // FileWriter fw = new FileWriter(f);
    // // fw.write(gson.toJson(funcs));
    // // fw.flush();
    // // fw.close();
    // // } catch (IOException e) {
    // // e.printStackTrace();
    // // }
    //
    // // for (String s : sigs) {
    // // FuncProfile fp = new FuncProfile(s, 0L);
    // // if (!funcs.contains(fp)) funcs.add(fp);
    // // }
    // Profile p = new Profile(funcs);
    // res.add(p);
    // }
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // throw new RuntimeException(e);
    // }
    System.out.println(res.size() + " profiles read successfully.");
    return res;
  }

  static double clip(double val, double low, double high) {
    assert low <= high;
    if (val < low)
      val = low;
    if (val > high)
      val = high;
    return val;
  }
}
