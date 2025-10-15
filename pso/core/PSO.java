package pso.core;

import pso.model.*;
import pso.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class PSO {
    private final int swarmSize;
    private final int dimensions;
    private final int maxIterations;
    private final double w, c1, c2;
    private final double minPosition, maxPosition;
    private final double minVelocity, maxVelocity;
    private final FitnessFunction function;

    private final List<Particle> swarm = new ArrayList<>();
    private Position globalBest;
    private double globalBestValue = Double.POSITIVE_INFINITY;

    private IterationCallback callback; // 🟢 フィールドはここ（メソッドの外）

    public PSO(int swarmSize, int dimensions, int maxIterations,
               double w, double c1, double c2,
               double minPosition, double maxPosition,
               double minVelocity, double maxVelocity,
               FitnessFunction function) {
        System.out.println("    → PSOコンストラクタ開始");
        this.swarmSize = swarmSize;
        this.dimensions = dimensions;
        this.maxIterations = maxIterations;
        this.w = w;
        this.c1 = c1;
        this.c2 = c2;
        this.minPosition = minPosition;
        this.maxPosition = maxPosition;
        this.minVelocity = minVelocity;
        this.maxVelocity = maxVelocity;
        this.function = function;
    }

    public void setIterationCallback(IterationCallback callback) {
        this.callback = callback;
        this.globalBestValue = Double.MAX_VALUE;
        System.out.println("    → PSOコンストラクタ完了: 粒子数=" + swarmSize + ", 次元=" + dimensions);
    }

    public void run() {
        System.out.println("    → PSO.run() 開始");
        System.out.println("    → Step 1: 粒子群の初期化");
        initializeSwarm();
        // System.out.println("    → Step 2: メインループ開始 (" + maxIterations + "回)");
        for (int iter = 0; iter < maxIterations; iter++) {
            // System.out.println("    → 反復 " + iter + " 開始");
            for (int pIndex = 0; pIndex < swarm.size(); pIndex++) {
                Particle p = swarm.get(pIndex);
                // System.out.println("      → 粒子 " + pIndex + " の更新開始");
                updateVelocity(p);
                updatePosition(p);
                updatePersonalBest(p);
                updateGlobalBest(p);
                System.out.println("      → 粒子 " + pIndex + " の更新完了");
            }

            // 🟡 コールバック呼び出し
            if (callback != null) {
                List<Position> positions = new ArrayList<>();
                for (Particle p : swarm) {
                    positions.add(new Position(p.position.values.clone()));
                }
                callback.onIteration(iter, positions);
            }

            System.out.println("Iteration " + iter + ": Global Best Value = " + globalBestValue);
            System.out.println("    → 反復 " + iter + " 完了: Global Best Value = " + globalBestValue);
        }
        System.out.println("    → PSO.run() 完了");
    }

    private void initializeSwarm() {
        // System.out.println("      → initializeSwarm() 開始");
        for (int i = 0; i < swarmSize; i++) {
            System.out.println("        → 粒子 " + i + " の初期化");
            double[] position = new double[dimensions];
            double[] velocity = new double[dimensions];

            for (int d = 0; d < dimensions; d++) {
                position[d] = RandomUtils.randomDouble(minPosition, maxPosition);
                velocity[d] = RandomUtils.randomDouble(minVelocity, maxVelocity);
            }

            System.out.println("        → 初期位置: (" + position[0] + ", " + position[1] + ")");
            System.out.println("        → 初期速度: (" + velocity[0] + ", " + velocity[1] + ")");
            
            Particle p = new Particle(new Position(position), new Velocity(velocity));
            double fitness = function.evaluate(p.position);
            p.personalBest = new Position(p.position.values.clone());
            p.personalBestValue = fitness;

            swarm.add(p);

            System.out.println("        → 初期評価値: " + fitness);
            
            if (fitness < globalBestValue) {
                globalBestValue = fitness;
                globalBest = new Position(p.position.values.clone());
                System.out.println("        → 新しい全体ベスト: " + fitness);
            }
        }
        System.out.println("      → initializeSwarm() 完了: 全体ベスト値 = " + globalBestValue);
    }

    private void updateVelocity(Particle p) {
        // System.out.println("        → updateVelocity() 開始");
        for (int d = 0; d < dimensions; d++) {
            double r1 = Math.random();
            double r2 = Math.random();
            double cognitive = c1 * r1 * (p.personalBest.values[d] - p.position.values[d]);
            double social = c2 * r2 * (globalBest.values[d] - p.position.values[d]);

            double oldVelocity = p.velocity.values[d];
            p.velocity.values[d] = w * p.velocity.values[d] + cognitive + social;

            // 制限
            p.velocity.values[d] = Math.max(minVelocity, Math.min(maxVelocity, p.velocity.values[d]));
            System.out.println("        → 次元 " + d + ": 速度 " + oldVelocity + " → " + p.velocity.values[d]);
        }
        System.out.println("        → updateVelocity() 完了");
    }

    private void updatePosition(Particle p) {
        // System.out.println("        → updatePosition() 開始");
        for (int d = 0; d < dimensions; d++) {
            double oldPosition = p.position.values[d];
            p.position.values[d] += p.velocity.values[d];

            // 範囲制限
            p.position.values[d] = Math.max(minPosition, Math.min(maxPosition, p.position.values[d]));
            System.out.println("        → 次元 " + d + ": 位置 " + oldPosition + " → " + p.position.values[d]);
            // double newPos = p.position.values[d] + p.velocity.values[d];
            if (p.position.values[d] == p.position.values[d] && p.velocity.values[d] != 0.0) {
                System.out.println("⚠️ 精度喪失発生: pos=" + p.position.values[d] + ", vel=" + p.velocity.values[d]);
            }
            // p.position.values[d] = newPos;
        }
        System.out.println("        → updatePosition() 完了");
    }

    private void updatePersonalBest(Particle p) {
        // System.out.println("        → updatePersonalBest() 開始");
        double fitness = function.evaluate(p.position);
        System.out.println("        → 現在の評価値: " + fitness + " (個人ベスト: " + p.personalBestValue + ")");
        if (fitness < p.personalBestValue) {
            p.personalBestValue = fitness;
            p.personalBest = new Position(p.position.values.clone());
            System.out.println("        → 個人ベスト更新: " + fitness);
        }
        System.out.println("        → updatePersonalBest() 完了");
    }

    private void updateGlobalBest(Particle p) {
        System.out.println("        → updateGlobalBest() 開始");
        System.out.println("        → 個人ベスト値: " + p.personalBestValue + " (全体ベスト: " + globalBestValue + ")");
        if (p.personalBestValue < globalBestValue) {
            globalBestValue = p.personalBestValue;
            globalBest = new Position(p.personalBest.values.clone());
            System.out.println("        → 全体ベスト更新: " + globalBestValue);
        }
        System.out.println("        → updateGlobalBest() 完了");
    }
}
