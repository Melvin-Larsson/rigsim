package org.example.editor;

import org.example.Vector2;

import java.awt.event.MouseEvent;

public class AddParticleTool extends Tool{
    public AddParticleTool(Editor editor) {
        super(editor);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.editor.addParticle((super.editor.createEditorParticle(new Vector2(e.getX(), e.getY()))));
        super.editor.getEditorPanel().repaint();
    }
}
