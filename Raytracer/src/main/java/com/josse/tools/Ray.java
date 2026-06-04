package com.josse.tools;

// A ray is like a laser beam: it starts at a point (origin) and travels in one straight direction.
// The raytracer fires one ray per pixel and follows it to find what it hits.
public class Ray {

    private Vector3D origin;
    private Vector3D direction;

    public Ray() {
        this.origin = new Vector3D(0, 0, 0);
        this.direction = new Vector3D(0, 0, 1);
    }

    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }

    // Returns the 3D point along the ray at distance t: origin + direction * t.
    public Vector3D pointAt(double t) {
        return origin.add(direction.scale(t));
    }

    public Vector3D getOrigin() { return origin; }
    public Vector3D getDirection() { return direction; }

    public void setOrigin(Vector3D origin) { this.origin = origin; }
    public void setDirection(Vector3D direction) { this.direction = direction.normalize(); }
}
