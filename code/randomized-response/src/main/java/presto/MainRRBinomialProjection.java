package presto;

import java.nio.file.Paths;

public class MainRRBinomialProjection extends MainRRBinomial implements SimplexProjection {
  MainRRBinomialProjection(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new MainRRBinomialProjection(args).run();
  }

  @Override
  Profile postProcessing(int trial_num, Profile servProfile, double z) {
    Profile adjProfile = super.postProcessing(trial_num, servProfile, z);
    String toCSV = intermediateResFile(trial_num) + "-after-qp.csv";
    if (Paths.get(toCSV).toFile().exists())
      return null;
    String fromCSV = intermediateResFile(trial_num) + "-after-postprocessing.csv";
    projectToSimplexCC20(app, trial_num, adjProfile, realProfile.getTotalFreq(), fromCSV, toCSV);
    return null;
  }
}
