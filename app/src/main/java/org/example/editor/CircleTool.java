package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class CircleTool extends Tool{
    private JToolBar toolbar;

    private java.util.List<Vector2> ghostParticles;
    private Vector2 ghostCenter = new Vector2(0,0);

    private float radius;
    private int count;

    public CircleTool(Editor editor) {
        super(editor);

        this.radius = 50;
        this.count = 10;
        this.toolbar = createToolbar();
        this.ghostParticles = new ArrayList<>();
    }

    @Override
    public void paint(Graphics g){
        for(Vector2 particle : ghostParticles){
            g.setColor(Color.GRAY);
            editor.drawParticle(particle, g);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e){
        Vector2 center = new Vector2(e.getX(), e.getY());
        updateGhostParticles(center, this.radius, this.count);
    }

    @Override
    public void mouseReleased(MouseEvent e){
        Vector2 center = new Vector2(e.getX(), e.getY());

        if(e.getButton() == MouseEvent.BUTTON1){
            addParticles(center, this.radius, this.count);
        }
    }

    @Override
    public JToolBar getToolBar(){
        return this.toolbar;
    }

    private JToolBar createToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

        JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(radius, 1, 1000, 1));
        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(count, 2, 100, 1));

        ChangeListener spinnerUpdates = ce -> {
            this.radius = (float)(double)radiusSpinner.getValue(); //Must cast Object (Double) to double before casting to float
            this.count = (int)countSpinner.getValue();
            updateGhostParticles(this.ghostCenter, this.radius, this.count);
        };
        radiusSpinner.addChangeListener(spinnerUpdates);
        countSpinner.addChangeListener(spinnerUpdates);

        toolBar.add(new JLabel("Radius"));
        toolBar.add(radiusSpinner);
        toolBar.add(new JLabel("Count"));
        toolBar.add(countSpinner);

        return toolBar;
    }

    private void configureCircle(Vector2 center){
    }

    private void updateGhostParticles(Vector2 center, float radius, int count){
        this.ghostCenter = center;
        this.ghostParticles = getParticles(center, radius, count);
        super.editor.getEditorPanel().repaint();
    }

    private void addParticles(Vector2 center, float radius, int count){
        editor.addMultipleParticles(getParticles(center, radius, count).stream().map(v -> super.editor.createEditorParticle(v)).toList());
        editor.getEditorPanel().repaint();
    }

    private java.util.List<Vector2> getParticles(Vector2 center, float radius, int count){
        java.util.List<Vector2> result = new ArrayList<>(count);
        float angle = (float)Math.PI * 2 / count;
        for(int i = 0; i < count; i++){
            Vector2 pos = new Vector2((float)Math.cos(angle * i) * radius, (float)Math.sin(angle * i) * radius).add(center);
            result.add(pos);
        }
        return result;
    }
}
