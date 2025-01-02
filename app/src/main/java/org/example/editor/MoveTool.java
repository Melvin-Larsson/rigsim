package org.example.editor;

import org.example.ImmutableList;
import org.example.Vector2;

import java.awt.event.MouseEvent;
import java.util.List;

public class MoveTool extends Tool{
    private Vector2 lastPosition;
    private ImmutableList<EditorParticle> moveParticles;

    public MoveTool(Editor editor) {
        super(editor);
    }

    @Override
    public void mousePressed(MouseEvent e){
        this.lastPosition = new Vector2(e.getX(), e.getY());

        this.moveParticles = super.editor.getSelectedEditorParticles();
        if(moveParticles.isEmpty()){
            List<EditorParticle> atCursor = super.editor.getEditorParticleAt(this.lastPosition);
            if(!atCursor.isEmpty()) this.moveParticles = new ImmutableList<>(List.of(atCursor.get(0)));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e){
        Vector2 currPosition = new Vector2(e.getX(), e.getY());
        Vector2 diff = currPosition.sub(lastPosition);

        for(EditorParticle particle : moveParticles){
            particle.setPosition(particle.getPosition().add(diff));
        }

        this.lastPosition = currPosition;

        super.editor.getEditorPanel().repaint();
    }
}