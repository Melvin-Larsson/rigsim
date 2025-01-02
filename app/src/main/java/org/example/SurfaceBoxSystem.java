package org.example;

import org.example.simulation.*;

import java.util.ArrayList;
import java.util.List;

public class SurfaceBoxSystem extends SoftBodySystem {
    private static final float MASS = 10;

    public static Force testForce;

    public SurfaceBoxSystem(int width, int height) {
        super(width, height);
    }

    @Override
    protected List<SimulationParticle> createParticles(ParticleSystem system) {
        Vector2 center = new Vector2(super.width / 2.0f, super.height / 10.0f);
        Vector2[] positions = {new Vector2(-1, -1), new Vector2(1, -1), new Vector2(1, 1), new Vector2(-1, 1)};

        List<Particle> particles = new ArrayList<>(positions.length);

        for (int i = 0; i < positions.length; i++){
            particles.add(new Particle(positions[i].add(center), MASS));
        }

        List<SimulationParticle> simParticles = ParticleFactory.createSimulationParticles(particles);
        system.addAllParticles(simParticles);

        for (int i = 0; i < positions.length; i++){
            SimulationParticle curr = simParticles.get(i);
            for (int j = i + 1; j < positions.length; j++){
                SimulationParticle next = simParticles.get(j);
                Vector2 diff = next.getPosition().sub(curr.getPosition());
                Force f = new SpringForce(curr, next, diff.length(), 5000, 100);
                if(next.getPosition().getX() == curr.getPosition().getX()){
                    testForce = f;
                }
                system.addForce(f);
            }
        }

        return simParticles;
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
