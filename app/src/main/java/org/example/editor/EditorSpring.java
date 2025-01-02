package org.example.editor;

import java.io.Serializable;

class EditorSpring implements Serializable {
    final EditorParticle p1;
    final EditorParticle p2;
    final float springConstant;
    final float dampingConstant;

    public EditorSpring(EditorParticle p1, EditorParticle p2, float springConstant, float dampingConstant){
        this.p1 = p1;
        this.p2 = p2;
        this.springConstant = springConstant;
        this.dampingConstant = dampingConstant;
    }
}
