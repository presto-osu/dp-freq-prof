package presto;

public class MainRRBinomial extends MainRR implements BinomialDist {
  MainRRBinomial(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new MainRRBinomial(args).run();
  }

  @Override
  void randomizedResponse(Profile profile, Profile servProfile, double p) {
    long totalFreq = profile.getTotalFreq();
    if (totalFreq != 5 * funcs.size()) {
      throw new RuntimeException("BAD in RR!!!!!!!!!!!!!!!!!!");
    }
    funcs
        .parallelStream()
        .forEach(
            m -> {
              servProfile.add(
                  m,
                  getBinomial(profile.get(m), p) + getBinomial(totalFreq - profile.get(m), 1 - p));
            });
  }
}
