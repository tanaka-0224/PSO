package pso.core;

import pso.model.Position;
import java.util.List;

@FunctionalInterface
public interface IterationCallback {
    void onIteration(int iteration, List<Position> positions);
}
