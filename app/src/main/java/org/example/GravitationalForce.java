package org.example;

import java.util.List;

public class GravitationalForce implements Force{
    @Override
    public void apply(List<ParticleSystem.Particle> particles) {
        for (ParticleSystem.Particle particle: particles){
            particle.addForce(new Vector2(0,particle.getMass() * 9.82f ));
        }
    }
}
