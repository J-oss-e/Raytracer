package com.josse.objects;

import com.josse.tools.Intersection;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class Sphere extends Object3D {

    private double radius;

    public Sphere() {
        super();
        this.radius = 1.0;
    }

    public Sphere(Vector3D position, double radius, Color color) {
        super(position, color);
        this.radius = radius;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        Vector3D L = this.position.subtract(ray.getOrigin());

        double tca = L.dot(ray.getDirection());
        if (tca < 0) return new Intersection();

        double d2 = L.dot(L) - tca * tca;
        double radius2 = radius * radius;
        if (d2 > radius2) return new Intersection();

        double thc = Math.sqrt(radius2 - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;

        if (t0 < 0) {
            t0 = t1;
            if (t0 < 0) return new Intersection();
        }

        Vector3D hitPoint = ray.pointAt(t0);
        Vector3D normal = hitPoint.subtract(this.position).normalize();
        if (normal.dot(ray.getDirection()) > 0) {
            normal = normal.scale(-1);
        }
        return new Intersection(true, t0, hitPoint, this, normal);
    }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
}