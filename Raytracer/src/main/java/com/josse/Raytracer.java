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

    private static final int WIDTH = 800;
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
        // Camera pulled back and slightly elevated to see all three spheres clearly
        Camera camera = new Camera(new Vector3D(0, 3, 14), 60.0, WIDTH, HEIGHT, 0.5, 100.0);

        Scene scene = new Scene(camera, Color.BLACK);

        // Floor
        Vector3D fl0 = new Vector3D(-10, -1.5,  10);
        Vector3D fl1 = new Vector3D( 10, -1.5,  10);
        Vector3D fl2 = new Vector3D( 10, -1.5, -10);
        Vector3D fl3 = new Vector3D(-10, -1.5, -10);
        scene.addObject(new Triangle(fl0, fl1, fl2, Color.DARKGRAY));
        scene.addObject(new Triangle(fl0, fl2, fl3, Color.DARKGRAY));

        // LEFT — very shiny blue sphere (shininess=128, tight pinpoint highlight)
        Sphere shiny = new Sphere(new Vector3D(-4, 0, 0), 1.5, Color.CORNFLOWERBLUE);
        shiny.setMaterial(new Material(0.05, 0.5, 1.0, 128, 0.0, 0.0, 1.0));
        scene.addObject(shiny);

        // CENTER — medium shininess white sphere (shininess=16, broad soft glow)
        Sphere medium = new Sphere(new Vector3D(0, 0, 0), 1.5, Color.WHITE);
        medium.setMaterial(new Material(0.05, 0.7, 0.9, 16, 0.0, 0.0, 1.0));
        scene.addObject(medium);

        // RIGHT — matte red sphere (specular=0, diffuse only — no highlight)
        Sphere matte = new Sphere(new Vector3D(4, 0, 0), 1.5, Color.TOMATO);
        matte.setMaterial(new Material(0.05, 0.9, 0.0, 1, 0.0, 0.0, 1.0));
        scene.addObject(matte);

        // Main light — white point light, upper-left, creates the specular highlights
        scene.addLight(new PointLight(new Vector3D(-2, 6, 8), Color.WHITE, 25));
        // Fill light — dim directional from the right, softens shadow sides
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
                Color color = trace(ray, scene, camera.getNear(), camera.getFar());
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

    private Color trace(Ray ray, Scene scene, double near, double far) {
        Intersection closest = findClosest(ray, scene, near, far);
        return shade(closest, ray, scene);
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

    private Color shade(Intersection closest, Ray ray, Scene scene){
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