package pso;

import pso.core.*;
import pso.model.Position;
import pso.visual.PSOVisualizerPanel;

import javax.swing.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 評価関数の定義（Sphere関数）
        FitnessFunction sphere = position -> {
            double sum = 0;
            for (double x : position.values) {
                sum += x * x;
            }
            return sum;
        };

        // PSO設定
        PSO pso = new PSO(
                30,     // 粒子数
                2,      // 次元数
                10,    // 反復数
                0.5, 1.5, 1.5,   // w, c1, c2
                -10, 10,         // 位置範囲
                -1, 1,           // 速度範囲
                sphere           // 評価関数
        );

        // 可視化パネルを準備
        PSOVisualizerPanel visualPanel = new PSOVisualizerPanel();
        JFrame frame = new JFrame("PSO Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(visualPanel);
        frame.setSize(600, 600);
        frame.setVisible(true);

        // コールバックで位置を渡す
        pso.setIterationCallback((iteration, positions) -> {
            SwingUtilities.invokeLater(() -> {
                visualPanel.setPositions(positions);
                frame.setTitle("PSO Visualizer - Iteration: " + iteration);
            });

            try {
                Thread.sleep(100); // 描画速度の調整（100ms）
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 別スレッドでPSO実行（UIがフリーズしないように）
        new Thread(pso::run).start();
    }
}
