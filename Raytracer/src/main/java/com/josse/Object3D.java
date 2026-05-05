package com.josse;

import javafx.scene.paint.Color;

public abstract class Object3D {

    protected Vector3D position;
    protected Color color;

    public Object3D() {
        this.position = new Vector3D(0, 0, 0);
        this.color = Color.WHITE;
    }

    public Object3D(Vector3D position, Color color) {
        this.position = position;
        this.color = color;
    }

    public abstract Intersection intersect(Ray ray);

    public abstract Vector3D getNormal(Vector3D hitPoint);

    public Vector3D getPosition() { return position; }
    public Color getColor() { return color; }

    public void setPosition(Vector3D position) { this.position = position; }
    public void setColor(Color color) { this.color = color; }
}