package com.josse.objects;

import com.josse.tools.IIntersectable;
import com.josse.tools.Intersection;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class Triangle extends Object3D implements IIntersectable {

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
    public Intersection getIntersection(Ray ray) {
        Vector3D rayOrigin = ray.getOrigin(); // the camera position — where the ray starts
        Vector3D rayDirection = ray.getDirection(); // the direction the ray is traveling

        Vector3D edge1 = v1.subtract(v0); // edge from v0 to v1
        Vector3D edge2 = v2.subtract(v0); // edge from v0 to v2

        Vector3D dirCrossEdge1 = rayDirection.cross(edge1);
        //cross(dir, edge1) — helper to calculate determinant and U parameter

        double det = edge2.dot(dirCrossEdge1);
        if (det > -EPSILON && det < EPSILON) return new Intersection();
        //Is the ray parallel to the triangle plane? If so, no hit occurs.

        double invDet = 1.0 / det;
        Vector3D toOrigin = rayOrigin.subtract(v0);

        double u = invDet * toOrigin.dot(dirCrossEdge1);
        if (u < 0.0 || u > 1.0) return new Intersection();
        //Is the intersection point outside of the triangle? If so, no hit occurs.

        Vector3D originCrossEdge2 = toOrigin.cross(edge2);
        //cross(toOrigin, edge2) — helper to calculate V parameter

        double v = invDet * rayDirection.dot(originCrossEdge2);
        if (v < 0.0 || (u + v) > 1.0 + EPSILON) return new Intersection();
        //Is the intersection point outside of the triangle? If so, no hit occurs.

        double t = invDet * originCrossEdge2.dot(edge1);
        if (t < EPSILON) return new Intersection();
        //Is the intersection point behind the ray origin? If so, no hit occurs.
        
        Vector3D normal = edge1.cross(edge2).normalize();

        Vector3D hitPoint = ray.pointAt(t);
        return new Intersection(true, t, hitPoint, this, normal);
    }

    public Vector3D getV0() { return v0; }
    public Vector3D getV1() { return v1; }
    public Vector3D getV2() { return v2; }
}