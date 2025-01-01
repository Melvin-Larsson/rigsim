package org.example;

import javax.swing.*;
import java.awt.*;

public class SystemRenderer extends JPanel {
    private Vector2[] state = new Vector2[0];
    private final Vector2 pixelsPerMeter;

    public SystemRenderer(Vector2 pixelsPerMeter){
        this.pixelsPerMeter = pixelsPerMeter;
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        for (Vector2 pos : state){
            Point p = scale(pos);
            g.fillOval(p.x - 1, p.y - 1, 3, 3);
        }
    }

    public void updateState(Vector2[] state){
        this.state = state;
        this.repaint();
    }

    private Point scale(Vector2 position){
        return new Point(Math.round(position.getX() * pixelsPerMeter.getX()), Math.round(position.getY() * pixelsPerMeter.getY()));
    }
}
