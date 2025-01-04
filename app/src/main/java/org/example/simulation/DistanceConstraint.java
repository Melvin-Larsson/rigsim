package org.example.simulation;

import org.apache.commons.math3.linear.RealMatrix;
import org.example.Vector2;

import java.util.List;

public class DistanceConstraint implements Constraint {
    private SimulationParticle p1;
    private SimulationParticle p2;
    private float distance;

    public DistanceConstraint(SimulationParticle p1, SimulationParticle p2, float distance){
        this.p1 = p1;
        this.p2 = p2;
        this.distance = distance;
    }

    @Override
    public void apply(float time) {

    }

    @Override
    public void insertConstraint(List<Particle> particles, RealMatrix constraint, int row, ParticleSystem system) {
//        constraint.set(row, 0, particle.getPosition().distance(this.position));
        Vector2 diff = this.p2.getPosition().sub(this.p1.getPosition());
        constraint.setEntry(row, 0, (diff.dot(diff) - this.distance * this.distance) / 2);
    }

    @Override
    public void insertJacobian(List<Particle> particles, RealMatrix jacobian, int row, ParticleSystem system) {
        ParticleSystem.MatrixPosition mp1 = system.getMatrixPosition(this.p1.getParticle());
        ParticleSystem.MatrixPosition mp2 = system.getMatrixPosition(this.p2.getParticle());

        Vector2 diff = this.p1.getPosition().sub(this.p2.getPosition());

        jacobian.setEntry(row, mp1.getX(), diff.getX());
        jacobian.setEntry(row, mp1.getY(), diff.getY());

        jacobian.setEntry(row, mp2.getX(), -diff.getX());
        jacobian.setEntry(row, mp2.getY(), -diff.getY());
    }

    @Override
    public void insertJacobianDerivative(List<Particle> particles, RealMatrix jacobianDerivative, int row, ParticleSystem system) {
        ParticleSystem.MatrixPosition mp1 = system.getMatrixPosition(this.p1.getParticle());
        ParticleSystem.MatrixPosition mp2 = system.getMatrixPosition(this.p2.getParticle());

        Vector2 diff = this.p1.getVelocity().sub(this.p2.getVelocity());

        jacobianDerivative.setEntry(row, mp1.getX(), diff.getX());
        jacobianDerivative.setEntry(row, mp1.getY(), diff.getY());

        jacobianDerivative.setEntry(row, mp2.getX(), -diff.getX());
        jacobianDerivative.setEntry(row, mp2.getY(), -diff.getY());
    }
}
