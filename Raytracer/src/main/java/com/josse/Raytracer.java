package com.josse;

import com.josse.lights.DirectionalLight;
import com.josse.lights.Light;
import com.josse.lights.PointLight;
import com.josse.objects.Camera;
import com.josse.objects.Object3D;
import com.josse.objects.Sphere;
import com.josse.objects.Triangle;
import com.josse.tools.Intersection;
import com.josse.tools.Material;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Raytracer extends Application {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        Scene world = buildScene();

        WritableImage image = render(world);

        ImageView view = new ImageView(image);
        Group root = new Group(view);

        javafx.scene.Scene fxScene = new javafx.scene.Scene(root, WIDTH, HEIGHT, Color.BLACK);

        primaryStage.setTitle("Raytracer v1.1");
        primaryStage.setScene(fxScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Scene buildScene() {
        Camera camera = new Camera(new Vector3D(0, 3, 14), 60.0, WIDTH, HEIGHT, 0.5, 100.0);

        Scene scene = new Scene(camera, Color.PINK);

        // Floor — flat at y=-1.5, spans the full scene
        Vector3D fl0 = new Vector3D(-50, -1.5,  12);
        Vector3D fl1 = new Vector3D( 50, -1.5,  12);
        Vector3D fl2 = new Vector3D( 50, -1.5,  -7);
        Vector3D fl3 = new Vector3D(-50, -1.5,  -7);
        Triangle floor1 = new Triangle(fl0, fl1, fl2, Color.DARKGRAY);
        Triangle floor2 = new Triangle(fl0, fl2, fl3, Color.DARKGRAY);
        floor1.setMaterial(new Material(0.05, 0.8, 0.1, 8, 0.0, 0.0, 1.0));
        floor2.setMaterial(new Material(0.05, 0.8, 0.1, 8, 0.0, 0.0, 1.0));
        scene.addObject(floor1);
        scene.addObject(floor2);

        // Mirror back wall — vertical, behind the spheres at z=-6, facing the camera
        Vector3D wl0 = new Vector3D(-10, -1.5, -6);
        Vector3D wl1 = new Vector3D( 10, -1.5, -6);
        Vector3D wl2 = new Vector3D( 10,  8.0, -6);
        Vector3D wl3 = new Vector3D(-10,  8.0, -6);
        Triangle wall1 = new Triangle(wl0, wl1, wl2, Color.WHITE);
        Triangle wall2 = new Triangle(wl0, wl2, wl3, Color.WHITE);
        wall1.setMaterial(new Material(0.0, 0.05, 0.0, 1, 1, 0.0, 1.0));
        wall2.setMaterial(new Material(0.0, 0.05, 0.0, 1, 1, 0.0, 1.0));
        scene.addObject(wall1);
        scene.addObject(wall2);

        // LEFT — mirror sphere, reflects the scene around it
        Sphere mirror = new Sphere(new Vector3D(-4, 0, 0), 1.5, Color.WHITE);
        mirror.setMaterial(new Material(0.02, 0.05, 0.5, 64, 0.85, 0.0, 1.0));
        scene.addObject(mirror);

        // CENTER — broad specular glow, no reflection
        Sphere medium = new Sphere(new Vector3D(0, 0, 0), 1.5, Color.WHITE);
        medium.setMaterial(new Material(0.05, 0.7, 0.9, 16, 0.0, 0.0, 1.0));
        scene.addObject(medium);

        // RIGHT — matte red, diffuse only
        Sphere matte = new Sphere(new Vector3D(4, 0, 0), 1.5, Color.TOMATO);
        matte.setMaterial(new Material(0.05, 0.9, 0.0, 1, 0.0, 0.0, 1.0));
        scene.addObject(matte);

        // Main white point light — upper-left, primary source of highlights
        scene.addLight(new PointLight(new Vector3D(-2, 6, 8), Color.WHITE, 25));
        // Soft fill — dim directional from the right, lifts shadow sides
        scene.addLight(new DirectionalLight(new Vector3D(1.0, -0.3, -0.5), Color.WHITE, 0.15));

        return scene;
    }

    private WritableImage render(Scene scene) {
        Camera camera = scene.getCamera();
        int w = camera.getWidth();
        int h = camera.getHeight();

        WritableImage image = new WritableImage(w, h);
        PixelWriter pw = image.getPixelWriter();

        int barWidth = 40;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Ray ray = camera.generateRay(x, y);
                Color color = trace(ray, scene, camera.getNear(), camera.getFar(), 4);
                pw.setColor(x, y, color);
            }
            int filled = (y + 1) * barWidth / h;
            int percent = (y + 1) * 100 / h;
            String bar = "#".repeat(filled) + "-".repeat(barWidth - filled);
            System.out.printf("\rRendering: [%s] %d%%", bar, percent);
        }
        System.out.println("\rRendering: [" + "#".repeat(barWidth) + "] 100% - done.");
        return image;
    }

    private Color trace(Ray ray, Scene scene, double near, double far, int depth) {
        Intersection closest = findClosest(ray, scene, near, far);
        return shade(closest, ray, scene, depth);
    }

    private Intersection findClosest(Ray ray, Scene scene, double near, double far) {
        //Declare the closest intersection to see if it hits or not
        Intersection closest = new Intersection();

        //Main iteration to find the closest intersection of the ray with the objects in the scene
        for (Object3D obj : scene.getObjects()) {
            Intersection hit = obj.getIntersection(ray);

            //If it doesn't hit, or if it's outside the near and far planes, skip to the next object
            if (!hit.isHit()) continue;
            if (hit.getT() < near || hit.getT() > far) continue;

            if (hit.getT() < closest.getT()) {
                closest = hit;
            }
        }

        return closest;
    }

    private boolean isInShadow(Vector3D point, Vector3D normal, Light light, Scene scene){
        Vector3D shadowOrigin =point.add(normal.scale(1e-4));
        Vector3D toLight = light.getDirectionOfLight(point);
        double shadowFar = light.getMaxShadowDistance(point);


        Ray shadowRay = new Ray(shadowOrigin, toLight);
        Intersection shadowHit = findClosest(shadowRay, scene, 1e-4, shadowFar);
        return shadowHit.isHit(); 
    }

    private Color shade(Intersection closest, Ray ray, Scene scene, int depth) {
        //If it doesn't hit anything, return the background color
        if (!closest.isHit()) {
            return scene.getBackgroundColor();
        }

        //If it does hit, calculate the color based on the lights and the material properties of the object
        Object3D object = closest.getObject();
        Color objectColor = object.getColor();
        Material mat = object.getMaterial();
        double r = mat.getAmbient() * objectColor.getRed();
        double g = mat.getAmbient() * objectColor.getGreen();
        double b = mat.getAmbient() * objectColor.getBlue();

        //View direction is the opposite of the ray direction
        Vector3D viewDir = ray.getDirection().scale(-1);

        for (Light light : scene.getLights()) {

            if(isInShadow(closest.getPoint(), closest.getNormal(), light, scene)) continue;

            double NdotL = light.getNDotL(closest);
            if (NdotL <= 0) continue;

            double intensity = light.getIntensity();
            double attenuation = light.getAttenuation(closest.getPoint());
            Color lc = light.getColor();
            Vector3D lightDir = light.getDirectionOfLight(closest.getPoint());

            //Diffuse component using Lambert's cosine law
            r += lc.getRed()   * objectColor.getRed()   * mat.getDiffuse() * intensity * NdotL * attenuation;
            g += lc.getGreen() * objectColor.getGreen() * mat.getDiffuse() * intensity * NdotL * attenuation;
            b += lc.getBlue()  * objectColor.getBlue()  * mat.getDiffuse() * intensity * NdotL * attenuation;

            //Half Vector for Blinn-Phong specular calculation. Is the normalized sum of the light direction and the view direction
            Vector3D halfVector = lightDir.add(viewDir).normalize();
            double NdotH = closest.getNormal().dot(halfVector);

            //Specular component using Blinn-Phong model
            if (NdotH > 0) {
                double spec = Math.pow(NdotH, mat.getShininess());
                r += lc.getRed()   * intensity * spec * mat.getSpecular() * attenuation;
                g += lc.getGreen() * intensity * spec * mat.getSpecular() * attenuation;
                b += lc.getBlue()  * intensity * spec * mat.getSpecular() * attenuation;
            }
        }

        if(mat.getReflectivity() > 0 && depth > 0){
            Vector3D d = ray.getDirection();
            Vector3D n = closest.getNormal();
            Vector3D reflectDir = d.subtract(n.scale(2.0 * d.dot(n))).normalize();
            Vector3D reflectOrigin = closest.getPoint().add(closest.getNormal().scale(1e-4));
            Ray reflectRay = new Ray(reflectOrigin, reflectDir);
            Color reflectColor = trace(reflectRay, scene, 1e-4, Double.POSITIVE_INFINITY, depth - 1);
            r = (1 - mat.getReflectivity()) * r + mat.getReflectivity() * reflectColor.getRed();
            g = (1 - mat.getReflectivity()) * g + mat.getReflectivity() * reflectColor.getGreen();
            b = (1 - mat.getReflectivity()) * b + mat.getReflectivity() * reflectColor.getBlue();
        }

        if(mat.getTransparency() > 0 && depth > 0){
            Vector3D d = ray.getDirection();
            Vector3D n = closest.getNormal();
            boolean entering = d.dot(n) < 0;
            Vector3D refractNormal = entering ? n : n.scale(-1);
            double eta = entering ? (1.0 / mat.getIor()) : mat.getIor();

            double cosI = -d.dot(refractNormal);
            double sinT2 = eta * eta * (1.0 - cosI * cosI);
            if (sinT2 <= 1.0) {
                double cosT = Math.sqrt(1.0 - sinT2);
                Vector3D refractDir = d.scale(eta).add(refractNormal.scale(eta * cosI - cosT)).normalize();
                Vector3D refractOrigin = closest.getPoint().subtract(refractNormal.scale(1e-4));
                Ray refractRay = new Ray(refractOrigin, refractDir);
                Color refractColor = trace(refractRay, scene, 1e-4,
                        Double.POSITIVE_INFINITY, depth - 1);
                r = (1 - mat.getTransparency()) * r + mat.getTransparency() * refractColor.getRed();
                g = (1 - mat.getTransparency()) * g + mat.getTransparency() * refractColor.getGreen();
                b = (1 - mat.getTransparency()) * b + mat.getTransparency() * refractColor.getBlue();
            } else {
                // Total internal reflection — all light reflects, regardless of reflectivity
                Vector3D reflectDir = d.subtract(n.scale(2.0 * d.dot(n))).normalize();
                Vector3D reflectOrigin = closest.getPoint().add(refractNormal.scale(1e-4));
                Ray reflectRay = new Ray(reflectOrigin, reflectDir);
                Color reflectColor = trace(reflectRay, scene, 1e-4, Double.POSITIVE_INFINITY, depth - 1);
                r = reflectColor.getRed();
                g = reflectColor.getGreen();
                b = reflectColor.getBlue();
            }
        }
        

        // Clamp the color values to the range [0, 1]
        r = Math.min(1.0, r);
        g = Math.min(1.0, g);
        b = Math.min(1.0, b);

        return new Color(r, g, b, 1.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}   