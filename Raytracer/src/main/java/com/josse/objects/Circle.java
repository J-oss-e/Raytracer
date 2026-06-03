package com.josse.objects;

import com.josse.tools.AABB;
import com.josse.tools.Intersection;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class Circle extends Object3D {

    private double radius;
    private Vector3D normal; // unit normal of the plane the disk lies in

    public Circle() {
        super();
        this.radius = 1.0;
        this.normal = new Vector3D(0, 1, 0);
    }

    public Circle(Vector3D position, double radius, Vector3D normal, Color color) {
        super(position, color);
        this.radius = radius;
        this.normal = normal.normalize();
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        double denom = normal.dot(ray.getDirection());
        if (Math.abs(denom) < 1e-8) return new Intersection(); // ray parallel to disk plane

        double t = normal.dot(position.subtract(ray.getOrigin())) / denom;
        if (t < 0) return new Intersection();

        Vector3D hitPoint = ray.pointAt(t);
        Vector3D diff = hitPoint.subtract(position);
        if (diff.dot(diff) > radius * radius) return new Intersection(); // outside disk

        // flip normal toward the incoming ray
        Vector3D outNormal = denom < 0 ? normal : new Vector3D(-normal.x, -normal.y, -normal.z);
        return new Intersection(true, t, hitPoint, this, outNormal);
    }

    @Override
    public AABB getBoundingBox() {
        // per-axis half-extent of the disk: r * sqrt(1 - n_i^2)
        double ex = radius * Math.sqrt(1.0 - normal.x * normal.x);
        double ey = radius * Math.sqrt(1.0 - normal.y * normal.y);
        double ez = radius * Math.sqrt(1.0 - normal.z * normal.z);
        Vector3D min = new Vector3D(position.x - ex, position.y - ey, position.z - ez);
        Vector3D max = new Vector3D(position.x + ex, position.y + ey, position.z + ez);
        return new AABB(min, max);
    }

    public double getRadius() { return radius; }
    public Vector3D getNormal() { return normal; }
    public void setRadius(double radius) { this.radius = radius; }
    public void setNormal(Vector3D normal) { this.normal = normal.normalize(); }
}
