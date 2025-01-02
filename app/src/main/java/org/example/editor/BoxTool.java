package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class BoxTool extends Tool{
    private java.util.List<Vector2> ghostParticles;
    private Vector2 ghostPosition = new Vector2(0,0);

    private int width = 4;
    private int height = 4;
    private boolean dialogOpen = false;
    private JToolBar toolbar;

    public BoxTool(Editor editor) {
        super(editor);
        this.ghostParticles = new ArrayList<>();
        this.toolbar = createToolbar();
    }
    @Override
    public JToolBar getToolBar(){
        return this.toolbar;
    }

    private JToolBar createToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));


        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(width, 1, 100, 1));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(height, 1, 100, 1));

        ChangeListener spinnerUpdates = ce -> {
            int width = (int)widthSpinner.getValue();
            int height = (int)heightSpinner.getValue();
            this.width = width;
            this.height = height;
            updateGhostParticles(this.ghostPosition, width, height, 20f);
        };

        widthSpinner.addChangeListener(spinnerUpdates);
        heightSpinner.addChangeListener(spinnerUpdates);

        toolBar.add(new JLabel("Width"));
        toolBar.add(widthSpinner);
        toolBar.add(new JLabel("Height"));
        toolBar.add(heightSpinner);

        return toolBar;
    }

    @Override
    public void paintFront(Graphics g){
        for(Vector2 particle : ghostParticles){
            g.setColor(Color.GRAY);
            editor.drawParticle(particle, g);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e){
        if(dialogOpen){
            return;
        }

        Vector2 topLeft = new Vector2(e.getX(), e.getY());
        updateGhostParticles(topLeft, this.width, this.height, 20f);
    }

    @Override
    public void mouseReleased(MouseEvent e){
        Vector2 topLeft = new Vector2(e.getX(), e.getY());

        if(e.getButton() == MouseEvent.BUTTON1){
            addParticles(topLeft, this.width, this.height, 20f);
        }
    }

    private void updateGhostParticles(Vector2 topLeft, int width, int height, float spacing){
        this.ghostPosition = topLeft;
        this.ghostParticles = getParticles(topLeft, width, height, spacing);
        super.editor.getEditorPanel().repaint();
    }

    private void addParticles(Vector2 topLeft, int width, int height, float spacing){
        editor.addMultipleParticles(getParticles(topLeft, width, height, spacing).stream().map(v -> super.editor.createEditorParticle(v)).toList());
        editor.getEditorPanel().repaint();
    }

    private java.util.List<Vector2> getParticles(Vector2 topLeft, int width, int height, float spacing){
        java.util.List<Vector2> result = new ArrayList<>(width * height);
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                result.add(new Vector2(spacing * x, spacing * y).add(topLeft));
            }
        }
        return result;
    }
}
