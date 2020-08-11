package presto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public interface SimplexProjection {
  static File NULL_FILE = new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));

  default void projectToSimplexCC20(String app, int trial_num, Profile adjProfile, long sum, String fromCSV,
      String toCSV) {
    // TODO: call octave or matlab
    String lePairCSVName = Paths.get("le-pairs-csv", app + ".csv").toAbsolutePath().toString();
    String cmd = String.format(
        "addpath(genpath('./common/src/main/matlab'));projectToSimplexCC20('%s','%s','%s',%d);exit;", fromCSV, toCSV,
        lePairCSVName, sum);
    try {
      ProcessBuilder pb = new ProcessBuilder("matlab", "-nodisplay", "-nosplash", "-nodesktop", "-nojvm", "-r", cmd);
      pb.inheritIO();
      pb.redirectOutput(NULL_FILE);
      Process process = pb.start();
      process.waitFor();
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
