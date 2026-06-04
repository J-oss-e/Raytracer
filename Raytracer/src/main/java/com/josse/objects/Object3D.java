package com.josse.objects;

import com.josse.tools.AABB;
import com.josse.tools.Intersection;
import com.josse.tools.Material;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

// Abstract base for all renderable 3D objects. Every object must implement ray intersection and bounding box.
public abstract class Object3D {

    protected Vector3D position;
    protected Color color;
    protected Material material;

    public abstract Intersection getIntersection(Ray ray);

    public Object3D() {
        this.position = new Vector3D(0, 0, 0);
        this.color = Color.WHITE;
        this.material = new Material();
    }

    public Object3D(Vector3D position, Color color) {
        this.position = position;
        this.color = color;
        this.material = new Material();
    }

    public Object3D(Vector3D position, Color color, Material material) {
        this.position = position;
        this.color = color;
        this.material = material;
    }

    public Vector3D getPosition() { return position; }
    public Color getColor() { return color; }
    public Material getMaterial() { return material; }

    public void setPosition(Vector3D position) { this.position = position; }
    public void setColor(Color color) { this.color = color; }
    public void setMaterial(Material material) { this.material = material; }

    public abstract AABB getBoundingBox();
}