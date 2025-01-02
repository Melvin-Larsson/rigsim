package org.example;

import org.example.simulation.Particle;

import java.util.List;

public interface Force {
    void apply(List<Particle> particles);
}
