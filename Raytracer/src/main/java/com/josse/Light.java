package com.josse;

import javafx.scene.paint.Color;

public class Light {

    private Vector3D direction;
    private Color color;
    private double intensity;

    public Light(Vector3D direction, Color color, double intensity) {
        this.direction = direction.normalize();
        this.color = color;
        this.intensity = intensity;
    }

    public Vector3D getDirection() {
        return direction;
    }

    public Color getColor() {
        return color;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setDirection(Vector3D direction) {
        this.direction = direction.normalize();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
