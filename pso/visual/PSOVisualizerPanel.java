package pso.visual;

import pso.model.Position;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PSOVisualizerPanel extends JPanel {
    private List<Position> positions;

    public void setPositions(List<Position> positions) {
        this.positions = positions;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (positions == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLUE);

        for (Position pos : positions) {
            double[] v = pos.values;
            if (v.length < 2) continue; // 2次元だけ描画対象

            int x = toScreenX(v[0]);
            int y = toScreenY(v[1]);
            g2.fillOval(x - 4, y - 4, 8, 8); // 粒子の描画
        }
    }

    private int toScreenX(double x) {
        return (int) ((x + 10) / 20.0 * getWidth());
    }

    private int toScreenY(double y) {
        return (int) ((y + 10) / 20.0 * getHeight());
    }
}
