package org.example.editor;

import org.example.ImmutableList;
import org.example.Vector2;

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
        List<EditorParticle> removeParticles = new ArrayList<>();
        Vector2 mousePosition = new Vector2(e.getX(), e.getY());

        ImmutableList<EditorParticle> particles = super.editor.getEditorParticles();
        for (EditorParticle particle : particles){
            if(particle.getPosition().distance(mousePosition) <= deleteRadius){
                removeParticles.add(particle);
            }
        }
        editor.removeMultipleParticles(removeParticles);
        super.editor.getEditorPanel().repaint();
    }
}
