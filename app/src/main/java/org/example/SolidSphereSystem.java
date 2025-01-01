package org.example;

import java.util.List;

public class SolidSphereSystem extends SoftBodySystem {
    private ParticleSystem.Particle[][] ps;

    private static final float radius = 1;
    private static final int layers = 3;
    private static final int angles = 10;
    private static final float mass = 10;

    public SolidSphereSystem(int width, int height){
        super(width, height);
    }

    @Override
    protected ParticleSystem.Particle[] createParticles(ParticleSystem system) {
        ps = new ParticleSystem.Particle[layers][angles];

        Vector2 center = new Vector2(2,2);
        for(int i = 0; i < angles; i++){
            float angle = (float)(i * 2 * Math.PI / angles);
            for(int j = 0; j < layers; j++){
                float currRadius = radius / 3 * (j + 1);
                Vector2 pos = center.add(new Vector2((float)Math.cos(angle) * currRadius, (float)Math.sin(angle) * currRadius));
                ps[j][i] = system.addParticle(pos, mass / ps.length);
            }
            for(int j = 0; j < layers - 1; j++){
                ParticleSystem.Particle curr = ps[j][i];
                ParticleSystem.Particle next = ps[j + 1][i];
                Vector2 diff = next.getPosition().sub(curr.getPosition());
                system.addForce(new SpringForce(curr, next, diff.length(), 500, 1000));
            }
        }

        for(int i = 0; i < layers; i++){
            for(int j = 0; j < angles; j++){
                ParticleSystem.Particle curr = ps[i][j];
                ParticleSystem.Particle next = ps[i][(j + 1) % angles];
                Vector2 diff = next.getPosition().sub(curr.getPosition());
                system.addForce(new SpringForce(curr, next, diff.length(), 500, 1000));
            }
        }

        for(int i = 0; i < angles; i++){
            for(int j = i + 1; j < angles; j++){
                ParticleSystem.Particle curr = ps[0][i];
                ParticleSystem.Particle next = ps[0][j];
                Vector2 diff = next.getPosition().sub(curr.getPosition());
                system.addForce(new SpringForce(curr, next, diff.length(), 500, 1000));
            }
        }
        ParticleSystem.Particle[] result = new ParticleSystem.Particle[layers * angles];
        for(int i = 0; i < result.length; i++){
            result[i] = ps[i / angles][i % angles];
        }

        return result;
    }

    @Override
    public List<Line> getMesh() {
        return List.of();
    }

    @Override
    public Vector2[] getBorder() {
        Vector2[] result = new Vector2[angles];
        for(int i = 0; i < angles; i++){
            result[i] = ps[layers - 1][i].getPosition();
        }
        return result;
    }
}
