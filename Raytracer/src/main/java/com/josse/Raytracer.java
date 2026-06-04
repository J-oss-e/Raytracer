package com.josse;

import com.josse.lights.Light;
import com.josse.lights.PointLight;
import com.josse.objects.Camera;
import com.josse.objects.Circle;
import com.josse.objects.Model3D;
import com.josse.objects.Object3D;
import com.josse.objects.Sphere;
import com.josse.objects.Triangle;
import com.josse.tools.Intersection;
import com.josse.tools.Material;
import com.josse.tools.ObjReader;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;
import com.josse.lights.DirectionalLight;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Raytracer extends Application {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1420;

    @Override
    public void start(Stage primaryStage) {
        Scene world = buildScene();

        WritableImage image = render(world);

        savePng(image, "Love.png");

        ImageView view = new ImageView(image);
        Group root = new Group(view);

        javafx.scene.Scene fxScene = new javafx.scene.Scene(root, WIDTH, HEIGHT, Color.BLACK);

        primaryStage.setTitle("Raytracer v1.3");
        primaryStage.setScene(fxScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Scene buildScene() {
        Camera camera = new Camera(new Vector3D(0, 2, 14), new Vector3D(0, 10, -10), 60.0, WIDTH, HEIGHT, 0.5, 300.0);

        Scene scene = new Scene(camera, Color.GRAY);

        // Floor
        Vector3D fl0 = new Vector3D(-1000, -1.5,  50);
        Vector3D fl1 = new Vector3D( 1000, -1.5,  50);
        Vector3D fl2 = new Vector3D( 1000, -1.5, -70);
        Vector3D fl3 = new Vector3D(-1000, -1.5, -70);
        Triangle floor1 = new Triangle(fl0, fl1, fl2, Color.LIGHTGRAY);
        Triangle floor2 = new Triangle(fl0, fl2, fl3, Color.LIGHTGRAY);
        floor1.setMaterial(new Material(0.05, 0.8, 0.1, 8, 0, 0.0, 1.0));
        floor2.setMaterial(new Material(0.05, 0.8, 0.1, 8, 0, 0.0, 1.0));
        scene.addObject(floor1);
        scene.addObject(floor2);

        //mirror
        Vector3D ml0 = new Vector3D(-15, -2,  -35);
        Vector3D ml1 = new Vector3D( 0, -2,  -40);
        Vector3D ml2 = new Vector3D( 0, 15, -40);
        Vector3D ml3 = new Vector3D(-15, 15,  -35);
        Triangle mirror1 = new Triangle(ml0, ml1, ml2, Color.LIGHTGRAY);
        Triangle mirror2 = new Triangle(ml0, ml2, ml3, Color.LIGHTGRAY);
        mirror1.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0.92, 0.0, 1.0));
        mirror2.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0.92, 0.0, 1.0));
        scene.addObject(mirror1);
        scene.addObject(mirror2);

        Vector3D ml4 = new Vector3D(15, -2,  -35);
        Vector3D ml5 = new Vector3D( 0, -2,  -40);
        Vector3D ml6 = new Vector3D( 0, 15, -40);
        Vector3D ml7 = new Vector3D(15, 15,  -35);
        Triangle mirror3 = new Triangle(ml4, ml5, ml6, Color.LIGHTGRAY);
        Triangle mirror4 = new Triangle(ml4, ml6, ml7, Color.LIGHTGRAY);
        mirror3.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0.92, 0.0, 1.0));
        mirror4.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0.92, 0.0, 1.0));
        scene.addObject(mirror3);
        scene.addObject(mirror4);

        Sphere sphere = new Sphere(new Vector3D(0, 11, -10), 6.0, Color.PURPLE);
        sphere.setMaterial(new Material(0.02, 0.4, 0.4, 128, 0, 0.6, 1.5));
        scene.addObject(sphere);

        Model3D model = ObjReader.loadModel("Resources/Statue of Liberty - Bombed.obj", Color.WHITE, new Vector3D(-5, -1.5, 17));
        model.setMaterial(new Material(0.02, 0.4, 0.4, 128, 0, 0.3, 1.0));
        //scene.addObject(model);

        Model3D meltingHeart = ObjReader.loadModel("Resources/melting-heart.obj", Color.RED, new Vector3D(0, 5.5, -10), 0.6, 0, Math.toRadians(270), 0);
        meltingHeart.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0, 0.0, 1.0));
        scene.addObject(meltingHeart);

        Model3D meltingHead = ObjReader.loadModel("Resources/4b9ad10c4aed42699e869ce25a7c8772.obj", Color.WHITE, new Vector3D(13, -6, -14), 3.5, 0, 0, 0);
        meltingHead.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0.9, 0.0, 1.0));
        scene.addObject(meltingHead);

        Model3D meltingMan = ObjReader.loadModel("Resources/efffeff91cc54531bc4a3beffcf4216e.obj", Color.BLUE, new Vector3D(-14, 5, -14), 4, 0, 0, 0);
        meltingMan.setMaterial(new Material(0.02, 0.05, 0.9, 128, 0.92, 0.0, 1.0));
        scene.addObject(meltingMan);

        Model3D heart = ObjReader.loadModel("Resources/heart.obj", Color.RED, new Vector3D(0, 11, -14), 0.75);
        heart.setMaterial(new Material(0.02, 0.4, 0.4, 128, 0.3, 0.0, 1.0));
        //scene.addObject(heart);

        Circle backdrop = new Circle(new Vector3D(0, 4, -70), 50.0, new Vector3D(0, 0, 1), Color.WHITE);
        backdrop.setMaterial(new Material(0.1, 0.8, 0.2, 128, 0, 0.0, 1.0));
        //scene.addObject(backdrop);

        scene.addLight(new PointLight(new Vector3D(0, 30, -15), Color.WHITE, 100));
        scene.addLight(new PointLight(new Vector3D(5, 8, -10), Color.RED, 70));
        scene.addLight(new DirectionalLight(new Vector3D(-6, 6, 8), Color.WHITE, 20));

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
                Color color = trace(ray, scene, camera.getNear(), camera.getFar(), 5);
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

    private void savePng(WritableImage image, String filename) {
        try {
            BufferedImage buf = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(buf, "png", new File(filename));
            System.out.println("Saved: " + new File(filename).getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Could not save PNG: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}   