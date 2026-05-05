package com.josse;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class Scene {

    private List<Object3D> objects;
    private List<Light> lights;
    private Color backgroundColor;
    private Camera camera;

    public Scene() {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.backgroundColor = Color.WHITE;
        this.camera = new Camera();
    }

    public Scene(Camera camera, Color backgroundColor) {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.camera = camera;
        this.backgroundColor = backgroundColor;
    }

    public void addObject(Object3D object) {
        this.objects.add(object);
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }

    public List<Object3D> getObjects() { return objects; }
    public List<Light> getLights() { return lights; }
    public Color getBackgroundColor() { return backgroundColor; }
    public Camera getCamera() { return camera; }

    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    public void setCamera(Camera camera) { this.camera = camera; }
}