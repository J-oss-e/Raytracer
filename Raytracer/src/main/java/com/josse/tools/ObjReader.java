package com.josse.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException; // Si vas a leer archivos
import java.util.ArrayList; // Para manejar errores de lectura
import java.util.List;

import com.josse.objects.Model3D;
import com.josse.objects.Triangle;

import javafx.scene.paint.Color;

// Reads Wavefront .obj files and creates Model3D objects. Handles scale, XYZ rotation, and quad faces.
public class ObjReader {
        public static Model3D loadModel(String path, Color color, Vector3D position) {
            return loadModel(path, color, position, 1.0, 0, 0, 0);
        }

        public static Model3D loadModel(String path, Color color, Vector3D position, double scale) {
            return loadModel(path, color, position, scale, 0, 0, 0);
        }

        // Loads a model with full control over scale and XYZ rotation (angles in radians).
        public static Model3D loadModel(String path, Color color, Vector3D position, double scale,
                                        double rotX, double rotY, double rotZ) {
            List<Triangle> triangles = loadTriangles(path, color, position, scale, rotX, rotY, rotZ);
            return new Model3D(triangles, color, position);
        }

        // Applies X-axis, then Y-axis, then Z-axis rotation (in radians) to a vertex.
        private static Vector3D rotateVertex(Vector3D v, double rx, double ry, double rz) {
            double x = v.getX(), y = v.getY(), z = v.getZ();
            // X-axis rotation
            double cosX = Math.cos(rx), sinX = Math.sin(rx);
            double y1 = y * cosX - z * sinX;
            double z1 = y * sinX + z * cosX;
            // Y-axis rotation
            double cosY = Math.cos(ry), sinY = Math.sin(ry);
            double x2 = x * cosY + z1 * sinY;
            double z2 = -x * sinY + z1 * cosY;
            // Z-axis rotation
            double cosZ = Math.cos(rz), sinZ = Math.sin(rz);
            double x3 = x2 * cosZ - y1 * sinZ;
            double y3 = x2 * sinZ + y1 * cosZ;
            return new Vector3D(x3, y3, z2);
        }

        // Main parsing logic: reads the .obj file line by line and builds a list of triangles with applied scale and rotation.
        private static List<Triangle> loadTriangles(String path, Color color, Vector3D position, double scale,
                                                     double rotX, double rotY, double rotZ) {
            List<Vector3D> vertices = new ArrayList<>();
            List<Vector3D> normals = new ArrayList<>();
            List<Triangle> triangles = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String linea;
                // Read each line: 'v' = vertex, 'vn' = normal, 'f' = face (triangle or quad).
                while ((linea = br.readLine()) != null) {
                    if (linea.startsWith("v ")) {
                        String[] partes = linea.split("\\s+");
                        Vector3D raw = new Vector3D(
                            Double.parseDouble(partes[1]) * scale,
                            Double.parseDouble(partes[2]) * scale,
                            Double.parseDouble(partes[3]) * scale);
                        Vector3D rotated = rotateVertex(raw, rotX, rotY, rotZ);
                        vertices.add(new Vector3D(
                            rotated.getX() + position.getX(),
                            rotated.getY() + position.getY(),
                            rotated.getZ() + position.getZ()));
                    }
                    else if(linea.startsWith("vn ")){
                        String[] partes = linea.split("\\s+");
                        Vector3D raw = new Vector3D(
                            Double.parseDouble(partes[1]),
                            Double.parseDouble(partes[2]),
                            Double.parseDouble(partes[3]));
                        normals.add(rotateVertex(raw, rotX, rotY, rotZ).normalize());
                    }
                    else if(linea.startsWith("f ")) {
                        String[] partes = linea.split("\\s+");
                        int v1 = Integer.parseInt(partes[1].split("/")[0]) - 1;
                        int v2 = Integer.parseInt(partes[2].split("/")[0]) - 1;
                        int v3 = Integer.parseInt(partes[3].split("/")[0]) - 1;
                        int n1 = 0, n2 = 0, n3 = 0, n4 = 0;

                        if(partes[1].split("/").length >= 3){
                            n1 = Integer.parseInt(partes[1].split("/")[2]) - 1;
                            n2 = Integer.parseInt(partes[2].split("/")[2]) - 1;
                            n3 = Integer.parseInt(partes[3].split("/")[2]) - 1;

                            if (partes.length == 5) {
                                int v4 = Integer.parseInt(partes[4].split("/")[0]) - 1;
                                n4 = Integer.parseInt(partes[4].split("/")[2]) - 1;
                                Triangle triangle1 = new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3), normals.get(n1), normals.get(n2), normals.get(n3), color);
                                Triangle triangle2 = new Triangle(vertices.get(v1), vertices.get(v3), vertices.get(v4), normals.get(n1), normals.get(n3), normals.get(n4), color);
                                triangles.add(triangle1);
                                triangles.add(triangle2);
                            } else {
                                Triangle triangle = new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3), normals.get(n1), normals.get(n2), normals.get(n3), color);
                                triangles.add(triangle);
                            }
                        }else{
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
                }
            }   catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
            }
            return triangles;
        }
}
