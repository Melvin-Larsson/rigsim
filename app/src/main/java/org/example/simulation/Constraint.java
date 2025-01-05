package org.example.simulation;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;

public interface Constraint {
    void apply(float time);

    void insertConstraint(List<Particle> particles, RealVector constraint, int row, ParticleSystem system);
    void insertJacobian(List<Particle> particles, RealMatrix jacobian, int row, ParticleSystem system);
    void insertJacobianDerivative(List<Particle> particles, RealMatrix jacobianDerivative, int row, ParticleSystem system);
}
