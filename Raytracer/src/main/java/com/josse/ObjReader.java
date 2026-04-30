package com.josse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException; // Si vas a leer archivos
import java.util.ArrayList; // Para manejar errores de lectura
import java.util.List;

import javafx.scene.paint.Color;

public class ObjReader {
        public static List<Triangle> loadTriangles(String path, Color color) {
            // 1. crear lista de vértices (List<Vector3D>)
            List<Vector3D> vertices = new ArrayList<>();
            // 2. crear lista de triángulos (List<Triangle>)
            List<Triangle> triangles = new ArrayList<>();
            // 3. abrir el archivo y leer línea por línea
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
                        Triangle triangle = new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3), color);
                        triangles.add(triangle);
                    }
                }
            }   catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
            }
            return triangles;
        }
}
