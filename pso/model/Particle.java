package pso.model;

import pso.core.Velocity;

public class Particle {
    public Position position;
    public Velocity velocity;
    public Position personalBest;
    public double personalBestValue;

    public Particle(Position position, Velocity velocity) {
        this.position = position;
        this.velocity = velocity;
        this.personalBest = new Position(position.values.clone());
        this.personalBestValue = Double.MAX_VALUE;
    }
}
