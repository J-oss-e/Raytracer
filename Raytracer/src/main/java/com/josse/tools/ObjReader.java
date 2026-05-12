package com.josse.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException; // Si vas a leer archivos
import java.util.ArrayList; // Para manejar errores de lectura
import java.util.List;

import com.josse.objects.Model3D;
import com.josse.objects.Triangle;

import javafx.scene.paint.Color;

public class ObjReader {
        public static Model3D loadModel(String path, Color color, Vector3D position) {
            List<Triangle> triangles = loadTriangles(path, color);
            return new Model3D(triangles, color, position);
        }

        private static List<Triangle> loadTriangles(String path, Color color) {
            List<Vector3D> vertices = new ArrayList<>();
            List<Triangle> triangles = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (linea.startsWith("v ")) {
                        String[] partes = linea.split("\\s+");
                        double x = Double.parseDouble(partes[1]);
                        double y = Double.parseDouble(partes[2]);
                        double z = Double.parseDouble(partes[3]);
                        vertices.add(new Vector3D(x, y, z));
                    }
                    else if(linea.startsWith("f ")) {
                        String[] partes = linea.split("\\s+");
                        int v1 = Integer.parseInt(partes[1].split("/")[0]) - 1;
                        int v2 = Integer.parseInt(partes[2].split("/")[0]) - 1;
                        int v3 = Integer.parseInt(partes[3].split("/")[0]) - 1;

                        if (partes.length == 5) {
                            int v4 = Integer.parseInt(partes[4].split("/")[0]) - 1;
                            Triangle triangle1 = new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3), color);
                            Triangle triangle2 = new Triangle(vertices.get(v1), vertices.get(v3), vertices.get(v4), color);
                            triangles.add(triangle1);
                            triangles.add(triangle2);
                        } else {
                            Triangle triangle = new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3), color);
                            triangles.add(triangle);
                        }
                    }
                }
            }   catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
            }
            return triangles;
        }
}
