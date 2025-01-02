package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Editor extends JPanel {
    private List<Vector2> particles;
    private List<Vector2> selectedParticles;
    private Tool currentTool = null;
    private JPanel content;

    private Vector2 pixelsPerMeter;

    private static final int radius = 5;

    private static final File RESOURCE_FOLDER = new File("src/main/resources/");
    private static final File ADD_PARTICLE_ICON = new File(RESOURCE_FOLDER, "add_particle_icon.png");
    private static final File DELETE_PARTICLE_ICON = new File(RESOURCE_FOLDER, "delete_particle_icon.png");
    private static final File SELECT_ICON = new File(RESOURCE_FOLDER, "select_icon.png");
    private static final File BOX_ICON = new File(RESOURCE_FOLDER, "box_icon.png");
    private static final File SPRING_ICON = new File(RESOURCE_FOLDER, "spring_icon.png");

    private static final String TOOL_KEY = "tool";

    public Editor(Vector2 pixelsPerMeter){
        this.pixelsPerMeter = pixelsPerMeter;
        this.particles = new ArrayList<>();
        this.selectedParticles = new ArrayList<>();

        this.setLayout(new BorderLayout());

        this.content = new JPanel(){
            @Override
            public void paint(Graphics g){
                super.paint(g);
                g.setColor(Color.BLACK);
                for (Vector2 particle : particles){
                    drawParticle(particle, g);
                }

                if(Editor.this.selectedParticles.size() > 0){
                    System.out.println("Selected");
                }
                g.setColor(Color.BLUE);
                for (Vector2 particle : selectedParticles){
                    drawParticle(particle, g);
                }

                if(currentTool != null){
                    currentTool.paint(g);
                }
            }
        };
        this.add(this.content, BorderLayout.CENTER);

        ToolData[] tools = {new ToolData(ADD_PARTICLE_ICON, new AddParticleTool(this)),
                            new ToolData(DELETE_PARTICLE_ICON, new DeleteParticleTool(this)),
                            new ToolData(SELECT_ICON, new SelectTool(this)),
                            new ToolData(BOX_ICON, new BoxTool(this))};
        this.add(createToolbar(this.content, tools), BorderLayout.NORTH);

        content.setFocusable(true);
        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.getComponent().requestFocus();
            }
        });
        content.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE){
                    particles.removeAll(selectedParticles);
                    selectedParticles.clear();
                    content.repaint();
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
            }
            this.currentTool = (Tool)source.getClientProperty(TOOL_KEY);
            if(this.currentTool != null){
                this.currentTool.onSelect();
                content.addMouseListener(this.currentTool);
                content.addMouseMotionListener(this.currentTool);
            }
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

    public void drawParticle(Vector2 particle, Graphics g){
        Point p = new Point((int)particle.getX(), (int)particle.getY());
        g.fillOval(p.x - radius, p.y - radius, radius * 2 + 1, radius * 2 + 1);
    }

    public List<Vector2> getParticles(){
        return this.particles;
    }

    public List<Vector2> getSelectedParticles(){
        return this.selectedParticles;
    }

    public JPanel getEditorPanel(){
        return this.content;
    }

    private class ToolData{
        private final ImageIcon icon;
        private final Tool tool;

        private ToolData(File iconFile, Tool tool){
            this.icon = new ImageIcon(iconFile.getAbsolutePath());
            this.tool = tool;
        }
    }


}
