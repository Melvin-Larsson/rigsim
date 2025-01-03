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

    private Particle mouseParticle;
    private Particle selectedParticle;
    private SpringForce springForce;

    private List<Particle> particles = List.of();

    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    private float radius = 0.2f;
    private float mouseMass = 1000;
    private float springConstant = 10000.0f;
    private float springDamping = 100f;

    public MouseForce(Scale scale){
        this.scale = scale;
        reset();
    }

    private void reset(){
        Vector2 zero = new Vector2(0 ,0);
        this.selectedParticle = null;
        this.mouseParticle = null;
        this.mousePosition = null;
        this.springForce = null;
    }

    @Override
    public void apply(List<Particle> particles) {
        this.particles = particles;

        Runnable task = tasks.poll();
        if(task != null){
            task.run();
        }

        if(this.springForce != null){
            this.springForce.apply(particles);
        }

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
                    this.mouseParticle = new Particle(this.mousePosition, mouseMass);
                    Vector2 diff = mousePosition.sub(particle.getPosition());
                    this.springForce = new SpringForce(new SimulationParticle(selectedParticle), new SimulationParticle(mouseParticle), diff.length(), springConstant, springDamping);
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
        this.tasks.add(() -> {
            if(this.mouseParticle != null){
                this.mousePosition = scale.scaleToMeters(e.getPoint());
                this.mouseParticle.setPosition(this.mousePosition);
            }
        });
    }
}
