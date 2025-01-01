package org.example;

import java.util.List;

public class SpringForce implements Force{
    private ParticleSystem.Particle particleA;
    private ParticleSystem.Particle particleB;
    private float springLength;
    private float springConstant;
    private float dampingConstant;

    public SpringForce(ParticleSystem.Particle particleA, ParticleSystem.Particle particleB, float springLength, float springConstant, float dampingConstant){
        this.particleA = particleA;
        this.particleB = particleB;
        this.springLength = springLength;
        this.springConstant = springConstant;
        this.dampingConstant = dampingConstant;
    }

    @Override
    public void apply(List<ParticleSystem.Particle> particles) {
        Vector2 pA = particleA.getPosition();
        Vector2 pB = particleB.getPosition();
        Vector2 vA = particleA.getVelocity();
        Vector2 vB = particleB.getVelocity();

        Vector2 L = pA.sub(pB);
        Vector2 dL = vA.sub(vB);
        if(L.length() < 1){
            return;
        }
//        System.out.println("L: " + L + ", dL: " + dL);
        float length = L.length();
        float length2 = dL.length();
        float dot = dL.dot(L);

        float magnitudeSpring = (L.length() - this.springLength) * this.springConstant;
        float magnitudeDamping = dL.dot(L) * this.dampingConstant / L.length();

        Vector2 f = L.normalized().mul(magnitudeSpring + magnitudeDamping);

        particleB.addForce(f);
        particleA.addForce(f.mul(-1));
    }
}
