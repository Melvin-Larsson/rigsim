package org.example.simulation;

import org.example.Force;
import org.example.Scale;
import org.example.Vector2;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MouseForce extends MouseAdapter implements Force {
    private Scale scale;

    private Vector2 mousePosition = null;

    private Particle selectedParticle;
    private Vector2 force;
    private Vector2 lastMouseParticleDiff;
    private Vector2 diffSum;
    private int sumTerms;
    private List<Particle> particles = List.of();

    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    private float radius = 0.2f;
    private float p = 40;
    private float i = 0.001f;
    private float d = 10000000;

    public MouseForce(Scale scale){
        this.scale = scale;
        reset();
    }

    private void reset(){
        Vector2 zero = new Vector2(0 ,0);
        this.selectedParticle = null;
        this.force = zero;
        this.lastMouseParticleDiff = zero;
        this.diffSum = zero;
        this.sumTerms = 1;
        this.mousePosition = null;
    }

    @Override
    public void apply(List<Particle> particles) {
        this.particles = particles;

        Runnable task = tasks.poll();
        if(task != null){
            task.run();
        }

        if(this.selectedParticle == null){
            return;
        }

        this.selectedParticle.addForce(this.force);

        if(this.mousePosition == null){
            return;
        }


        Vector2 diff = mousePosition.sub(selectedParticle.getPosition());
        this.force = diff.mul(this.p);

        if(lastMouseParticleDiff != null){
            Vector2 derivative = diff.sub(lastMouseParticleDiff);
            this.force = this.force.add(derivative.mul(this.d));
        }

        this.force = this.force.add(diffSum.mul(i));

        this.diffSum = this.diffSum.add(diff);
        this.sumTerms++;
        this.lastMouseParticleDiff = diff;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        Vector2 mousePosition = scale.scaleToMeters(e.getPoint());
        for(Particle particle : particles){
            if(particle.getPosition().distance(mousePosition) <= this.radius){
                this.tasks.add(() -> {
                    this.selectedParticle = particle;
                    this.mousePosition = mousePosition;
                });
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        this.tasks.add(this::reset);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        this.mousePosition = scale.scaleToMeters(e.getPoint());
    }
}
