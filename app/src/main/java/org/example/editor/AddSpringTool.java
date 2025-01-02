package org.example.editor;

import org.example.ImmutableList;
import org.example.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

public class AddSpringTool extends Tool{
    private EditorParticle startParticle;
    private Point mousePosition;
    private JToolBar toolBar;

    private JSpinner dampingConstantSpinner;
    private JSpinner springConstantSpinner;

    private static final float DEFAULT_SPRING_CONSTANT = 100;
    private static final float DEFAULT_DAMPING_CONSTANT = 10;

    public AddSpringTool(Editor editor) {
        super(editor);
        this.toolBar = createToolbar();
    }

    @Override
    public JToolBar getToolBar(){
        return this.toolBar;
    }

    private JToolBar createToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

        dampingConstantSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_DAMPING_CONSTANT, 0, Editor.MAX_DAMPING_CONSTANT, 1));
        toolBar.add(new JLabel("Damping constant"));
        toolBar.add(dampingConstantSpinner);



        springConstantSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_SPRING_CONSTANT, 0, Editor.MAX_SPRING_CONSTANT, 1));
        toolBar.add(new JLabel("Spring constant"));
        toolBar.add(springConstantSpinner);

        toolBar.addSeparator();

        JButton connectAll = new JButton("Connect all");
        connectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectAll();
            }
        });
        toolBar.add(connectAll);

        return toolBar;
    }

    private void connectAll(){
        ImmutableList<EditorParticle> selected = super.editor.getSelectedEditorParticles();
        if(selected.isEmpty()){
            return;
        }

        for(int i = 0; i < selected.size(); i++){
            EditorParticle curr = selected.get(i);
            for(int j = i + 1; j < selected.size(); j++){
                EditorParticle next = selected.get(j);
                super.editor.addConnection(new EditorSpring(curr, next, getSpringConstant(), getDampingConstant()));
            }
        }

        super.editor.getEditorPanel().repaint();
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

            super.editor.addConnection(new EditorSpring(startParticle, particles.getFirst(), getSpringConstant(), getDampingConstant()));
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

    private float getSpringConstant(){
        return (float)(double)springConstantSpinner.getValue();
    }
    private float getDampingConstant(){
        return (float)(double)dampingConstantSpinner.getValue();
    }
}
