package pso.util;

import java.util.Random;

public class RandomUtils {
    private static final Random rand = new Random();

    public static double randomDouble(double min, double max) {
        return min + (max - min) * rand.nextDouble();
    }
}
