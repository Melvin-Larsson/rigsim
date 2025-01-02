package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class AddParticleTool extends Tool{
    public AddParticleTool(Editor editor) {
        super(editor);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.editor.getParticles().add(new Vector2(e.getX(), e.getY()));
        super.editor.getEditorPanel().repaint();
    }
}
