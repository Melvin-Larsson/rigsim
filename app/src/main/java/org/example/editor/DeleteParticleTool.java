package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DeleteParticleTool extends Tool{
    private float deleteRadius = 10;

    public DeleteParticleTool(Editor editor) {
        super(editor);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        List<Vector2> removeParticles = new ArrayList<>();
        Vector2 mousePosition = new Vector2(e.getX(), e.getY());

        List<Vector2> particles = super.editor.getParticles();
        for (Vector2 particle : particles){
            if(particle.distance(mousePosition) <= deleteRadius){
                removeParticles.add(particle);
            }
        }
        System.out.println("Removed " + removeParticles.size());
        particles.removeAll(removeParticles);
        super.editor.getEditorPanel().repaint();
    }
}
