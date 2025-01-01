package org.example;

import java.util.List;

public class ViscousDragForce implements Force{
    private float drag;

    public ViscousDragForce(float drag){
        this.drag = drag;
    }

    @Override
    public void apply(List<ParticleSystem.Particle> particles) {
        for (ParticleSystem.Particle particle : particles){
            Vector2 force = particle.getVelocity().mul(-drag);
            particle.addForce(force);
        }
    }
}
