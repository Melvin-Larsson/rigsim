package org.example;

import org.example.simulation.*;

import java.util.ArrayList;
import java.util.List;

public class SurfaceSphereSystem extends SoftBodySystem{
    private static final float MASS = 2;

    public SurfaceSphereSystem(int width, int height) {
        super(width, height);
    }

    @Override
    protected List<SimulationParticle> createParticles(ParticleSystem system) {
        int count = 10;
        List<Particle> ps = new ArrayList<>(count);

        Vector2 center = new Vector2(width / 2.0f,height * 0.2f);
        float radius = 1f;
        for(int i = 0; i < count; i++){
            float angle = (float)(i * 2 * Math.PI / count);
            Vector2 pos = center.add(new Vector2((float)Math.cos(angle) * radius, (float)Math.sin(angle) * radius));
            ps.add(new Particle(pos, MASS));
        }

        List<SimulationParticle> particles = ParticleFactory.createSimulationParticles(ps);
        system.addAllParticles(particles);

        for(int i = 0; i < particles.size(); i++){
            SimulationParticle curr = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++){
                SimulationParticle next = particles.get(j);
                Vector2 diff = next.getPosition().sub(curr.getPosition());
                system.addForce(new SpringForce(curr, next, diff.length(), 500, 20));
            }
        }

        return particles;
    }

    @Override
    public List<Line> getMesh() {
        List<Line> result = new ArrayList<>();

        for (int i = 0; i < super.particles.size(); i++){
            Vector2 p1 = super.particles.get(i).getPosition();
            for (int j = i + 1; j < super.particles.size(); j++){
                Vector2 p2 = super.particles.get(j).getPosition();
                result.add(new Line(p1, p2));
            }
        }

        return result;
    }

    @Override
    public Vector2[] getBorder() {
        return getNodes();
    }
}
