package org.example.editor;

import org.example.ImmutableList;

public class ConnectAllTool extends Tool{
    public ConnectAllTool(Editor editor) {
        super(editor);
    }

    @Override
    public void onSelect(){
        ImmutableList<EditorParticle> selected = super.editor.getSelectedEditorParticles();

        for(int i = 0; i < selected.size(); i++){
            EditorParticle curr = selected.get(i);
            for(int j = i + 1; j < selected.size(); j++){
                EditorParticle next = selected.get(j);
                super.editor.addConnection(new Connection(curr, next));
            }
        }

        super.editor.clearSelection();
    }

}
