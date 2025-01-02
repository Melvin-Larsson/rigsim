package org.example.simulation;

import org.example.Force;
import org.example.Vector2;

import java.util.List;

public class ViscousDragForce implements Force {
    private float drag;

    public ViscousDragForce(float drag){
        this.drag = drag;
    }

    @Override
    public void apply(List<Particle> particles) {
        for (Particle particle : particles){
            Vector2 force = particle.getVelocity().mul(-drag);
            particle.addForce(force);
        }
    }

    public float getDrag() {
        return drag;
    }

    public void setDrag(float drag) {
        this.drag = drag;
    }

}
