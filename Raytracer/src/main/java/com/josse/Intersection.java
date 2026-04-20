package com.josse;

public class Intersection {

    private boolean hit;
    private double t;
    private Vector3D point;
    private Object3D object;

    public Intersection() {
        this.hit = false;
        this.t = Double.POSITIVE_INFINITY;
        this.point = null;
        this.object = null;
    }

    public Intersection(boolean hit, double t, Vector3D point, Object3D object) {
        this.hit = hit;
        this.t = t;
        this.point = point;
        this.object = object;
    }

    public boolean isHit() { return hit; }
    public double getT() { return t; }
    public Vector3D getPoint() { return point; }
    public Object3D getObject() { return object; }

    public void setHit(boolean hit) { this.hit = hit; }
    public void setT(double t) { this.t = t; }
    public void setPoint(Vector3D point) { this.point = point; }
    public void setObject(Object3D object) { this.object = object; }
}
