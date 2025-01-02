package org.example.simulation;

import org.example.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParticleSystem {
    private List<Particle> particles;
    private List<SimulationParticle> simulationParticles;
    private List<Force> forces;

    private ImmutableList<SimulationParticle> immutableSimulationParticles;
    private ImmutableList<Force> immutableForces;

    private ODESolver<DParticle, DParticle> solver = new RungeKuttaSolver<>();


    private int width;
    private int height;

    public ParticleSystem(int width, int height){
        this.width = width;
        this.height = height;

        particles = new ArrayList<>();
        forces = new ArrayList<>();
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

    public void step(float time){
        for (Particle particle : particles){
            particle.clearForces();
        }

        for (Force force : forces){
            force.apply(particles);
        }

        ParticleODE ode = new ParticleODE();
        for (Particle particle : particles){
            DParticle dp = new DParticle(particle.position, particle.velocity, particle);
            DParticle newDp = solver.step(dp, ode, time);
            particle.position = newDp.dPosition;
            particle.velocity = newDp.dVelocity;
//            particle.position = particle.position.add(particle.velocity.mul(time));
            if(particle.position.getY() > this.height){
                particle.position = new Vector2(particle.position.getX(), this.height);
                particle.velocity = new Vector2(particle.velocity.getX(), -particle.velocity.getY() * 0.8f);
            }
//
//            Vector2 netForce = particle.forces.stream().reduce(new Vector2(0,0), Vector2::add);
//            particle.velocity = particle.velocity.add(netForce.div(particle.mass).mul(time));
        }
    }

    public void setSolver(ODESolver solver){
        System.out.println("Using " + solver.getClass() + " solver");
        this.solver = solver;
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
