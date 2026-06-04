package com.josse.lights;

import com.josse.tools.Intersection;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

// A light that radiates from a single point in space. Brightness falls off with distance.
public class PointLight extends Light {
    private Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    // Returns the normalized direction from the surface point toward the light position.
    @Override
    public Vector3D getDirectionOfLight(Vector3D point) {
        return position.subtract(point).normalize();
    }

    // Lambert shading term: how much the surface faces the light (clamped to 0 if facing away).
    @Override
    public double getNDotL(Intersection intersection) {
        Vector3D lightDir = getDirectionOfLight(intersection.getPoint());
        return Math.max(0.0, intersection.getNormal().dot(lightDir));
    }

    // Shadow rays stop here — the light is at this distance so there's no need to look further.
    @Override
    public double getMaxShadowDistance(Vector3D point) {
        return position.subtract(point).length();
    }

    // Brightness falls off linearly with distance. Clamped at 1e-4 to avoid division by zero.
    @Override
    public double getAttenuation(Vector3D point) {
        double d = position.subtract(point).length();
        return 1.0 / Math.max(d, 1e-4);
    }
}
