package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class CircleTool extends Tool{
    private boolean dialogOpen;
    private java.util.List<Vector2> ghostParticles;

    private float radius;
    private int count;

    public CircleTool(Editor editor) {
        super(editor);

        dialogOpen = false;
        this.ghostParticles = new ArrayList<>();
        this.radius = 50;
        this.count = 10;
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
        if(dialogOpen){
            return;
        }

        Vector2 topLeft = new Vector2(e.getX(), e.getY());
        updateGhostParticles(topLeft, this.radius, this.count);
    }

    @Override
    public void mouseReleased(MouseEvent e){
        Vector2 center = new Vector2(e.getX(), e.getY());

        if(e.getButton() == MouseEvent.BUTTON3){
            configureCircle(center);
        }
        else if(e.getButton() == MouseEvent.BUTTON1){
            addParticles(center, this.radius, this.count);
        }
    }

    private void configureCircle(Vector2 center){
        JPanel panel = new JPanel();
        JDialog dialog = new JDialog();
        dialogOpen = true;

        JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(radius, 1, 1000, 1));
        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(count, 2, 100, 1));

        updateGhostParticles(center, radius, count);
        ChangeListener spinnerUpdates = ce -> {
            float radius = (float)(double)radiusSpinner.getValue(); //Must cast Object (Double) to double before casting to float
            int count = (int)countSpinner.getValue();
            updateGhostParticles(center, radius, count);
        };

        radiusSpinner.addChangeListener(spinnerUpdates);
        countSpinner.addChangeListener(spinnerUpdates);

        panel.add(new JLabel("Radius"));
        panel.add(radiusSpinner);
        panel.add(new JLabel("Count"));
        panel.add(countSpinner);

        JButton submit = new JButton("Submit");
        submit.addActionListener(ae -> {
            this.radius = (float)(double)radiusSpinner.getValue(); //Must cast Object (Double) to double before casting to float
            this.count = (int)countSpinner.getValue();
            ghostParticles.clear();
            dialog.dispose();
        });
        panel.add(submit);

        dialog.setTitle("Box tool");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CircleTool.this.dialogOpen = false;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                CircleTool.this.dialogOpen = false;
            }
        });
        dialog.setMinimumSize(new Dimension(300, 70));
        dialog.add(panel);
        dialog.pack();
        dialog.setVisible(true);
        dialog.setLocationRelativeTo(editor);
    }

    private void updateGhostParticles(Vector2 center, float radius, int count){
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
