package org.example;

import java.util.List;

public abstract class SoftBodySystem {
    public final int width;
    public final int height;
    private final ParticleSystem system;
    protected ParticleSystem.Particle[] particles;

    public SoftBodySystem(int width, int height){
        this.width = width;
        this.height = height;
        this.system = new ParticleSystem(width, height);
        this.particles = createParticles(this.system);
    }

    protected abstract  ParticleSystem.Particle[] createParticles(ParticleSystem system);
    public abstract List<Line> getMesh();
    public abstract Vector2[] getBorder();


    public ParticleSystem getSystem(){
        return system;
    }

    public Vector2[] getNodes(){
        Vector2[] result = new Vector2[particles.length];
        for (int i = 0; i < particles.length; i++){
            result[i] = particles[i].getPosition();
        }
        return result;
    }
}
