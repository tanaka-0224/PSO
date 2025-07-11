package pso;

import pso.core.*;
import pso.model.Position;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== PSO プログラム開始 ===");
        System.out.println("1. 評価関数（Sphere関数）を定義");
        
        FitnessFunction sphere = position -> {
            System.out.println("    → FitnessFunction.evaluate() 呼び出し: 位置 = (" + 
                             position.values[0] + ", " + position.values[1] + ")");
            double sum = 0;
            for (double x : position.values) {
                sum += x * x;
            }
            System.out.println("    → 評価値 = " + sum);
            return sum;
        };

        System.out.println("2. PSOインスタンスを作成");
        PSO pso = new PSO(
            3,   // 粒子数（デバッグ用に1個に減らす）
            2,   // 次元
            3,   // 反復数（デバッグ用に1回に減らす）
            0.5, 1.5, 1.5, // w, c1, c2
            -10, 10,       // 位置範囲
            -1, 1,         // 速度範囲
            sphere         // 評価関数
        );

        System.out.println("3. PSOアルゴリズムを実行");
        pso.run();
        System.out.println("=== プログラム終了 ===");
    }
}
