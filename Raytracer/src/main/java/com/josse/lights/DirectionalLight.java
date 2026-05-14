package com.josse.lights;

import com.josse.tools.Intersection;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class DirectionalLight extends Light {

    private Vector3D direction;

    public DirectionalLight(Vector3D direction, Color color, double intensity){
        super(color, intensity);
        this.direction = direction.normalize();
    }

    //The direction field already shows the ray from the light source to the point, 
    //so if we negate it, it will show the direction from the point to the light source.
    @Override
    public Vector3D getDirectionOfLight(Vector3D point) {
        return direction.scale(-1);
    }

    @Override
    public double getNDotL(Intersection intersection) {
        Vector3D lightDir = getDirectionOfLight(intersection.getPoint());
        return Math.max(0.0, intersection.getNormal().dot(lightDir));
    }

    @Override
    public double getMaxShadowDistance(Vector3D point) {
        return Double.POSITIVE_INFINITY;
    }
}
