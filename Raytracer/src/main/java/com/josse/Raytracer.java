package com.josse;

import java.util.List;

import com.josse.lights.Light;
import com.josse.objects.Camera;
import com.josse.objects.Object3D;
import com.josse.objects.Triangle;
import com.josse.tools.Intersection;
import com.josse.tools.ObjReader;
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

        primaryStage.setTitle("Raytracer v0.4");
        primaryStage.setScene(fxScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Scene buildScene() {
        Camera camera = new Camera(new Vector3D(2, 8, 20), 60.0, WIDTH, HEIGHT, 0.5, 45.0);

        Scene scene = new Scene(camera, Color.BLACK);

        List<Triangle> mesh = ObjReader.loadTriangles("C:\\Users\\Angel\\Documents\\Up ISGC\\4to Semestre\\Raytracer\\Raytracer\\Resources\\Lowpoly_tree_sample.obj", Color.ORANGE);
        for (Triangle t : mesh) {
            scene.addObject(t);
        }

        // Luz direccional
        scene.addLight(new Light(
            new Vector3D(0.0, 0.0, -1.0),
            Color.WHITE,
            1.0
        ));

        return scene;
    }

    private WritableImage render(Scene scene) {
        Camera camera = scene.getCamera();
        int w = camera.getWidth();
        int h = camera.getHeight();

        WritableImage image = new WritableImage(w, h);
        PixelWriter pw = image.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Ray ray = camera.generateRay(x, y);
                Color color = trace(ray, scene, camera.getNear(), camera.getFar());
                pw.setColor(x, y, color);
            }
        }
        return image;
    }

    private Color trace(Ray ray, Scene scene, double near, double far) {
        Intersection closest = findClosest(ray, scene, near, far);
        return shade(closest, scene, ray);
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

    private Color shade(Intersection closest, Scene scene, Ray ray){
        //If it doesn't hit anything, return the background color
        if (!closest.isHit()) {
            return scene.getBackgroundColor();
        }

        //If it does hit, calculate the color based on the lights and the material properties of the object
        Object3D object = closest.getObject();
        Vector3D normal = closest.getNormal();
        // If the normal is facing the ray, flip it
        if (normal.dot(ray.getDirection()) > 0) normal = normal.scale(-1);

        Color objectColor = object.getColor();
        double r = 0, g = 0, b = 0;
    
        // For each light in the scene, calculate the contribution to the color based on the light's intensity, color, and direction
        for (Light light : scene.getLights()) {
            Vector3D firstLight = light.getDirection().scale(-1).normalize();
            double NdotL = Math.max(0.0, normal.dot(firstLight));

            // If the surface is perpendicular to the light, skip to the next light
            if (NdotL <= 0) continue;
            double li = light.getIntensity();
            Color lc = light.getColor();

            r += lc.getRed()   * objectColor.getRed()   * li * NdotL;
            g += lc.getGreen() * objectColor.getGreen() * li * NdotL;
            b += lc.getBlue()  * objectColor.getBlue()  * li * NdotL;
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