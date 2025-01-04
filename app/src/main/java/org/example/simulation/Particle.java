package org.example.simulation;

import org.example.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Particle{
    private final Vector2 startPosition;

    Vector2 position;
    Vector2 velocity;
    private float mass;
    List<Vector2> forces;

    public Particle(Vector2 position, float mass) {
        this.position = position;
        this.startPosition = position;
        this.velocity = new Vector2(0, 0);
        this.mass = mass;
        this.forces = new ArrayList<>();
    }

    void reset() {
        this.position = startPosition;
        this.velocity = new Vector2(0, 0);
        this.forces.clear();
    }

    void addForce(Vector2 force) {
        this.forces.add(force);
    }

    Vector2 getForceSum(){
        return this.forces.stream().reduce(new Vector2(0,0), Vector2::add);
    }

    void clearForces() {
        this.forces.clear();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getMass() {
        return mass;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }
}
