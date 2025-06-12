package pso;

import pso.core.*;
import pso.model.Position;

public class Main {
    public static void main(String[] args) {
        FitnessFunction sphere = position -> {
            double sum = 0;
            for (double x : position.values) {
                sum += x * x;
            }
            return sum;
        };

        PSO pso = new PSO(
            30,  // 粒子数
            2,   // 次元
            100, // 反復数
            0.5, 1.5, 1.5, // w, c1, c2
            -10, 10,       // 位置範囲
            -1, 1,         // 速度範囲
            sphere         // 評価関数
        );

        pso.run();
    }
}
