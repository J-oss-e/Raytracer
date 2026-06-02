package com.josse.objects;

import java.util.ArrayList;
import java.util.List;

import com.josse.tools.AABB;
import com.josse.tools.Intersection;
import com.josse.tools.Ray;
import com.josse.tools.Vector3D;

import javafx.scene.paint.Color;

public class Model3D extends Object3D {
    private List<Triangle> triangles;
    private BVHNode bvh;

    public Model3D(List<Triangle> Triangles, Color color, Vector3D Position){
        super(Position, color);
        this.triangles = Triangles;
        this.bvh = BVHNode.build(new ArrayList<>(triangles));
    }

    @Override
    public Intersection getIntersection(Ray ray){
        Intersection closest = bvh.getIntersection(ray);
        return closest.isHit() ? new Intersection(true, closest.getT(), closest.getPoint(), this, closest.getNormal()) :
               new Intersection();
    }

    @Override
    public AABB getBoundingBox() {
        return bvh.getBoundingBox();
    }
    
}
