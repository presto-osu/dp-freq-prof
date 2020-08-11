package presto;

import java.util.Random;

public interface BinomialDist {
    default long getBinomial(long range, double p) {
        double mean = range * p;
        double variance = range * p * (1 - p);
        double stddev = Math.sqrt(variance);
        double value = getGaussian(mean, stddev);
        return Math.round(value);
    }

    Random rand = new Random();

    default double getGaussian(double mean, double stddev) {
        return mean + rand.nextGaussian() * stddev;
    }
}
