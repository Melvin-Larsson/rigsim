package org.example;

import java.util.ArrayList;
import java.util.List;

public class SurfaceSphereSystem extends SoftBodySystem{
    private static final float MASS = 4;

    public SurfaceSphereSystem(int width, int height) {
        super(width, height);
    }

    @Override
    protected ParticleSystem.Particle[] createParticles(ParticleSystem system) {
        ParticleSystem.Particle ps[] = new ParticleSystem.Particle[20];

        Vector2 center = new Vector2(width / 2.0f,height * 0.2f);
        float radius = 1f;
        for(int i = 0; i < ps.length; i++){
            float angle = (float)(i * 2 * Math.PI / ps.length);
            Vector2 pos = center.add(new Vector2((float)Math.cos(angle) * radius, (float)Math.sin(angle) * radius));
            ps[i] = system.addParticle(pos, MASS);
        }
        for(int i = 0; i < ps.length; i++){
            ParticleSystem.Particle curr = ps[i];
            for (int j = i + 1; j < ps.length; j++){
                ParticleSystem.Particle next = ps[j];
                Vector2 diff = next.getPosition().sub(curr.getPosition());
                system.addForce(new SpringForce(curr, next, diff.length(), 500, 10));
            }
        }
        return ps;
    }

    @Override
    public List<Line> getMesh() {
        List<Line> result = new ArrayList<>();

        for (int i = 0; i < super.particles.length; i++){
            Vector2 p1 = super.particles[i].getPosition();
            for (int j = i + 1; j < super.particles.length; j++){
                Vector2 p2 = super.particles[j].getPosition();
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
