package org.example.editor;

import org.example.Vector2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class AddSpringTool extends Tool{
    private EditorParticle startParticle;
    private Point mousePosition;

    public AddSpringTool(Editor editor) {
        super(editor);
    }

    @Override
    public void paint(Graphics g){
        if(startParticle == null || mousePosition == null){
            return;
        }

        Vector2 position = startParticle.getPosition();
        g.drawLine((int)position.getX(), (int)position.getY(), mousePosition.x, mousePosition.y);
    }

    @Override
    public void mousePressed(MouseEvent e){
        List<EditorParticle> particles = super.editor.getEditorParticleAt(new Vector2(e.getX(), e.getY()));
        if(particles.isEmpty()){
            return;
        }
        startParticle = particles.getFirst();
    }

    @Override
    public void mouseReleased(MouseEvent e){
        if(startParticle == null){
            return;
        }

        try{
            List<EditorParticle> particles = super.editor.getEditorParticleAt(new Vector2(e.getX(), e.getY()));
            if(particles.isEmpty()){
                return;
            }

            super.editor.addConnection(new Connection(startParticle, particles.getFirst()));
        }finally{
            startParticle = null;
            mousePosition = null;
            super.editor.getEditorPanel().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e){
        if(startParticle == null){
            return;
        }
        this.mousePosition = e.getPoint();
        super.editor.getEditorPanel().repaint();
    }
}
