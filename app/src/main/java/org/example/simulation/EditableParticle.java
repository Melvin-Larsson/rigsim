package org.example.simulation;

import org.example.Vector2;

public interface EditableParticle {
    void setPosition(Vector2 position);
    Vector2 getPosition();

    void setMass(float mass);
    float getMass();
}
