package org.example;

import java.io.Serializable;

public class Vector2 implements Serializable {
    private final float x;
    private final float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vector2 add(Vector2 other){
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 sub(Vector2 other){
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 mul(float constant){
        return new Vector2(this.x * constant, this.y * constant);
    }

    public Vector2 div(float constant){
        return new Vector2(this.x / constant, this.y / constant);
    }

    public float dot(Vector2 v2){
        return this.x * v2.x + this.y * v2.y;
    }

    public float length(){
        return (float)Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public float distance(Vector2 other){
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2 normalized(){
        return this.div(this.length());
    }

    @Override
    public String toString(){
        return "(" + this.x + ", " + this.y + ")";
    }
}
