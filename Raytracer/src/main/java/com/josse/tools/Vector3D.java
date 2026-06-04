package com.josse.tools;

// 3D vector (or point). Used for positions, directions, and all math in the raytracer.
public class Vector3D {

    public double x;
    public double y;
    public double z;

    public Vector3D() {
        this(0.0, 0.0, 0.0);
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }


    public Vector3D add(Vector3D v) {
        return new Vector3D(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public Vector3D subtract(Vector3D v) {
        return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3D scale(double s) {
        return new Vector3D(this.x * s, this.y * s, this.z * s);
    }

    // Dot product: measures how much two vectors align. Used in lighting angle calculations and intersection tests.
    public double dot(Vector3D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    // Cross product: returns a vector perpendicular to both this and v. Used for surface normals and building the camera basis.
    public Vector3D cross(Vector3D v) {
        return new Vector3D(
            this.y * v.z - this.z * v.y,
            this.z * v.x - this.x * v.z,
            this.x * v.y - this.y * v.x
        );
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    // Returns a unit-length copy of this vector. Returns a zero vector if length is zero to avoid division by zero.
    public Vector3D normalize() {
        double len = length();
        if (len == 0.0) {
            return new Vector3D(0, 0, 0);
        }
        return new Vector3D(this.x / len, this.y / len, this.z / len);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }

    @Override
    public String toString() {
        return "Vector3D(" + x + ", " + y + ", " + z + ")";
    }

    // Returns a vector with the smallest component from each axis of a and b. Used to build AABB corners.
    public static Vector3D min(Vector3D a, Vector3D b) {
        return new Vector3D(
            Math.min(a.x, b.x),
            Math.min(a.y, b.y),
            Math.min(a.z, b.z)
        );
    }

    // Returns a vector with the largest component from each axis of a and b. Used to build AABB corners.
    public static Vector3D max(Vector3D a, Vector3D b) {
        return new Vector3D(
            Math.max(a.x, b.x),
            Math.max(a.y, b.y),
            Math.max(a.z, b.z)
        );
    }

    // Returns x, y, or z by axis index (0, 1, 2). Used in BVH axis comparisons.
    public double get(int index) {
        switch (index) {
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default: throw new IndexOutOfBoundsException("Index must be 0, 1, or 2");
        }
    }
}
