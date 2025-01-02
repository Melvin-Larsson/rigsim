package org.example;

import org.example.simulation.Particle;
import org.example.simulation.ParticleSystem;
import org.example.simulation.SimulationParticle;

import java.util.List;

public abstract class SoftBodySystem {
    public final int width;
    public final int height;
    private final ParticleSystem system;
    protected List<SimulationParticle> particles;

    public SoftBodySystem(int width, int height){
        this.width = width;
        this.height = height;
        this.system = new ParticleSystem(width, height);
        this.particles = createParticles(this.system);
    }

    protected abstract List<SimulationParticle> createParticles(ParticleSystem system);
    public abstract List<Line> getMesh();
    public abstract Vector2[] getBorder();


    public ParticleSystem getSystem(){
        return system;
    }

    public Vector2[] getNodes(){
        Vector2[] result = new Vector2[particles.size()];
        for (int i = 0; i < particles.size(); i++){
            result[i] = particles.get(i).getPosition();
        }
        return result;
    }
}
