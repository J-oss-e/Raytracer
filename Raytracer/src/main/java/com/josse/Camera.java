package com.josse;

public class Camera {

    private Vector3D position;
    private double fov;
    private int width; 
    private int height;

    public Camera() {
        this.position = new Vector3D(0, 0, 0);
        this.fov = 60.0;
        this.width = 400;
        this.height = 400;
    }

    public Camera(Vector3D position, double fov, int width, int height) {
        this.position = position;
        this.fov = fov;
        this.width = width;
        this.height = height;
    }

    public Ray generateRay(int x, int y) {
        double aspect = (double) width / (double) height;
        double scale = Math.tan(Math.toRadians(fov) * 0.5);
        double px = (2.0 * ((x + 0.5) / width) - 1.0) * aspect * scale;
        double py = (1.0 - 2.0 * ((y + 0.5) / height)) * scale;

        Vector3D dir = new Vector3D(px, py, 1.0).normalize();
        return new Ray(position, dir);
    }

    public Vector3D getPosition() { return position; }
    public double getFov() { return fov; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setPosition(Vector3D position) { this.position = position; }
    public void setFov(double fov) { this.fov = fov; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}
