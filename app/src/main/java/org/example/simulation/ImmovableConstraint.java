package org.example.simulation;

import org.apache.commons.math3.linear.RealMatrix;
import org.ejml.simple.SimpleMatrix;
import org.example.Vector2;

import java.util.List;

public class ImmovableConstraint implements Constraint{
    private Particle particle;

    public ImmovableConstraint(SimulationParticle particle){
        this.particle = particle.getParticle();
    }

    @Override
    public void apply(float time) {
        this.particle.clearForces();
    }

    @Override
    public void insertConstraint(List<Particle> particles, RealMatrix constraint, int row, ParticleSystem system) {

    }

    @Override
    public void insertJacobian(List<Particle> particles, RealMatrix jacobian, int row, ParticleSystem system) {

    }

    @Override
    public void insertJacobianDerivative(List<Particle> particles, RealMatrix jacobianDerivative, int row, ParticleSystem system) {

    }
}
