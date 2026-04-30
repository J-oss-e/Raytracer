package com.josse;

import java.util.List;

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

        primaryStage.setTitle("Raytracer v0.2");
        primaryStage.setScene(fxScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Scene buildScene() {
        Camera camera = new Camera(new Vector3D(0, 0, 0), 60.0, WIDTH, HEIGHT, 0.5, 50.0);

        Scene scene = new Scene(camera, Color.WHITE);

        scene.addObject(new Sphere(new Vector3D(-0.5, 0.3, 5.0), 0.5, Color.RED));
        scene.addObject(new Sphere(new Vector3D( 0.6, 0.3, 6.0), 0.3, Color.BLUE));

        scene.addObject(new Triangle(
            new Vector3D( 0.0,  0.6, 4.0),
            new Vector3D(-0.6, -0.4, 4.0),
            new Vector3D( 0.6, -0.4, 4.0),
            Color.GREEN
        ));

        List<Triangle> mesh = ObjReader.loadTriangles("C:\\Users\\Angel\\Documents\\Up ISGC\\4to Semestre\\Raytracer\\Raytracer\\Resources\\escandalosos.obj", Color.ORANGE);
        for (Triangle t : mesh) {
            scene.addObject(t);
        }
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
                Color color = trace(ray, scene);
                pw.setColor(x, y, color);
            }
        }
        return image;
    }

    private Color trace(Ray ray, Scene scene) {
        Camera cam = scene.getCamera();
        double near = cam.getNear();
        double far  = cam.getFar();

        Intersection closest = new Intersection();

        for (Object3D obj : scene.getObjects()) {
            Intersection hit = obj.intersect(ray);

            if (!hit.isHit()) continue;
            if (hit.getT() < near || hit.getT() > far) continue;

            if (hit.getT() < closest.getT()) {
                closest = hit;
            }
        }

        if (closest.isHit()) {
            return closest.getObject().getColor();
        }
        return scene.getBackgroundColor();
    }

    public static void main(String[] args) {
        launch(args);
    }
}