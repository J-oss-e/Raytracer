# Raytracer

Final project for **Multimedia and Computer Graphics**. A CPU raytracer written in Java with JavaFX output. Supports Blinn-Phong shading, shadows, reflections, refractions, and 3D model loading (.obj).

---

## Requirements

- Java 17+
- JavaFX SDK
- Maven

```
mvn clean javafx:run
```

---

## How it works

Each pixel fires one ray from the camera into the scene. The ray finds the closest object it hits, then `shade()` computes the color:

```
color = ambient
      + Σ lights [ diffuse * NdotL + specular * NdotH^shininess ]   (skipped if in shadow)
      + reflectivity  * color of reflected ray
      + transparency  * color of refracted ray
```

Reflected and refracted rays recurse up to `depth` times. 3D models are accelerated with a **BVH** (Bounding Volume Hierarchy) so each ray skips most triangles.

---

## Configuration — `Raytracer.java`

All scene setup lives in `buildScene()`. The render constants are at the top of the file.

### Canvas size

```java
private static final int WIDTH  = 1920;
private static final int HEIGHT = 1080;
```

### Output file

```java
savePng(image, "DeathSpace.png");
```

Change the filename string to whatever you want the PNG to be called.

### Recursion depth

```java
Color color = trace(ray, scene, camera.getNear(), camera.getFar(), 5);
//                                                                  ^ bounce limit
```

Higher depth = more accurate reflections/refractions, slower render.

---

## Scene setup — inside `buildScene()`

### Camera

```java
new Camera(position, target, fov, WIDTH, HEIGHT, near, far)
```

| Parameter  | Type       | Description                                  |
|------------|------------|----------------------------------------------|
| `position` | `Vector3D` | Where the camera sits in the world           |
| `target`   | `Vector3D` | The point the camera looks at                |
| `fov`      | `double`   | Vertical field of view in degrees            |
| `near`     | `double`   | Closest distance a hit can register          |
| `far`      | `double`   | Farthest distance a hit can register         |

```java
// Example
Camera camera = new Camera(
    new Vector3D(0, 10, 0),       // position
    new Vector3D(0, 10, -10),     // look at
    60.0,                          // fov
    WIDTH, HEIGHT,
    0.5, 300.0                     // near, far
);
```

---

### Objects

#### Sphere

```java
new Sphere(position, radius, color)
```

#### Circle (flat disk)

```java
new Circle(position, radius, normal, color)
```

`normal` is the direction the disk faces (e.g. `new Vector3D(0, 1, 0)` faces upward).

#### Triangle

```java
new Triangle(v0, v1, v2, color)
// or with per-vertex normals for smooth shading:
new Triangle(v0, v1, v2, n0, n1, n2, color)
```

Quads are two triangles sharing a diagonal (`v0-v1-v2` + `v0-v2-v3`).

#### 3D model from .obj file

```java
ObjReader.loadModel(path, color, position, scale, rotX, rotY, rotZ)
```

| Parameter  | Type       | Description                                       |
|------------|------------|---------------------------------------------------|
| `path`     | `String`   | Path to the .obj file (relative to working dir)  |
| `color`    | `Color`    | Base color applied to the whole model            |
| `position` | `Vector3D` | World-space offset added after rotation          |
| `scale`    | `double`   | Uniform scale factor                             |
| `rotX/Y/Z` | `double`   | Rotation around each axis **in radians**         |

```java
// Example: load at 5x scale, rotated 270° on X, 180° on Y
ObjReader.loadModel(
    "Resources/spaceman.obj",
    Color.WHITE,
    new Vector3D(50, -125, 40),
    5,
    Math.toRadians(270), Math.toRadians(180), 0
);
```

---

### Material

Every object gets a material with `setMaterial(...)`. The default material is matte and opaque.

```java
new Material(ambient, diffuse, specular, shininess, reflectivity, transparency, ior)
```

| Parameter       | Range   | Effect                                                           |
|-----------------|---------|------------------------------------------------------------------|
| `ambient`       | 0 – 1   | Minimum brightness even in full shadow                          |
| `diffuse`       | 0 – 1   | How much the surface scatters light (Lambert term)              |
| `specular`      | 0 – 1   | Strength of the highlight                                        |
| `shininess`     | 1 – 256 | Tightness of the highlight — higher = smaller, sharper spot    |
| `reflectivity`  | 0 – 1   | How mirror-like the surface is (0 = none, 1 = full mirror)     |
| `transparency`  | 0 – 1   | How much light passes through (0 = opaque, 1 = glass)          |
| `ior`           | ≥ 1.0   | Index of refraction (1.0 = air, 1.4 = glass, 2.4 = diamond)   |

```java
// Matte floor
new Material(0.05, 0.8, 0.1, 8,   0.0, 0.0, 1.0)

// Mirror
new Material(0.02, 0.05, 0.9, 128, 0.92, 0.0, 1.0)

// Transparent glass sphere
new Material(0.02, 0.4, 0.4, 128,  0.0, 0.8, 1.4)

// Shiny reflective model
new Material(0.02, 0.02, 0.9, 128, 0.8, 0.0, 1.0)
```

---

### Lights

#### Point light — radiates from a position, falls off with distance

```java
new PointLight(position, color, intensity)
```

#### Directional light — parallel rays from a fixed direction, no falloff

```java
new DirectionalLight(direction, color, intensity)
```

| Parameter   | Type       | Description                                          |
|-------------|------------|------------------------------------------------------|
| `position`  | `Vector3D` | Where the point light sits in the world              |
| `direction` | `Vector3D` | Direction the light travels (not toward the light)  |
| `color`     | `Color`    | Tint of the light                                    |
| `intensity` | `double`   | Brightness multiplier                                |

```java
scene.addLight(new PointLight(new Vector3D(0, 50, 5),   Color.WHITE,  80));
scene.addLight(new PointLight(new Vector3D(10, 25, 45), Color.WHITE, 100));
```

---

## Project structure

```
src/main/java/com/josse/
├── Raytracer.java          — entry point, render loop, shading
├── Scene.java              — scene container
├── objects/
│   ├── Object3D.java       — abstract base for all objects
│   ├── Sphere.java
│   ├── Triangle.java
│   ├── Circle.java
│   ├── Model3D.java        — OBJ model, delegates to BVH
│   ├── BVHNode.java        — acceleration structure
│   └── Camera.java
├── lights/
│   ├── Light.java          — abstract base
│   ├── PointLight.java
│   └── DirectionalLight.java
└── tools/
    ├── Vector3D.java
    ├── Ray.java
    ├── Intersection.java
    ├── AABB.java           — bounding box for BVH
    ├── Material.java
    └── ObjReader.java      — .obj file parser
```
