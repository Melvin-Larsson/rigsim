package org.example.simulation;

import org.example.*;
import org.apache.commons.math3.linear.*;

import java.util.ArrayList;
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

    private DiagonalMatrix W;
    private RealVector C;
    private RealMatrix jacobian;
    private RealMatrix jacobianDerivative;
    private RealVector dq;
    private RealVector Q;
    private boolean matrixesInitialized = false;

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
        this.matrixesInitialized = false;
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


    private void initMatrices(){
        if(this.constraints.isEmpty()){
            return;
        }
        W = new DiagonalMatrix(this.particles.size() * 2);
        C = new ArrayRealVector(this.constraints.size());
        jacobian = new OpenMapRealMatrix(this.constraints.size(), this.particles.size() * 2);
        jacobianDerivative = new OpenMapRealMatrix(this.constraints.size(), this.particles.size() * 2);
        dq = new ArrayRealVector(this.particles.size() * 2);
        Q = new ArrayRealVector(this.particles.size() * 2);

        int i = 0;
        for(Particle p : this.particles) {
            W.setEntry(i * 2, i * 2, 1/p.getMass());
            W.setEntry(i * 2 + 1, i * 2 + 1, 1/p.getMass());
            i++;
        }

        this.matrixesInitialized = true;
    }

    public void step(float time){
        for (Particle particle : particles){
            particle.clearForces();
        }

        for (Force force : forces){
            force.apply(particles);
        }

        if(!matrixesInitialized){
            initMatrices();
        }

        if(!this.constraints.isEmpty()){
            int i = 0;
            for(Particle p : this.particles){
                dq.setEntry(i * 2, p.getVelocity().getX());
                dq.setEntry(i * 2 + 1, p.getVelocity().getY());

                Vector2 resultingForce = p.getForceSum();
                Q.setEntry(i * 2, resultingForce.getX());
                Q.setEntry(i * 2 + 1, resultingForce.getY());

                i++;
            }

            i = 0;
            for(Constraint constraint : this.constraints){
                constraint.insertConstraint(this.particles, C, i, this);
                constraint.insertJacobian(this.particles, jacobian, i, this);
                constraint.insertJacobianDerivative(this.particles, jacobianDerivative, i, this);

                i++;
            }

            RealVector dC = jacobian.operate(dq);
            RealVector right = jacobianDerivative.operate(dq).mapMultiply(-1)
                    .subtract(jacobian.operate(W.operate(Q)))
                    .subtract(C.mapMultiply(ks))
                    .subtract(dC.mapMultiply(kd));

            RealVector solution = solve2(jacobian, W, right);
            RealVector forces = multTranspose(jacobian, solution);

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

    private RealVector multTranspose(RealMatrix J, RealVector x){
        int numRows = J.getRowDimension();  // m
        int numCols = J.getColumnDimension();  // n
        RealVector JT_x = new ArrayRealVector(numCols);  // Result vector (size n)

        OpenMapRealMatrix om;
        // Compute J^T * x without forming J^T
        for (int col = 0; col < numCols; col++) {
            double dotProduct = 0.0;
            for (int row = 0; row < numRows; row++) {
                dotProduct += J.getEntry(row, col) * x.getEntry(row);
            }
            JT_x.setEntry(col, dotProduct);
        }

        return JT_x;
    }

    private RealVector solve2(RealMatrix J, DiagonalMatrix W, RealVector B){
        RealLinearOperator operator = new RealLinearOperator() {
            @Override
            public RealVector operate(RealVector x) {
                RealVector JT_x = multTranspose(J, x);

                RealVector M_JT_x = W.operate(JT_x);
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
            ConjugateGradient cg = new ConjugateGradient(10, 1e-5, false);
            return cg.solve(operator, B);
        }catch(Exception e){
            return new ArrayRealVector(B.getDimension());
        }
    }

    public void setSolver(ODESolver solver){
        System.out.println("Using " + solver.getClass() + " solver");
        this.solver = solver;
    }

    public ODESolver getSolver(){
        return this.solver;
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
