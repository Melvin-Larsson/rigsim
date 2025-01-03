package org.example;

import java.awt.*;

public class Scale {
    private Vector2 pixelsPerMeter;

    public Scale(Vector2 pixelsPerMeter){
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public Scale(float pixelsPerMeterX, float pixelsPerMeterY){
        this.pixelsPerMeter = new Vector2(pixelsPerMeterX, pixelsPerMeterY);
    }

    public Scale(float scale){
        this.pixelsPerMeter = new Vector2(scale, scale);
    }

    public Vector2 scaleToMeters(Vector2 v){
        return new Vector2(v.getX() / pixelsPerMeter.getX(), v.getY() / pixelsPerMeter.getY());
    }
    public Vector2 scaleToMeters(Point p){
        return new Vector2(p.x / pixelsPerMeter.getX(), p.y / pixelsPerMeter.getY());
    }
    public Vector2 scaleToMeters(Dimension d){
        return new Vector2(d.width / pixelsPerMeter.getX(), d.height / pixelsPerMeter.getY());
    }

    public Vector2 scaleToPixels(Vector2 v){
        return new Vector2(v.getX() * pixelsPerMeter.getX(), v.getY() * pixelsPerMeter.getY());
    }

    public void setPixelsPerMeter(Vector2 pixelsPerMeter) {
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public Vector2 getPixelsPerMeter() {
        return pixelsPerMeter;
    }
    public Vector2 getMetersPerPixel(){
        return new Vector2(1 / pixelsPerMeter.getX(), 1 / pixelsPerMeter.getY());
    }
}
