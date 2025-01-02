package org.example.simulation;

import org.example.Vector2;
import java.util.*;

public class ParticleFactory {
    public static List<SimulationParticle> createSimulationParticles(List<Particle> editableParticles) {
        return editableParticles.stream()
                                .map(p -> new SimulationParticle(new Particle(p.getPosition(), p.getMass())))
                                .toList();
    }
}
