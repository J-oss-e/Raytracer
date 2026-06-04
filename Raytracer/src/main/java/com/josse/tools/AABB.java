package com.josse.tools;


// Axis-aligned bounding box. Used in the BVH to cheaply reject rays before testing actual geometry.
public class AABB {
    public Vector3D min;
    public Vector3D max;

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }


    // Slab test: intersects the ray with each axis pair and checks if all three overlap. Fast rejection for BVH traversal.
    public boolean intersect(Ray ray, double tMin, double tMax) {
        // Check the ray against each axis slab (X=0, Y=1, Z=2). If any axis fails, the ray misses the box.
        for (int a = 0; a < 3; a++) {
            double invD = 1.0 / ray.getDirection().get(a);
            double t0 = (min.get(a) - ray.getOrigin().get(a)) * invD;
            double t1 = (max.get(a) - ray.getOrigin().get(a)) * invD;
            if (invD < 0.0) {
                double temp = t0;
                t0 = t1;
                t1 = temp;
            }
            tMin = Math.max(t0, tMin);
            tMax = Math.min(t1, tMax);
            if (tMax <= tMin) {
                return false;
            }
        }
        return true;
    }

    // Returns the smallest AABB that contains both this box and other.
    public AABB union(AABB other) {
        double minX = Math.min(this.min.x, other.min.x);
        double minY = Math.min(this.min.y, other.min.y);
        double minZ = Math.min(this.min.z, other.min.z);
        double maxX = Math.max(this.max.x, other.max.x);
        double maxY = Math.max(this.max.y, other.max.y);
        double maxZ = Math.max(this.max.z, other.max.z);
        return new AABB(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
    }

    // Returns the center point of the box. Used to sort triangles when choosing a BVH split.
    public Vector3D centroid() {
        return new Vector3D(
            (min.x + max.x) / 2.0,
            (min.y + max.y) / 2.0,
            (min.z + max.z) / 2.0
        );
    }
}
