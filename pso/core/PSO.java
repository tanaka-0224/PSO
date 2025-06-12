package pso.core;

import pso.model.*;
import pso.util.RandomUtils;
import java.util.*;

public class PSO {
    private final int swarmSize;
    private final int dimensions;
    private final int maxIterations;
    private final double w, c1, c2;
    private final double minPosition, maxPosition;
    private final double minVelocity, maxVelocity;
    private final FitnessFunction function;
    private List<Particle> swarm;
    private Position globalBest;
    private double globalBestValue;

    public PSO(int swarmSize, int dimensions, int maxIterations,
               double w, double c1, double c2,
               double minPosition, double maxPosition,
               double minVelocity, double maxVelocity,
               FitnessFunction function) {
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
        this.globalBestValue = Double.MAX_VALUE;
        this.swarm = new ArrayList<>();
    }

    public void run() {
        initializeSwarm();
        for (int iter = 0; iter < maxIterations; iter++) {
            for (Particle p : swarm) {
                updateVelocity(p);
                updatePosition(p);
                updatePersonalBest(p);
                updateGlobalBest(p);
            }
            System.out.println("Iteration " + iter + ": Global Best Value = " + globalBestValue);
        }
    }

    private void initializeSwarm() {
        for (int i = 0; i < swarmSize; i++) {
            double[] position = new double[dimensions];
            double[] velocity = new double[dimensions];
            for (int d = 0; d < dimensions; d++) {
                position[d] = RandomUtils.randomDouble(minPosition, maxPosition);
                velocity[d] = RandomUtils.randomDouble(minVelocity, maxVelocity);
            }
            Particle p = new Particle(new Position(position), new Velocity(velocity));
            double fitness = function.evaluate(p.position);
            p.personalBestValue = fitness;
            if (fitness < globalBestValue) {
                globalBestValue = fitness;
                globalBest = new Position(p.position.values.clone());
            }
            swarm.add(p);
        }
    }

    private void updateVelocity(Particle p) {
        for (int d = 0; d < dimensions; d++) {
            double r1 = Math.random();
            double r2 = Math.random();
            double cognitive = c1 * r1 * (p.personalBest.values[d] - p.position.values[d]);
            double social = c2 * r2 * (globalBest.values[d] - p.position.values[d]);
            p.velocity.values[d] = w * p.velocity.values[d] + cognitive + social;

            // 制限
            p.velocity.values[d] = Math.max(minVelocity, Math.min(maxVelocity, p.velocity.values[d]));
        }
    }

    private void updatePosition(Particle p) {
        for (int d = 0; d < dimensions; d++) {
            p.position.values[d] += p.velocity.values[d];
            // 制限
            p.position.values[d] = Math.max(minPosition, Math.min(maxPosition, p.position.values[d]));
        }
    }

    private void updatePersonalBest(Particle p) {
        double fitness = function.evaluate(p.position);
        if (fitness < p.personalBestValue) {
            p.personalBestValue = fitness;
            p.personalBest = new Position(p.position.values.clone());
        }
    }

    private void updateGlobalBest(Particle p) {
        if (p.personalBestValue < globalBestValue) {
            globalBestValue = p.personalBestValue;
            globalBest = new Position(p.personalBest.values.clone());
        }
    }
}
