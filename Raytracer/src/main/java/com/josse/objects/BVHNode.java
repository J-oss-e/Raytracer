package com.josse.objects;
import java.util.List;

import com.josse.tools.AABB;
import com.josse.tools.Intersection;
import com.josse.tools.Ray;

// A node in the scene's acceleration tree. Geometry is organized into a hierarchy of nested boxes:
// if a ray misses the outer box it skips everything inside, so most triangles are never tested.
public class BVHNode extends Object3D {
    public Object3D left;
    public Object3D right;
    private AABB bounds;

    public BVHNode(Object3D left, Object3D right) {
        this.left = left;
        this.right = right;
        this.bounds = left.getBoundingBox().union(right.getBoundingBox());
    }

    @Override
    public AABB getBoundingBox() {
        return this.bounds;
    }

    // Check the outer bounding box first. If the ray misses it, nothing inside can be hit — skip the whole branch.
    // If it does hit, test both child groups and return whichever intersection is closer to the camera.
    @Override
    public Intersection getIntersection(Ray ray) {
        AABB box = getBoundingBox();
        if (!box.intersect(ray, 0.001, Double.POSITIVE_INFINITY)) {
            return new Intersection();
        }
        Intersection hitLeft = left.getIntersection(ray);
        Intersection hitRight = right.getIntersection(ray);
        return hitLeft.isHit() && hitRight.isHit() ? (hitLeft.getT() < hitRight.getT() ? hitLeft : hitRight) :
               hitLeft.isHit() ? hitLeft :
               hitRight.isHit() ? hitRight :
               new Intersection();
    }

    // Builds the tree top-down: pick a random axis, sort all triangles by their position along it, cut the list in
    // half, then repeat on each half. This continues until every leaf holds a single triangle.
    public static BVHNode build(List<Triangle> triangles){
        final int axis = (int)(Math.random() * 3);
        // Order the triangles by where their center sits along the chosen axis, so the split divides them evenly in space.
        triangles.sort((a, b) -> {
            double aCentroid = a.getBoundingBox().centroid().get(axis);
            double bCentroid = b.getBoundingBox().centroid().get(axis);
            return Double.compare(aCentroid, bCentroid);
        });
        
        if (triangles.size() == 1) {
            return new BVHNode(triangles.get(0), triangles.get(0)); // only one triangle left — both child slots point to it so the bounding-box logic still works
        } else if (triangles.size() == 2) {
            return new BVHNode(triangles.get(0), triangles.get(1));
        } else {
            // Split the triangles into two groups
            int mid = triangles.size() / 2;
            List<Triangle> leftTriangles = triangles.subList(0, mid);
            List<Triangle> rightTriangles = triangles.subList(mid, triangles.size());

            // Recursively build the left and right subtrees
            BVHNode leftNode = build(leftTriangles);
            BVHNode rightNode = build(rightTriangles);

            // Create a new BVH node with the left and right subtrees
            return new BVHNode(leftNode, rightNode);
        }
    }
}
