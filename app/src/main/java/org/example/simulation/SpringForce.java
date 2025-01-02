package org.example.simulation;

import org.example.Force;
import org.example.Vector2;

import java.util.List;

public class SpringForce implements Force {
    private Particle particleA;
    private Particle particleB;

    private SimulationParticle simulationParticleA;
    private SimulationParticle simulationParticleB;

    private float springLength;
    private float springConstant;
    private float dampingConstant;

    public SpringForce(SimulationParticle particleA, SimulationParticle particleB, float springLength, float springConstant, float dampingConstant){
        this.particleA = particleA.getParticle();
        this.particleB = particleB.getParticle();

        this.simulationParticleA = particleA;
        this.simulationParticleB = particleB;

        this.springLength = springLength;
        this.springConstant = springConstant;
        this.dampingConstant = dampingConstant;
    }

    @Override
    public void apply(List<Particle> particles) {
        Vector2 pA = particleA.getPosition();
        Vector2 pB = particleB.getPosition();
        Vector2 vA = particleA.getVelocity();
        Vector2 vB = particleB.getVelocity();

        Vector2 L = pA.sub(pB);
        Vector2 dL = vA.sub(vB);
//        if(L.length() < 1){
//            return;
//        }

        float magnitudeSpring = (L.length() - this.springLength) * this.springConstant;
        float magnitudeDamping = dL.dot(L) * this.dampingConstant / L.length();

        Vector2 f = L.normalized().mul(magnitudeSpring + magnitudeDamping);

        particleB.addForce(f);
        particleA.addForce(f.mul(-1));
    }

    public SimulationParticle getParticleA(){
        return simulationParticleA;
    }

    public SimulationParticle getParticleB(){
        return simulationParticleB;
    }
}
