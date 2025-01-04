package org.example.simulation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.checkerframework.checker.units.qual.A;
import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.simple.SimpleMatrix;
import org.ejml.sparse.csc.factory.DecompositionFactory_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.example.*;
import org.apache.commons.math3.linear.*;

import javax.swing.text.StyleConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ParticleSystem {
    public static final float DEFAULT_BOUNCE_KEEP = 0.9f;

    private List<Particle> particles;
    private List<SimulationParticle> simulationParticles;
    private List<Force> forces;
    private List<Constraint> constraints;

    private ImmutableList<SimulationParticle> immutableSimulationParticles;
    private ImmutableList<Force> immutableForces;

    private ODESolver<DParticle, DParticle> solver = new RungeKuttaSolver<>();

    private float bounceKeep = DEFAULT_BOUNCE_KEEP;

    private Vector2 size;
    private float particleRadius;
    float kd = 100;
    float ks = 10000;

    public ParticleSystem(Vector2 size, float particleRadius){
        this.setSize(size);
        this.particleRadius = particleRadius;

        particles = new ArrayList<>();
        forces = new ArrayList<>();
        constraints = new ArrayList<>();
        this.immutableForces = new ImmutableList<>(forces);
        simulationParticles = new ArrayList<>();
        this.immutableSimulationParticles = new ImmutableList<>(simulationParticles);
    }

    public void addParticle(SimulationParticle particle){
        this.particles.add(particle.getParticle());
        this.simulationParticles.add(particle);
    }

    public void addAllParticles(Collection<SimulationParticle> particles){
        this.particles.addAll(particles.stream().map(p -> p.getParticle()).toList());
        this.simulationParticles.addAll(particles);
    }

    public ImmutableList<SimulationParticle> getParticles(){
        return this.immutableSimulationParticles;
    }
    public ImmutableList<Force> getForces(){
        return this.immutableForces;
    }

    public void reset(){
        for (Particle particle : particles){
            particle.reset();
        }
    }

    public void addForce(Force force){
        this.forces.add(force);
    }

    public void addConstraints(Constraint constraint){
        this.constraints.add(constraint);
    }

    public void step(float time){
        for (Particle particle : particles){
            particle.clearForces();
        }

        for (Force force : forces){
            force.apply(particles);
        }

        if(!this.constraints.isEmpty()){
            RealMatrix mass = new Array2DRowRealMatrix(this.particles.size() * 2, this.particles.size() * 2);
            RealMatrix dq = new Array2DRowRealMatrix(this.particles.size() * 2, 1);
            RealMatrix Q = new Array2DRowRealMatrix(this.particles.size() * 2, 1);
            int i = 0;
            for(Particle p : this.particles){
                mass.setEntry(i * 2, i * 2, p.getMass());
                mass.setEntry(i * 2 + 1, i * 2 + 1, p.getMass());

                dq.setEntry(i * 2, 0, p.getVelocity().getX());
                dq.setEntry(i * 2 + 1, 0, p.getVelocity().getY());

                Vector2 resultingForce = p.getForceSum();
                Q.setEntry(i * 2, 0, resultingForce.getX());
                Q.setEntry(i * 2 + 1, 0, resultingForce.getY());

                i++;
            }
            LUDecomposition luDecomposition = new LUDecomposition(mass);
            RealMatrix W = luDecomposition.getSolver().getInverse();

            RealMatrix C = new Array2DRowRealMatrix(this.constraints.size(), 1);
            RealMatrix jacobian = new OpenMapRealMatrix(this.constraints.size(), this.particles.size() * 2);
            RealMatrix jacobianDerivative = new OpenMapRealMatrix(this.constraints.size(), this.particles.size() * 2);

            i = 0;
            for(Constraint constraint : this.constraints){
                constraint.insertConstraint(this.particles, C, i, this);
                constraint.insertJacobian(this.particles, jacobian, i, this);
                constraint.insertJacobianDerivative(this.particles, jacobianDerivative, i, this);

                i++;
            }

            RealMatrix dC = jacobian.multiply(dq);
            RealMatrix right = jacobianDerivative.multiply(dq).scalarMultiply(-1).subtract(jacobian.multiply(W).multiply(Q)).subtract(C.scalarMultiply(ks)).subtract(dC.scalarMultiply(kd));

            RealVector solution = solve2(jacobian, W, right.getColumnVector(0));
            RealVector forces = jacobian.transpose().operate(solution);

            i = 0;
            for (Particle p: particles){
                Vector2 force = new Vector2((float)forces.getEntry(i * 2), (float)forces.getEntry(i * 2 + 1));
                p.addForce(force);

                i++;
            }

            for(Constraint constraint : constraints){
                constraint.apply(time);
            }
        }

        ParticleODE ode = new ParticleODE();
        for (Particle particle : particles){
            DParticle dp = new DParticle(particle.position, particle.velocity, particle);
            DParticle newDp = solver.step(dp, ode, time);
            particle.position = newDp.dPosition;
            particle.velocity = newDp.dVelocity;

            if(particle.position.getY() + this.particleRadius > this.size.getY()){
                particle.position = new Vector2(particle.position.getX(), this.size.getY() - this.particleRadius);
                particle.velocity = new Vector2(particle.velocity.getX(), -particle.velocity.getY() * bounceKeep);
            }
        }
    }

    private RealVector solve2(RealMatrix J, RealMatrix W, RealVector B){
        RealLinearOperator operator = new RealLinearOperator() {
            @Override
            public RealVector operate(RealVector x) {
                // Step 1: Compute J^T x
                RealVector JT_x = J.transpose().operate(x);

                // Step 2: Multiply by diagonal M (extract diagonal and multiply element-wise)
                RealVector M_JT_x = W.operate(JT_x);

                // Step 3: Compute J (M J^T x)
                RealVector result =  J.operate(M_JT_x);

                double lambda = 1e-6;
                return result.add(x.mapMultiply(lambda));
            }

            @Override
            public int getRowDimension() {
                return J.getRowDimension(); // Number of rows in J
            }

            @Override
            public int getColumnDimension() {
                return J.getRowDimension(); // Number of rows in J
            }
        };

        // Solve using Conjugate Gradient
        try{
            ConjugateGradient cg = new ConjugateGradient(1000, 1e-10, true);
            return cg.solve(operator, B);
        }catch(Exception e){
            return new ArrayRealVector(B.getDimension());
        }
    }

    public void setSolver(ODESolver solver){
        System.out.println("Using " + solver.getClass() + " solver");
        this.solver = solver;
    }

    public float getBounceKeep() {
        return bounceKeep;
    }

    public void setBounceKeep(float bounceKeep) {
        if(bounceKeep < 0){
            throw new IllegalArgumentException("Bounce keep can not be less than zero. Provided value was " + bounceKeep);
        }

        this.bounceKeep = bounceKeep;
    }

    public void setSize(Vector2 size){
        if(size.getY() < 0){
            throw new IllegalArgumentException("Height can not be less than zero. Provided value was " + size.getY());
        }
        if(size.getX() < 0){
            throw new IllegalArgumentException("Width can not be less than zero. Provided value was " + size.getX());
        }
        this.size = size;
    }

    public void setParticleRadius(float particleRadius) {
        if(particleRadius <= 0){
            throw new IllegalArgumentException("Particle radius must be greater than zero. Provided value was " + particleRadius);
        }

        this.particleRadius = particleRadius;
    }

    public MatrixPosition getMatrixPosition(Particle particle){
        return new MatrixPosition(this.particles.indexOf(particle));
    }

    public class MatrixPosition{
        private int startIndex;

        public MatrixPosition(int start){
            this.startIndex = start * 2;
        }

        public int getX(){
            return startIndex;
        }

        public int getY(){
            return startIndex + 1;
        }
    }



        private class ParticleODE implements ODE<DParticle, DParticle> {
        @Override
        public DParticle f(DParticle dParticle) {
            Vector2 netForce = dParticle.particle.forces.stream().reduce(new Vector2(0,0), Vector2::add);
            return new DParticle(dParticle.dVelocity, netForce.div(dParticle.particle.getMass()), dParticle.particle);
        }

        @Override
        public DParticle add(DParticle dParticle, DParticle dParticle2) {
            return new DParticle(dParticle.dPosition.add(dParticle2.dPosition), dParticle.dVelocity.add(dParticle2.dVelocity), dParticle.particle);
        }

        @Override
        public DParticle scale(DParticle x, float factor) {
            return new DParticle(x.dPosition.mul(factor), x.dVelocity.mul(factor), x.particle);
        }
    }

     public class DParticle {
        private final Vector2 dPosition;
        private final Vector2 dVelocity;
        private final Particle particle;

        public DParticle(Vector2 dPosition, Vector2 dVelocity, Particle particle) {
            this.dPosition = dPosition;
            this.dVelocity = dVelocity;
            this.particle = particle;
        }
    }

}
