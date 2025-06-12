package pso.core;

import pso.model.Position;

public interface FitnessFunction {
    double evaluate(Position position);
}
