package com.josse.lights;

import com.josse.tools.Intersection;

import javafx.scene.paint.Color;

public abstract class Light {

    private Color color;
    private double intensity;

    public abstract double getNDotL(Intersection intersection);

    public Light(Color color, double intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public Color getColor() {
        return color;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
