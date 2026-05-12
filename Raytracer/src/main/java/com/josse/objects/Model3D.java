package com.josse.objects;

import java.util.List;

import com.josse.tools.Intersection;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class Model3D extends Object3D {
    private List<Triangle> triangles;

    public Model3D(List<Triangle> Triangles, Color color, Vector3D Position){
        super(Position, color);
        this.triangles = Triangles;
    }

    @Override
    public Intersection getIntersection(Ray ray){
        
        Intersection closest = new Intersection();
        
        for (Object3D obj : this.triangles) {
            Intersection hit = obj.getIntersection(ray);

            if (!hit.isHit()) continue;
            
            if (hit.getT() < closest.getT()) {
                closest = hit;
            }
        }

        Intersection modelHit = new Intersection(closest.isHit(), closest.getT(), closest.getPoint(), this, closest.getNormal());
        return modelHit;
    }
    
}
