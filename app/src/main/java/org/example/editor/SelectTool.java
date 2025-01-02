package org.example.editor;

import org.example.ImmutableList;
import org.example.Vector2;
import org.example.simulation.Particle;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class SelectTool extends Tool{
    private Point starPosition;
    private Point mousePosition;

    public SelectTool(Editor editor) {
        super(editor);
        this.starPosition = null;
    }

    @Override
    public void paint(Graphics g){
        g.setColor(Color.BLACK);
        if(starPosition != null && mousePosition != null){
            Rectangle rectangle = getSelection();
            g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.editor.clearSelection();
        this.starPosition = e.getPoint();
        this.mousePosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mousePosition = e.getPoint();

        super.editor.clearSelection();
        Rectangle selection = getSelection();
        for (EditorParticle particle : super.editor.getEditorParticles()){
            Vector2 v = particle.getPosition();
            Point p = new Point((int)v.getX(), (int)v.getY());
            if(selection.contains(p)){
                super.editor.selectParticle(particle);
            }
        }

        super.editor.getEditorPanel().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.starPosition = null;
        this.mousePosition = null;
        super.editor.getEditorPanel().repaint();
    }

    private Rectangle getSelection(){
        int x1 = Math.min(starPosition.x, mousePosition.x);
        int y1 = Math.min(starPosition.y, mousePosition.y);
        int x2 = Math.max(starPosition.x, mousePosition.x);
        int y2 = Math.max(starPosition.y, mousePosition.y);

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }
}
