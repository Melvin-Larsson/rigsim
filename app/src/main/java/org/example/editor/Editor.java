package org.example.editor;

import org.example.ImmutableList;
import org.example.Scale;
import org.example.Vector2;
import org.example.simulation.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

public class Editor extends JPanel {
    private List<EditorParticle> particles;
    private List<EditorParticle> selectedParticles;
    private List<EditorSpring> editorSprings;

    private Tool currentTool = null;
    private JPanel toolPanel;
    private GridBagConstraints optionalToolPanelConstraints;
    private JToolBar currentToolbar = null;

    private float particleMass = 1;

    private Scale scale;

    private static final int radius = 5;

    private static final File RESOURCE_FOLDER = new File("src/main/resources/");
    private static final File ADD_PARTICLE_ICON = new File(RESOURCE_FOLDER, "add_particle_icon.png");
    private static final File DELETE_PARTICLE_ICON = new File(RESOURCE_FOLDER, "delete_particle_icon.png");
    private static final File SELECT_ICON = new File(RESOURCE_FOLDER, "select_icon.png");
    private static final File BOX_ICON = new File(RESOURCE_FOLDER, "box_icon.png");
    private static final File SPRING_ICON = new File(RESOURCE_FOLDER, "spring_icon.png");
    private static final File CIRCLE_ICON = new File(RESOURCE_FOLDER, "circle_icon.png");
    private static final File MOVE_ICON = new File(RESOURCE_FOLDER, "move_icon.png");

    private static final String TOOL_KEY = "tool";

    public static final float MAX_SPRING_CONSTANT = 10000;
    public static final float MAX_DAMPING_CONSTANT = 10000;

    public Editor(Scale scale){
        this.scale = scale;
        this.particles = new ArrayList<>();
        this.selectedParticles = new ArrayList<>();
        this.editorSprings = new ArrayList<>();

        this.setLayout(new BorderLayout());
        JPanel center = new JPanel(); //A bit of a hack to get a border
        center.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        this.add(center, BorderLayout.CENTER);

        this.toolPanel = new JPanel();
        this.toolPanel.setLayout(new GridBagLayout());
        this.add(toolPanel, BorderLayout.NORTH);
        ToolData[] tools = {new ToolData(ADD_PARTICLE_ICON, new AddParticleTool(this)),
                            new ToolData(DELETE_PARTICLE_ICON, new DeleteParticleTool(this)),
                            new ToolData(SELECT_ICON, new SelectTool(this)),
                            new ToolData(BOX_ICON, new BoxTool(this)),
                            new ToolData(SPRING_ICON, new AddSpringTool(this)),
                            new ToolData(CIRCLE_ICON, new CircleTool(this)),
                            new ToolData(MOVE_ICON, new MoveTool(this))};
        JToolBar toolBar = createToolbar(this, tools);
        toolBar.setFloatable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        this.toolPanel.add(toolBar, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        this.optionalToolPanelConstraints = gbc;

        this.setFocusable(true);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.getComponent().requestFocus();
            }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE){
                    Editor.this.removeMultipleParticles(selectedParticles);
                    Editor.this.clearSelection();
                    Editor.this.repaint();
                }
            }
        });
    }

    private JToolBar createToolbar(JPanel content, ToolData... toolData){
        JToolBar toolbar = new JToolBar();
        ButtonGroup toolGroup = new ButtonGroup();

        ActionListener actionListener = e -> {
            JToggleButton source = (JToggleButton) e.getSource();
            if(this.currentTool != null){
                content.removeMouseListener(this.currentTool);
                content.removeMouseMotionListener(this.currentTool);
                if(currentToolbar != null){
                    this.toolPanel.remove(currentToolbar);
                }
            }
            this.currentTool = (Tool)source.getClientProperty(TOOL_KEY);
            if(this.currentTool != null){
                this.currentTool.onSelect();
                content.addMouseListener(this.currentTool);
                content.addMouseMotionListener(this.currentTool);
                currentToolbar = this.currentTool.getToolBar();
                if(currentToolbar != null){
                    this.currentToolbar.setFloatable(false);
                    this.toolPanel.add(this.currentToolbar, this.optionalToolPanelConstraints);
                }
            }

            this.revalidate();
            this.repaint();
        };

        for (ToolData tool : toolData){
            JToggleButton button = new JToggleButton();
            button.setIcon(tool.icon);
            button.putClientProperty(TOOL_KEY, tool.tool);
            button.addActionListener(actionListener);
            toolbar.add(button);
            toolGroup.add(button);

        }

        return toolbar;
    }

    public void save(ObjectOutputStream oos) throws IOException {
        oos.writeObject(particles);
        oos.writeObject(editorSprings);
    }

    public void load(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        this.particles = (List<EditorParticle>) ois.readObject();
        this.editorSprings = (List<EditorSpring>) ois.readObject();
        this.clearSelection();
        this.repaint();
    }

    public void reset(){
        this.particles.clear();
        this.editorSprings.clear();
        this.clearSelection();
        this.repaint();
    }

    void drawParticle(EditorParticle particle, Graphics g){
        this.drawParticle(particle.getPosition(), g);
    }
    void drawParticle(Vector2 position, Graphics g){
        Point p = new Point((int)position.getX(), (int)position.getY());
        g.fillOval(p.x - radius, p.y - radius, radius * 2 + 1, radius * 2 + 1);
    }

    void drawConnection(EditorSpring editorSpring, Graphics g){
        Vector2 start = editorSpring.p1.getPosition();
        Vector2 end = editorSpring.p2.getPosition();
        g.drawLine((int)start.getX(), (int)start.getY(), (int)end.getX(), (int)end.getY());
    }

    EditorParticle createEditorParticle(Vector2 position){
        return EditorParticle.createMoveable(position);
    }

    public void initializeSystem(ParticleSystem system){
        List<Particle> particles = new ArrayList<>();
        Map<EditorParticle, Integer> mapping = new HashMap<>();
        for (EditorParticle particle : this.particles){
            mapping.put(particle, particles.size());
            particles.add(new Particle(scale.scaleToMeters(particle.getPosition()), particle.getMass()));
        }
        List<SimulationParticle> simulationParticles = ParticleFactory.createSimulationParticles(particles);
        system.addAllParticles(simulationParticles);

        for (EditorParticle particle : this.particles){
            if(!particle.isMoveable()){
                int index = mapping.get(particle);
                system.addConstraints(new ImmovableConstraint(simulationParticles.get(index)));
            }
        }

//        for (int i = 0; i < Math.min(simulationParticles.size(), 2); i++){
//            system.addConstraints(new ImmovableConstraint(simulationParticles.get(i)));
//        }

        for (EditorSpring editorSpring : editorSprings){
            SimulationParticle p1 = simulationParticles.get(mapping.get(editorSpring.p1));
            SimulationParticle p2 = simulationParticles.get(mapping.get(editorSpring.p2));
            Vector2 diff = p2.getPosition().sub(p1.getPosition());
            if(editorSpring.isStiff){
                system.addConstraints(new DistanceConstraint(p1, p2, diff.length()));

                //Hack to get stiff constraints to display
                SpringForce force = new SpringForce(p1, p2, diff.length(), 0, 0);
                system.addForce(force);
            }else{
                SpringForce force = new SpringForce(p1, p2, diff.length(), editorSpring.springConstant, editorSpring.dampingConstant);
                system.addForce(force);
            }
        }
    }

    ImmutableList<EditorParticle> getEditorParticles(){
        return new ImmutableList<>(this.particles);
    }
    List<EditorParticle> getEditorParticleAt(Vector2 position){
        float radius = 10;
        List<EditorParticle> result = new ArrayList<>();
        for(EditorParticle particle : this.particles){
            if(particle.getPosition().distance(position) < radius){
                result.add(particle);
            }
        }
        return result;
    }

    void addParticle(EditorParticle particle){
        this.particles.add(particle);
    }
    void addMultipleParticles(Collection<EditorParticle> particles){
        this.particles.addAll(particles);
    }
    void removeParticle(EditorParticle particle){
        this.particles.remove(particle);
        List<EditorSpring> removeEditorSprings = new LinkedList<>();
        for(EditorSpring editorSpring : editorSprings){
            if(editorSpring.p1 == particle || editorSpring.p2 == particle){
                removeEditorSprings.add(editorSpring);
            }
        }
        editorSprings.removeAll(removeEditorSprings);
    }
    void removeMultipleParticles(Collection<EditorParticle> particles){
        for(EditorParticle particle : particles){
            this.removeParticle(particle);
        }
    }

    ImmutableList<EditorParticle> getSelectedEditorParticles(){
        return new ImmutableList<>(this.selectedParticles);
    }
    void selectParticle(EditorParticle particle){
        this.selectedParticles.add(particle);
    }
    void selectMultipleParticles(Collection<EditorParticle> particles){
        this.selectedParticles.addAll(particles);
    }
    void deselectParticle(EditorParticle particle){
        this.selectedParticles.add(particle);
    }
    void deselectMultipleParticles(Collection<EditorParticle> particles){
        this.selectedParticles.removeAll(particles);
    }
    void clearSelection(){
        this.selectedParticles.clear();
    }

    void addConnection(EditorSpring editorSpring){
        EditorSpring existingSpring = null;
        for (EditorSpring spring : this.editorSprings){
            if(spring.p1 == editorSpring.p1 && spring.p2 == editorSpring.p2 ||
               spring.p2 == editorSpring.p1 && spring.p1 == editorSpring.p2){
                existingSpring = spring;
                break;
            }
        }

        if(existingSpring != null){
            this.editorSprings.remove(existingSpring);
        }

        this.editorSprings.add(editorSpring);
    }

    JPanel getEditorPanel(){
        return this;
    }

    private class ToolData{
        private final ImageIcon icon;
        private final Tool tool;

        private ToolData(File iconFile, Tool tool){
            this.icon = new ImageIcon(iconFile.getAbsolutePath());
            this.tool = tool;
        }
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);

        if(currentTool != null){
            currentTool.paintBack(g);
        }

        for (EditorParticle particle : particles){
            if(particle.isMoveable()){
                g.setColor(Color.GRAY);
            }else{
                g.setColor(Color.BLACK);
            }
            drawParticle(particle, g);
        }

        g.setColor(Color.BLACK);
        for(EditorSpring editorSpring : editorSprings){
            g.setColor(getSpringColor(editorSpring));
            drawConnection(editorSpring, g);
        }

        if(Editor.this.selectedParticles.size() > 0){
            System.out.println("Selected");
        }
        g.setColor(Color.BLUE);
        for (EditorParticle particle : selectedParticles){
            drawParticle(particle, g);
        }

        if(currentTool != null){
            currentTool.paintFront(g);
        }
    }

    private Color getSpringColor(EditorSpring spring){
        if(spring.isStiff){
            return Color.RED;
        }

        float springConstant = Math.min(spring.springConstant, MAX_SPRING_CONSTANT);
        int rg = (int)((255.0 / Math.log10(MAX_SPRING_CONSTANT)) * Math.log10(springConstant));

        float dampingConstant = Math.min(spring.dampingConstant, MAX_DAMPING_CONSTANT);
        int b = (int)(255 - (255.0 / Math.log10(MAX_DAMPING_CONSTANT)) * Math.log10(dampingConstant));

        return new Color(rg, rg, b);
    }
}
