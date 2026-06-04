package com.josse.tools;


// An invisible box aligned to the world axes that wraps loosely around an object.
// Rays test this box first — far cheaper than testing every triangle inside — and skip the contents if they miss.
public class AABB {
    public Vector3D min;
    public Vector3D max;

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }


    // Tests whether the ray hits the box. Think of the box as three pairs of parallel walls (one per axis).
    // The ray hits the box only if it passes through all three pairs at overlapping distances; if it clears
    // any pair, the ray misses and we return false immediately.
    public boolean intersect(Ray ray, double tMin, double tMax) {
        // Run the wall test for all three axis pairs: left/right (X=0), up/down (Y=1), front/back (Z=2).
        for (int a = 0; a < 3; a++) {
            double invD = 1.0 / ray.getDirection().get(a);
            double t0 = (min.get(a) - ray.getOrigin().get(a)) * invD;
            double t1 = (max.get(a) - ray.getOrigin().get(a)) * invD;
            if (invD < 0.0) { // ray travels in the negative direction here — entry and exit distances are swapped, so fix that
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
