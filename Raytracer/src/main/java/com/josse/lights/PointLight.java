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
    public Vector3D getDirectionOfLight(Vector3D point) {
        return position.subtract(point).normalize();
    }

    @Override
    public double getNDotL(Intersection intersection) {
        Vector3D lightDir = getDirectionOfLight(intersection.getPoint());
        return Math.max(0.0, intersection.getNormal().dot(lightDir));
    }

    @Override
    public double getMaxShadowDistance(Vector3D point) {
        return position.subtract(point).length();
    }

    @Override
    public double getAttenuation(Vector3D point) {
        double d = position.subtract(point).length();
        return 1.0 / Math.max(d, 1e-4);
    }
}
