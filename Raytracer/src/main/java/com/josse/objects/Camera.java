package com.josse.objects;

import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

// Perspective camera. Generates one ray per pixel using a configurable field of view and look-at direction.
public class Camera {

    private Vector3D position;
    private double fov;
    private int width;
    private int height;
    private double near;
    private double far;

    // Camera basis — precomputed from look-at in constructor
    private Vector3D camRight;
    private Vector3D camUp;
    private Vector3D camForward;

    public Camera() {
        this.position = new Vector3D(0, 0, 0);
        this.fov = 60.0;
        this.width = 400;
        this.height = 400;
        this.near = 0.1;
        this.far = 1000.0;
        computeBasis(new Vector3D(0, 0, -1), new Vector3D(0, 1, 0));
    }

    public Camera(Vector3D position, double fov, int width, int height) {
        this(position, fov, width, height, 0.1, 1000.0);
    }

    public Camera(Vector3D position, double fov, int width, int height,
                  double near, double far) {
        // Default target: look straight down -Z from position
        this(position, position.add(new Vector3D(0, 0, -1)), fov, width, height, near, far);
    }

    public Camera(Vector3D position, Vector3D target, double fov, int width, int height) {
        this(position, target, fov, width, height, 0.1, 1000.0);
    }

    public Camera(Vector3D position, Vector3D target, double fov, int width, int height,
                  double near, double far) {
        this.position = position;
        this.fov = fov;
        this.width = width;
        this.height = height;
        this.near = near;
        this.far = far;
        computeBasis(target, new Vector3D(0, 1, 0));
    }

    // Builds the orthonormal camera basis from a target point and a world-up hint.
    // forward = direction the camera looks; right and up define the film plane.
    private void computeBasis(Vector3D target, Vector3D worldUp) {
        camForward = target.subtract(position).normalize();
        camRight   = camForward.cross(worldUp).normalize();
        camUp      = camRight.cross(camForward).normalize();
    }

    // Converts pixel coordinates (x, y) to a 3D ray through the scene in world space.
    public Ray generateRay(int x, int y) {
        double aspect = (double) width / (double) height;
        double scale  = Math.tan(Math.toRadians(fov) * 0.5);
        // Convert the pixel's screen position to a point on an imaginary flat "film" in front of the camera.
        // The aspect ratio keeps proportions correct; scale controls the zoom level (determined by FOV).
        double px = (2.0 * ((x + 0.5) / width)  - 1.0) * aspect * scale;
        double py = (1.0 - 2.0 * ((y + 0.5) / height)) * scale; // Y is flipped: screen-space down → world-space up

        // Map the pixel offset into world space using the camera basis
        Vector3D dir = camRight.scale(px)
                               .add(camUp.scale(py))
                               .add(camForward)
                               .normalize();
        return new Ray(position, dir);
    }

    public Vector3D getPosition() { return position; }
    public double getFov() { return fov; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getNear() { return near; }   // NEW
    public double getFar() { return far; }     // NEW

    public void setPosition(Vector3D position) { this.position = position; }
    public void setFov(double fov) { this.fov = fov; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setNear(double near) { this.near = near; } // NEW
    public void setFar(double far) { this.far = far; }     // NEW
}