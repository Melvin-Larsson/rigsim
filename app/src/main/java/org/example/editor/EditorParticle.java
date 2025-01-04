package org.example.editor;

import org.example.Vector2;

import java.io.Serializable;

public class EditorParticle implements Serializable {
    public static final float DEFAULT_MASS = 1;

    private Vector2 position;
    private float mass;
    private boolean moveable;

    private EditorParticle(Vector2 position, float mass, boolean moveable){
        this.position = position;
        this.mass = mass;
        this.moveable = moveable;
    }

    public static EditorParticle createImmovable(Vector2 position){
        return new EditorParticle(position, DEFAULT_MASS, false);
    }

    public static EditorParticle createMoveable(Vector2 position, float mass){
        return new EditorParticle(position, mass, true);
    }

    public static EditorParticle createMoveable(Vector2 position){
        return new EditorParticle(position, DEFAULT_MASS, true);
    }

    public Vector2 getPosition(){
        return position;
    }

    public void setPosition(Vector2 position){
        this.position = position;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public boolean isMoveable(){
        return this.moveable;
    }
}
