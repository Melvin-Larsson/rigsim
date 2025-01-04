package org.example.simulation;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.ejml.simple.SimpleMatrix;

import java.util.List;

public interface Constraint {
    void apply(float time);

    void insertConstraint(List<Particle> particles, RealMatrix constraint, int row, ParticleSystem system);
    void insertJacobian(List<Particle> particles, RealMatrix jacobian, int row, ParticleSystem system);
    void insertJacobianDerivative(List<Particle> particles, RealMatrix jacobianDerivative, int row, ParticleSystem system);
}
