package org.example.editor;

import org.example.ImmutableList;
import org.example.Pair;
import org.example.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AddSpringTool extends Tool{
    private EditorParticle startParticle;
    private Point mousePosition;
    private JToolBar toolBar;

    private JSpinner dampingConstantSpinner;
    private JSpinner springConstantSpinner;
    private JCheckBox stiffCheckbox;

    private JSpinner connectionRadiusSpinner;
    private JCheckBox connectNearbyCheckBox;

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

    @Override
    public void onSelect(){
        float margin = 0.1f;

        List<Float> distances = getSortedNeighbours().stream().map(this::distance).map(v -> (float)Math.ceil(v * 1/margin) * margin).distinct().toList();
        if(distances.isEmpty()){
            distances = List.of(0f);
        }

        this.connectionRadiusSpinner.setModel(new SpinnerListModel(distances));
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

        this.stiffCheckbox = new JCheckBox("Stiff");
        toolBar.add(stiffCheckbox);

        toolBar.addSeparator();

        JLabel connectRadiusLabel = new JLabel("Radius");
        connectRadiusLabel.setVisible(false);
        this.connectionRadiusSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        this.connectionRadiusSpinner.addChangeListener(e -> {
            super.editor.getEditorPanel().repaint();
        });
        connectionRadiusSpinner.setVisible(false);

        this.connectNearbyCheckBox = new JCheckBox("Connect nearby");
        this.connectNearbyCheckBox.addActionListener(e -> {
            connectionRadiusSpinner.setVisible(this.connectNearbyCheckBox.isSelected());
            connectRadiusLabel.setVisible(this.connectNearbyCheckBox.isSelected());
            toolBar.repaint();
            super.editor.getEditorPanel().repaint();
        });
        toolBar.add(this.connectNearbyCheckBox);

        toolBar.add(connectRadiusLabel);
        toolBar.add(connectionRadiusSpinner);

        JButton connectAll = new JButton("Connect all");
        connectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectMultiple();
            }
        });
        toolBar.add(connectAll);
        return toolBar;
    }

    private void connectMultiple(){
        List<Pair<EditorParticle, EditorParticle>> toConnect;
        if(shouldConnectNearby()){
            toConnect = getNeighbours(getConnectionRadius());
        }
        else{
            toConnect = getAllParis();
        }

        for(Pair<EditorParticle, EditorParticle> pair : toConnect){
            addConnection(pair.v1, pair.v2);
        }

        super.editor.getEditorPanel().repaint();
    }

    private void addConnection(EditorParticle p1, EditorParticle p2){
        if(stiffCheckbox.isSelected()){
            super.editor.addConnection(new EditorSpring(p1, p2));
        }
        else{
            super.editor.addConnection(new EditorSpring(p1, p2, getSpringConstant(), getDampingConstant()));
        }
    }


    @Override
    public void paintFront(Graphics g){
        if(shouldConnectNearby()){
            ImmutableList<EditorParticle> selected = super.editor.getSelectedEditorParticles();
            float radius = getConnectionRadius();
            g.setColor(new Color(0, 0, 1, 0.5f));

            List<Pair<EditorParticle, EditorParticle>> neighbours = getNeighbours(radius);
            for(Pair<EditorParticle, EditorParticle> neighbourPair : neighbours){
                Vector2 v1 = neighbourPair.v1.getPosition();
                Vector2 v2 = neighbourPair.v2.getPosition();

                g.drawLine((int)v1.getX(), (int) v1.getY(), (int)v2.getX(), (int)v2.getY());
            }
        }

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

            addConnection(startParticle, particles.getFirst());
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

    private List<Pair<EditorParticle, EditorParticle>> getNeighbours(float radius){
        ImmutableList<EditorParticle> selected = super.editor.getSelectedEditorParticles();
        List<Pair<EditorParticle, EditorParticle>> result = new ArrayList<>();

        for(int i = 0; i < selected.size(); i++){
            EditorParticle curr = selected.get(i);
            Vector2 v1 = curr.getPosition();
            for(int j = i + 1; j < selected.size(); j++){
                EditorParticle next = selected.get(j);
                Vector2 v2 = next.getPosition();

                if(v1.distance(v2) <= radius){
                    result.add(new Pair<EditorParticle, EditorParticle>(curr, next));
                }
            }
        }
         return result;
    }

    private List<Pair<EditorParticle, EditorParticle>> getAllParis(){
        ImmutableList<EditorParticle> selected = super.editor.getSelectedEditorParticles();
        List<Pair<EditorParticle, EditorParticle>> result = new ArrayList<>();

        for(int i = 0; i < selected.size(); i++){
            EditorParticle curr = selected.get(i);
            for(int j = i + 1; j < selected.size(); j++){
                EditorParticle next = selected.get(j);
                result.add(new Pair<EditorParticle, EditorParticle>(curr, next));
            }
        }
        return result;
    }

    private List<Pair<EditorParticle, EditorParticle>> getSortedNeighbours(){
        var pairs = getAllParis();
        pairs.sort((p1, p2) -> {
            float d1 = distance(p1);
            float d2 = distance(p2);

            return Float.compare(d1, d2);
        });

        return pairs;
    }

    private float distance(Pair<EditorParticle, EditorParticle> pair){
        return pair.v1.getPosition().distance(pair.v2.getPosition());
    }

    private float getSpringConstant(){
        return (float)(double)springConstantSpinner.getValue();
    }
    private float getDampingConstant(){
        return (float)(double)dampingConstantSpinner.getValue();
    }
    private boolean shouldConnectNearby(){
        return connectNearbyCheckBox.isSelected();
    }
    private float getConnectionRadius(){
        return (float)connectionRadiusSpinner.getValue();
    }
}
