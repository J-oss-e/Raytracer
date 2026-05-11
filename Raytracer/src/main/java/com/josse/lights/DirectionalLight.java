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

    @Override
    public double getNDotL(Intersection intersection) {
        return Math.max(0.0, intersection.getNormal().dot(direction.scale(-1)));
    }    
}
