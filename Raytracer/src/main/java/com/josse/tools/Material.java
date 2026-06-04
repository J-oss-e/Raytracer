package com.josse.tools;

// Describes how a surface interacts with light: how bright it glows in shadow (ambient), how rough or smooth
// it looks (diffuse/specular/shininess), whether it acts like a mirror (reflectivity), and whether light
// passes through it like glass (transparency + ior).
public class Material {
    public double ambient;
    public double diffuse;
    public double specular;
    public double shininess;
    public double reflectivity;
    public double transparency;
    public double ior; // Index of Refraction

    public Material(double ambient, double diffuse, double specular, double shininess, double reflectivity, double transparency, double ior) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.transparency = transparency;
        this.ior = ior;
    }

    public Material() {
        this(0.1, 0.7, 0.5, 32, 1, 0.0, 1.0);
    }

    public double getAmbient() {
        return ambient;
    }

    public double getDiffuse() {
        return diffuse;
    }

    public double getSpecular() {
        return specular;
    }

    public double getShininess() {
        return shininess;
    }

    public double getReflectivity() {
        return reflectivity;
    }

    public double getTransparency() {
        return transparency;
    }

    public double getIor() {
        return ior;
    }

}