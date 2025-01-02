package org.example.simulation;

import org.example.Force;
import org.example.Vector2;

import java.util.List;

public class GravitationalForce implements Force {
    private float acceleration;

    public GravitationalForce(float acceleration){
        this.acceleration = acceleration;
    }

    @Override
    public void apply(List<Particle> particles) {
        for (Particle particle: particles){
            particle.addForce(new Vector2(0,particle.getMass() * acceleration ));
        }
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

}
