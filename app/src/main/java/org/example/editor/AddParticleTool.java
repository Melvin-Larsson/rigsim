package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class AddParticleTool extends Tool{
    private final ParticleToolComponent particleToolComponent;
    private final JToolBar toolbar;

    public AddParticleTool(Editor editor) {
        super(editor);
        this.particleToolComponent = new ParticleToolComponent();
        this.toolbar = createToolbar(this.particleToolComponent);
    }

    @Override
    public JToolBar getToolBar(){
        return this.toolbar;
    }

    private JToolBar createToolbar(ParticleToolComponent particleToolComponent){
        JToolBar toolbar = new JToolBar();
        toolbar.add(particleToolComponent);

        return toolbar;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.editor.addParticle(this.particleToolComponent.createParticle(new Vector2(e.getX(), e.getY())));
        super.editor.getEditorPanel().repaint();
    }
}
