package org.example.editor;

import org.example.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ParticleToolComponent extends JPanel{

    private JSpinner weightSpinner;
    private JCheckBox moveableCheckBox;

    public ParticleToolComponent(){
        this.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        this.weightSpinner = new JSpinner(new SpinnerNumberModel(EditorParticle.DEFAULT_MASS, 0, 100, 1));
        JLabel weightLabel = new JLabel("Weight");

        this.moveableCheckBox = new JCheckBox("Moveable");
        this.moveableCheckBox.setSelected(true);
        this.moveableCheckBox.addActionListener(e -> {
            this.weightSpinner.setEnabled(this.moveableCheckBox.isSelected());
            weightLabel.setEnabled(this.moveableCheckBox.isSelected());

            ParticleToolComponent.this.repaint();
        });

        this.add(moveableCheckBox);
        this.add(weightLabel);

        this.add(weightSpinner);
    }

    public EditorParticle createParticle(Vector2 position){
        if(moveableCheckBox.isSelected()){
            return EditorParticle.createMoveable(position, (float)(double)weightSpinner.getValue());
        }
        return EditorParticle.createImmovable(position);
    }
}
