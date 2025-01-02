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
    private int width = 4;
    private int height = 4;
    private boolean dialogOpen = false;

    public BoxTool(Editor editor) {
        super(editor);
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
        if(dialogOpen){
            return;
        }

        Vector2 topLeft = new Vector2(e.getX(), e.getY());
        updateGhostParticles(topLeft, this.width, this.height, 20f);
    }

    @Override
    public void mouseReleased(MouseEvent e){
        Vector2 topLeft = new Vector2(e.getX(), e.getY());

        if(e.getButton() == MouseEvent.BUTTON3){
            configureBox(topLeft);
        }
        else if(e.getButton() == MouseEvent.BUTTON1){
            addParticles(topLeft, this.width, this.height, 20f);
        }
    }

    private void configureBox(Vector2 topLeft){
        JPanel panel = new JPanel();
        JDialog dialog = new JDialog();
        dialogOpen = true;

        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(width, 1, 100, 1));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(height, 1, 100, 1));

        updateGhostParticles(topLeft, width, height, 20f);
        ChangeListener spinnerUpdates = ce -> {
            int width = (int)widthSpinner.getValue();
            int height = (int)heightSpinner.getValue();
            updateGhostParticles(topLeft, width, height, 20f);
        };

        widthSpinner.addChangeListener(spinnerUpdates);
        heightSpinner.addChangeListener(spinnerUpdates);

        panel.add(new JLabel("Width"));
        panel.add(widthSpinner);
        panel.add(new JLabel("Height"));
        panel.add(heightSpinner);

        JButton submit = new JButton("Submit");
        submit.addActionListener(ae -> {
            this.width = (int)widthSpinner.getValue();
            this.height = (int)heightSpinner.getValue();
            ghostParticles.clear();
            dialog.dispose();
        });
        panel.add(submit);

        dialog.setTitle("Box tool");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                BoxTool.this.dialogOpen = false;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                BoxTool.this.dialogOpen = false;
            }
        });
        dialog.setMinimumSize(new Dimension(300, 70));
        dialog.add(panel);
        dialog.pack();
        dialog.setVisible(true);
        dialog.setLocationRelativeTo(editor);
    }

    private void updateGhostParticles(Vector2 topLeft, int width, int height, float spacing){
        this.ghostParticles = getParticles(topLeft, width, height, spacing);
        super.editor.getEditorPanel().repaint();
    }

    private void addParticles(Vector2 topLeft, int width, int height, float spacing){
        editor.getParticles().addAll(getParticles(topLeft, width, height, spacing));
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
