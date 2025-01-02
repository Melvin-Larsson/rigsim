package org.example.simulation;

import org.example.Vector2;

public class SimulationParticle {
    private Particle particle;

    SimulationParticle(Particle particle){
        this.particle = particle;
    }

    Particle getParticle() {return particle;}
    public Vector2 getPosition(){return particle.getPosition();}
    public Vector2 getVelocity(){return particle.getVelocity();}
    public float getMass(){return particle.getMass();}
}
