package org.example.editor;

import org.example.Vector2;

import java.io.Serializable;

public class EditorParticle implements Serializable {
    private Vector2 position;

    public EditorParticle(Vector2 position){
        this.position = position;
    }

    public Vector2 getPosition(){
        return position;
    }

    public void setPosition(Vector2 position){
        this.position = position;
    }
}
