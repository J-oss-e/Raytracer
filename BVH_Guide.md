# BVH Implementation Guide
### Raytracer v1.1 — Bounding Volume Hierarchy

---

## Why Does This Exist?

Before this implementation, rendering was slow because `Model3D` tested every ray against **every single triangle** in the mesh. A tree model with 5,000 triangles and an 800×800 image means roughly 3.2 billion triangle tests — just for primary rays.

A **Bounding Volume Hierarchy (BVH)** solves this by organizing triangles into a binary tree of boxes. When a ray misses a box, the entire subtree inside it is skipped with a single cheap test.

| Approach | Cost per ray | Example (5k triangles) |
|---|---|---|
| Brute force | O(N) | 5,000 tests |
| BVH | O(log N) | ~13 tests |

---

## Concept 1 — AABB (Axis-Aligned Bounding Box)

**Files changed:** `Vector3D.java` (additions), `AABB.java` (new file)

### What is an AABB?

The simplest possible volume: a box whose faces are perfectly aligned to the X, Y, and Z axes. Defined by just two points:

```
min = (minX, minY, minZ)   ← bottom-left-back corner
max = (maxX, maxY, maxZ)   ← top-right-front corner
```

Every triangle and sphere in the scene can compute its own AABB. We use these boxes to quickly reject rays before doing the expensive triangle math.

### The Slab Test (Ray vs Box)

Think of the box as three pairs of infinite parallel planes — one pair per axis:

```
X-slab: planes at x = min.x  and  x = max.x
Y-slab: planes at y = min.y  and  y = max.y
Z-slab: planes at z = min.z  and  z = max.z
```

A ray `P(t) = origin + t * direction` enters and exits each slab at two t-values. For the X-slab:

```
t0 = (min.x - origin.x) / direction.x
t1 = (max.x - origin.x) / direction.x
```

The ray hits the box **only if all three slab intervals overlap**:

```
tEnter = max(tX0, tY0, tZ0)   ← last time ray enters any slab
tExit  = min(tX1, tY1, tZ1)   ← first time ray exits any slab

HIT if: tEnter <= tExit  AND  tExit >= 0
```

```
X:    ------[==========]------
Y:    ----------[===========]-
Z:    ------[==============]--
              ^         ^
           tEnter     tExit   → HIT (intervals overlap)
```

If the ray direction on any axis is negative, t0 and t1 come out swapped — we detect this with `invD < 0` and swap them.

### Additions to `Vector3D.java`

Three new methods added to support component-wise comparisons and indexed access:

```java
// Returns a new vector with the smaller component from each input.
// Used to compute the min corner of a bounding box.
public static Vector3D min(Vector3D a, Vector3D b) {
    return new Vector3D(
        Math.min(a.x, b.x),
        Math.min(a.y, b.y),
        Math.min(a.z, b.z)
    );
}

// Returns a new vector with the larger component from each input.
// Used to compute the max corner of a bounding box.
public static Vector3D max(Vector3D a, Vector3D b) {
    return new Vector3D(
        Math.max(a.x, b.x),
        Math.max(a.y, b.y),
        Math.max(a.z, b.z)
    );
}

// Allows accessing x/y/z by index (0/1/2).
// Used in the slab test loop and BVH axis sorting.
public double get(int index) {
    switch (index) {
        case 0: return x;
        case 1: return y;
        case 2: return z;
        default: throw new IndexOutOfBoundsException("Index must be 0, 1, or 2");
    }
}
```

### `AABB.java` (new file)

```java
public class AABB {
    public Vector3D min;  // bottom-left-back corner
    public Vector3D max;  // top-right-front corner

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    // Slab test: returns true if the ray intersects this box
    // within the interval [tMin, tMax].
    public boolean intersect(Ray ray, double tMin, double tMax) {
        for (int a = 0; a < 3; a++) {
            double invD = 1.0 / ray.getDirection().get(a);
            double t0 = (min.get(a) - ray.getOrigin().get(a)) * invD;
            double t1 = (max.get(a) - ray.getOrigin().get(a)) * invD;
            // If ray direction is negative on this axis, t0 and t1 are
            // swapped — fix the order.
            if (invD < 0.0) { double temp = t0; t0 = t1; t1 = temp; }
            tMin = Math.max(t0, tMin);
            tMax = Math.min(t1, tMax);
            if (tMax <= tMin) return false;  // intervals don't overlap → miss
        }
        return true;
    }

    // Returns the box that contains both this box and another.
    // Used during BVH construction to combine children's bounds.
    public AABB union(AABB other) {
        return new AABB(
            new Vector3D(Math.min(this.min.x, other.min.x),
                         Math.min(this.min.y, other.min.y),
                         Math.min(this.min.z, other.min.z)),
            new Vector3D(Math.max(this.max.x, other.max.x),
                         Math.max(this.max.y, other.max.y),
                         Math.max(this.max.z, other.max.z))
        );
    }

    // Returns the center point of the box.
    // Used to sort triangles by position during BVH construction.
    public Vector3D centroid() {
        return new Vector3D(
            (min.x + max.x) / 2.0,
            (min.y + max.y) / 2.0,
            (min.z + max.z) / 2.0
        );
    }
}
```

---

## Concept 2 — Every Object Reports Its Bounds

**Files changed:** `Object3D.java`, `Triangle.java`, `Sphere.java`

For the BVH to wrap objects in boxes, each object must be able to report its own bounding box. We added an abstract method to `Object3D`:

```java
// Every subclass must implement this.
public abstract AABB getBoundingBox();
```

### Triangle

The AABB of a triangle is simply the component-wise min and max of its three vertices:

```java
@Override
public AABB getBoundingBox() {
    Vector3D min = Vector3D.min(Vector3D.min(v0, v1), v2);  // min of all 3 vertices
    Vector3D max = Vector3D.max(Vector3D.max(v0, v1), v2);  // max of all 3 vertices
    return new AABB(min, max);
}
```

### Sphere

A sphere at `position` with radius `r` fits exactly in a box from `position - r` to `position + r` on every axis:

```java
@Override
public AABB getBoundingBox() {
    Vector3D min = new Vector3D(position.x - radius, position.y - radius, position.z - radius);
    Vector3D max = new Vector3D(position.x + radius, position.y + radius, position.z + radius);
    return new AABB(min, max);
}
```

---

## Concept 3 — BVH Tree (Construction + Traversal)

**Files changed:** `BVHNode.java` (new file), `Model3D.java` (wired in)

### What is a BVH node?

`BVHNode` extends `Object3D`, so it fits anywhere an object is expected. It has:
- A cached `bounds` AABB that wraps everything below it
- A `left` and `right` child — each can be a `Triangle` (leaf) or another `BVHNode` (internal node)

```
         [Root BVHNode]         ← bounds = entire mesh
        /              \
 [BVHNode]          [BVHNode]   ← bounds = left half / right half
  /      \            /     \
[tri]   [tri]      [tri]   [tri] ← leaves: actual Triangle objects
```

### Construction — how the tree is built

`BVHNode.build(List<Triangle>)` is called once at scene load time. It recursively splits the triangle list:

1. Pick a random axis (X, Y, or Z)
2. Sort triangles by the centroid of their bounding box on that axis
3. Split the list at the midpoint
4. Recurse on each half
5. The resulting node's bounds = union of both children's bounds

Base cases:
- 1 triangle → both `left` and `right` point to the same triangle
- 2 triangles → `left = tri[0]`, `right = tri[1]`

```java
public class BVHNode extends Object3D {
    public Object3D left;
    public Object3D right;
    private AABB bounds;  // cached at construction — never recomputed

    public BVHNode(Object3D left, Object3D right) {
        this.left = left;
        this.right = right;
        // Compute and cache bounds once, from the children.
        this.bounds = left.getBoundingBox().union(right.getBoundingBox());
    }

    @Override
    public AABB getBoundingBox() {
        return this.bounds;  // O(1) — just a field read
    }

    // Traversal: test the ray against this node's box first.
    // If it misses the box, skip this entire subtree.
    @Override
    public Intersection getIntersection(Ray ray) {
        if (!bounds.intersect(ray, 0.001, Double.POSITIVE_INFINITY)) {
            return new Intersection();  // miss — skip subtree
        }
        Intersection hitLeft  = left.getIntersection(ray);
        Intersection hitRight = right.getIntersection(ray);

        // Return whichever is closer (or the one that hit, or nothing)
        if (hitLeft.isHit() && hitRight.isHit())
            return hitLeft.getT() < hitRight.getT() ? hitLeft : hitRight;
        if (hitLeft.isHit())  return hitLeft;
        if (hitRight.isHit()) return hitRight;
        return new Intersection();
    }

    // Build the BVH tree from a list of triangles.
    static BVHNode build(List<Triangle> triangles) {
        final int axis = (int)(Math.random() * 3);  // random split axis
        triangles.sort((a, b) -> Double.compare(
            a.getBoundingBox().centroid().get(axis),
            b.getBoundingBox().centroid().get(axis)
        ));

        if (triangles.size() == 1)
            return new BVHNode(triangles.get(0), triangles.get(0));
        if (triangles.size() == 2)
            return new BVHNode(triangles.get(0), triangles.get(1));

        int mid = triangles.size() / 2;
        BVHNode leftNode  = build(triangles.subList(0, mid));
        BVHNode rightNode = build(triangles.subList(mid, triangles.size()));
        return new BVHNode(leftNode, rightNode);
    }
}
```

### Wiring into `Model3D`

`Model3D` now builds the BVH once in its constructor and delegates all intersection work to it. The wrapping step (`this` as object) preserves the model's color for shading:

```java
public class Model3D extends Object3D {
    private List<Triangle> triangles;
    private BVHNode bvh;

    public Model3D(List<Triangle> triangles, Color color, Vector3D position) {
        super(position, color);
        this.triangles = triangles;
        // Build the BVH once at load time — not per ray.
        this.bvh = BVHNode.build(new ArrayList<>(triangles));
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        Intersection hit = bvh.getIntersection(ray);
        // Wrap: replace the object reference with `this` (the Model3D)
        // so shade() uses the model's color, not the internal triangle's.
        return hit.isHit()
            ? new Intersection(true, hit.getT(), hit.getPoint(), this, hit.getNormal())
            : new Intersection();
    }

    @Override
    public AABB getBoundingBox() {
        return bvh.getBoundingBox();  // delegates to root node
    }
}
```

---

## How It All Connects

```
Scene loads OBJ file
    → ObjReader creates List<Triangle>
    → Model3D constructor receives them
    → BVHNode.build() sorts + splits into a tree    ← happens ONCE

Per pixel:
    Camera.generateRay(x, y)
    → Model3D.getIntersection(ray)
        → BVHNode.getIntersection(ray)           ← starts at root
            → bounds.intersect(ray) ?            ← slab test O(1)
                NO  → return miss (skip subtree)
                YES → recurse into left + right
                        ... until reaching Triangle leaves
        → wrap result with Model3D as object
    → shade(hit, scene)
```

---

## Key Terms for Your Presentation

| Term | One-sentence definition |
|---|---|
| **AABB** | A box aligned to the XYZ axes, defined by a min corner and a max corner |
| **Slab test** | Check if a ray's t-intervals on all three axes overlap — cheap ray-box test |
| **BVH** | A binary tree where each node stores a box containing all its children |
| **Leaf node** | A BVH node that wraps a single triangle (or a small group) |
| **Internal node** | A BVH node that wraps two child nodes |
| **Traversal** | Walking the tree top-down, skipping subtrees whose box the ray misses |
| **Cached bounds** | The AABB stored once at construction so it isn't recomputed per ray |
| **union(a, b)** | The smallest AABB that contains both box a and box b |
| **centroid** | The center point of a box — used to sort triangles during construction |

---

*Implemented during v1.1 development — June 2026*
