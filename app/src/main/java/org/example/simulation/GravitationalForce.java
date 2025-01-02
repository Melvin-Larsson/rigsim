package org.example.simulation;

import org.example.Force;
import org.example.Vector2;

import java.util.List;

public class GravitationalForce implements Force {
    @Override
    public void apply(List<Particle> particles) {
        for (Particle particle: particles){
            particle.addForce(new Vector2(0,particle.getMass() * 9.82f ));
        }
    }
}
