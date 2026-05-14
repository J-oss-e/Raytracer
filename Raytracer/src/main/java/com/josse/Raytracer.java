package com.josse;

import com.josse.lights.DirectionalLight;
import com.josse.lights.Light;
import com.josse.lights.PointLight;
import com.josse.objects.Camera;
import com.josse.objects.Model3D;
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

    private static final int WIDTH = 1800;
    private static final int HEIGHT = 1000;

    @Override
    public void start(Stage primaryStage) {
        Scene world = buildScene();

        WritableImage image = render(world);

        ImageView view = new ImageView(image);
        Group root = new Group(view);

        javafx.scene.Scene fxScene = new javafx.scene.Scene(root, WIDTH, HEIGHT, Color.BLACK);

        primaryStage.setTitle("Raytracer v0.6");
        primaryStage.setScene(fxScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Scene buildScene() {
        Camera camera = new Camera(new Vector3D(0, 5, 18), 60.0, WIDTH, HEIGHT, 0.5, 100.0);

        Scene scene = new Scene(camera, Color.BLACK);
        
        Model3D model = ObjReader.loadModel("Resources/utah_teapot2.obj", Color.WHITE, new Vector3D(0, 0, 0));
        scene.addObject(model);
        Vector3D fl0 = new Vector3D(-10, -1,  10);
        Vector3D fl1 = new Vector3D( 10, -1,  10);
        Vector3D fl2 = new Vector3D( 10, -1, -10);
        Vector3D fl3 = new Vector3D(-10, -1, -10);

        scene.addObject(new Triangle(fl0, fl1, fl2, Color.GRAY));
        scene.addObject(new Triangle(fl0, fl2, fl3, Color.GRAY));

        scene.addLight(new DirectionalLight(new Vector3D(0.0, -1.0, -1.0), Color.WHITE, 0.5));
        scene.addLight(new PointLight(new Vector3D(0.0, 60.0, 10.0), Color.RED, 0.8));

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
        return shade(closest, scene);
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

    private Color shade(Intersection closest, Scene scene){
        //If it doesn't hit anything, return the background color
        if (!closest.isHit()) {
            return scene.getBackgroundColor();
        }

        //If it does hit, calculate the color based on the lights and the material properties of the object
        Object3D object = closest.getObject();

        Color objectColor = object.getColor();
        double r = 0, g = 0, b = 0;

        Vector3D shadowOrigin = closest.getPoint().add(closest.getNormal().scale(1e-4));

        for (Light light : scene.getLights()) {
            Vector3D toLight = light.getDirectionOfLight(closest.getPoint());
            double shadowFar = light.getMaxShadowDistance(closest.getPoint());

            Ray shadowRay = new Ray(shadowOrigin, toLight);
            Intersection shadowHit = findClosest(shadowRay, scene, 1e-4, shadowFar);
            if (shadowHit.isHit()) continue;

            double NdotL = light.getNDotL(closest);
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