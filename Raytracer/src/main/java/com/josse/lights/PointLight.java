package com.josse.lights;

import com.josse.tools.Intersection;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class PointLight extends Light {
    private Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    @Override
    public double getNDotL(Intersection intersection) {
        Vector3D lightDir = position.subtract(intersection.getPoint()).normalize();
        return Math.max(0.0, intersection.getNormal().dot(lightDir));
    }
    
}
