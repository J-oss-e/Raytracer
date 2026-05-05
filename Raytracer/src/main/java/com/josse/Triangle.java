package com.josse;

import javafx.scene.paint.Color;

public class Triangle extends Object3D {

    private static final double EPSILON = 1e-8;

    private Vector3D v0;
    private Vector3D v1;
    private Vector3D v2;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(centroid(v0, v1, v2), color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    private static Vector3D centroid(Vector3D a, Vector3D b, Vector3D c) {
        return new Vector3D(
            (a.x + b.x + c.x) / 3.0,
            (a.y + b.y + c.y) / 3.0,
            (a.z + b.z + c.z) / 3.0
        );
    }

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D O = ray.getOrigin();
        Vector3D D = ray.getDirection();

        Vector3D v1v0 = v1.subtract(v0);
        Vector3D v2v0 = v2.subtract(v0);

        Vector3D P = D.cross(v1v0);

        double det = v2v0.dot(P);
        if (det > -EPSILON && det < EPSILON) return new Intersection();

        double invDet = 1.0 / det;
        Vector3D T = O.subtract(v0);

        double u = invDet * T.dot(P);
        if (u < 0.0 || u > 1.0) return new Intersection();

        Vector3D Q = T.cross(v2v0);

        double v = invDet * D.dot(Q);
        if (v < 0.0 || (u + v) > 1.0 + EPSILON) return new Intersection();

        double t = invDet * Q.dot(v1v0);
        if (t < EPSILON) return new Intersection();

        Vector3D hitPoint = ray.pointAt(t);
        return new Intersection(true, t, hitPoint, this);
    }

    @Override
    public Vector3D getNormal(Vector3D hitPoint) {
        // V = v1 - v0,  W = v0 - v2,  Normal = Normalize(V x W)
        Vector3D V = v1.subtract(v0);
        Vector3D W = v2.subtract(v0);
        return V.cross(W).normalize();
    }

    public Vector3D getV0() { return v0; }
    public Vector3D getV1() { return v1; }
    public Vector3D getV2() { return v2; }
}