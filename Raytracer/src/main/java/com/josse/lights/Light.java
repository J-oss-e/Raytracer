package com.josse.lights;

import com.josse.tools.Intersection;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public abstract class Light {

    private Color color;
    private double intensity;

    public abstract double getNDotL(Intersection intersection);
    public abstract Vector3D getDirectionOfLight(Vector3D point);
    public abstract double getMaxShadowDistance(Vector3D point);
    public abstract double getAttenuation(Vector3D point);

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
