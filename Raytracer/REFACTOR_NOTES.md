 Issue 1: trace() is a God Method

  What it does: Raytracer.java trace() (lines 82–129) handles all of these at once:
  - Iterates over all objects to find the closest intersection
  - Applies near/far clipping
  - Retrieves the normal from the hit object
  - Computes Lambert diffuse shading for every light
  - Clamps and outputs color

  Why it's a problem: A method that does five conceptually separate things is hard to change without breaking the others. If you want to add shadows, you need a second ray-cast from the
  hit point toward the light — but that logic is tangled inside the same method that also does lighting. If you want to add a second shading model, you're modifying the same method that
  finds intersections.

  How it affects future features:
  - Shadows require re-running the intersection search from a different origin. Right now you'd copy-paste the loop or make trace() even larger.
  - Reflections require recursively calling trace-like logic. That recursive call would have to carry all the lighting logic with it.
  - Multiple shading models (Phong, toon, PBR) would all live in the same if/else chain inside trace().

  How an experienced engine developer structures it: They split the method into two explicit stages, which have names in the field:

  1. Ray casting — given a ray and a list of objects, return the closest Intersection. This is a pure function with no side effects.
  2. Shading — given an Intersection and the scene lights, compute a color. This is where lighting models live.

  In a real engine like PBRT or Mitsuba, these are separate systems. The integrator (shading) calls the accelerator (intersection finding) as a black box. Neither knows how the other
  works.

  Tradeoff: Splitting them requires Intersection to carry enough data for shading — specifically the surface normal and material. If Intersection is too thin (as yours is), you end up
  calling back into the object to get missing data, which is exactly the bug you have right now.

  ---
  Issue 2: Intersection doesn't carry the normal

  What's happening: Intersection.java stores { hit, t, point, Object3D object }. It does not store the normal. So after trace() finds the closest hit, it has to go back and call
  obj.getNormal(hitPoint) on line 106. This is the direct cause of your compile error — obj is typed as Object3D, which has no getNormal() method.

  Why it's a problem: The geometry is the only thing that can compute the correct normal — a sphere's normal depends on the center, a triangle's depends on the vertices. That computation
  happens during getIntersection(), at the moment the hit is found. After that, all the information is already there. Making trace() ask for it again after the fact is a broken handshake
  between two methods.

  How it affects future features:
  - Smooth shading requires barycentric interpolation of per-vertex normals. You can compute that inside getIntersection() where you already have the barycentric coordinates from the
  Möller-Trumbore algorithm. After getIntersection() returns, those coordinates are gone — you'd have to recompute them.
  - Bump mapping / normal maps modify the normal at the intersection point. If the normal isn't stored in Intersection, you can't apply them without another round-trip to the object.

  How an experienced engine developer structures it: Intersection (often called HitRecord or SurfaceInteraction) is treated as a complete description of the hit surface. At minimum it
  carries:

  hit, t, point, normal, material/color

  getIntersection() fills all of those fields. trace() never needs to call back into the shape.

  Tradeoff: A richer Intersection object costs slightly more memory per pixel, but the tradeoff is overwhelmingly worth it. This is the pattern used in every production raytracer.

  ---
  Issue 3: Object3D and IIntersectable are fighting each other

  What's happening:
  - Scene.java stores List<IIntersectable> (line 14) — which is correct and extensible.
  - Intersection.java stores Object3D object (line 11) — which requires the hit object to be an Object3D.
  - trace() line 104: Object3D obj = closest.getObject() — extracts the object as Object3D.
  - Sphere and Triangle both extends Object3D implements IIntersectable — they're in both hierarchies.

  Why it's a problem: This creates a hidden assumption: anything that is IIntersectable must also be an Object3D. But that's not enforced. If you ever create an intersectable object that
  doesn't extend Object3D — say, a procedural infinite plane — it can live in scene.getObjects() just fine, but it can never be stored in Intersection.object, so trace() would receive a
  null and crash.

  The two type hierarchies are supposed to be independent, but Intersection secretly couples them together.

  How it affects future features:
  - A Plane or BoundingBox that implements IIntersectable without extending Object3D would break Intersection.
  - Adding invisible geometry (collision meshes, portals) that participates in ray intersection but has no color becomes awkward.

  How an experienced engine developer structures it: Pick one hierarchy and commit. Two common approaches:

  1. Object3D is the root, it declares getIntersection() abstract. Everything in the scene is an Object3D. IIntersectable goes away or becomes an optional mixin. Intersection stores
  Object3D.
  2. IIntersectable is the root for scene geometry. Object3D adds color/material. Intersection stores IIntersectable. This is closer to what your Scene already does — but then
  Intersection must also store the normal separately, because you can't call getNormal() on a raw IIntersectable.

  Tradeoff: Option 1 is simpler and is what most simple raytracers do. Option 2 is more correct architecturally (geometry and material are separate concerns) but requires Intersection to
  be richer.

  ---
  Issue 4: Light is concrete and closed

  What's happening: Light.java is a single concrete class. It hardcodes the directional light model — it has a direction field and nothing else.

  Why it's a problem: The lighting loop in trace() lines 112–121 does:
  Vector3D L = light.getDirection().scale(-1).normalize();
  double NdotL = Math.max(0.0, N.dot(L));
  This calculation is directional-light-specific. It's written directly in the renderer, not delegated to the light. Adding a point light would require an instanceof check or a copy of
  the loop.

  How it affects future features:
  - Point light: contribution depends on distance from the hit point to the light position — the direction changes per hit point, intensity falls off with distance. None of that fits in
  the current Light API.
  - Spot light: adds a cone angle. Still doesn't fit.
  - Area light: requires sampling. Completely different interface.

  How an experienced engine developer structures it: Light becomes abstract with a method like:
  double computeNdotL(Intersection hit)
  Vector3D directionToLight(Vector3D point)
  The renderer calls these methods. It doesn't know whether the light is directional or a point — the subclass handles it. This is the Open/Closed Principle applied to lighting.

  Tradeoff: More classes. But the alternative is a growing if/else chain inside trace() which is worse.

  ---
  Issue 5: The camera can only look in one direction

  What's happening: Camera.generateRay() lines 39–47 hardcodes the ray direction as new Vector3D(px, py, -1.0). The camera always looks along the negative Z axis. The only way to change
  the view is to reposition your objects.

  Why it's a problem: In any real scene you want to orbit a camera, look at a specific target, tilt, etc. Right now none of that is expressible — the camera has a position but no
  orientation.

  How it affects future features:
  - Multiple camera angles (for a cutscene, a turntable render) require moving all geometry instead of moving the camera.
  - Look-at targeting (point the camera at the origin of the object you loaded) is impossible.
  - Depth of field requires a camera basis (right, up, forward vectors) to sample the lens aperture correctly.

  How an experienced engine developer structures it: A camera has three basis vectors computed from a position, a target, and an up vector:

  forward = normalize(target - position)
  right   = normalize(forward × worldUp)
  up      = right × forward

  generateRay(x, y) then uses these: direction = px*right + py*up + forward. The camera can look anywhere without touching any geometry.

  Tradeoff: Three extra vectors and a cross product. The benefit is a fully general camera.

  ---
  Issue 6: ObjReader returns a flat list of triangles, not a mesh

  What's happening: ObjReader.loadTriangles() returns List<Triangle>. buildScene() adds each triangle individually to the scene.

  Why it's a problem: Once you load a mesh this way, the triangles are loose in the scene. There is no object that represents the tree — just N anonymous triangles. You can't move the
  tree, scale it, replace its color, or unload it.

  How it affects future features:
  - Multiple meshes: the scene contains a flat mix of all triangles from all objects, with no way to know which triangle belongs to which mesh.
  - Per-object transformations: moving a mesh after load means iterating every triangle and manually updating vertices. That's destructive and error-prone.
  - Culling: a bounding volume around the tree can reject all its triangles with one test. With a flat list, every triangle is tested individually.

  How an experienced engine developer structures it: ObjReader returns a Mesh or Model3D object. That object holds List<Triangle> internally and implements IIntersectable by delegating to
   them. From the scene's perspective, the tree is one object. Internally it's many triangles. The division is invisible to the renderer.

  Tradeoff: One extra class. Worth it immediately if you plan to have more than one loaded mesh.

  ---
  Issue 7: Vector3D has inconsistent mutability

  What's happening: Vector3D.java has:
  - Public fields x, y, z (line 5–7) — can be written directly: v.x = 5.0
  - Public setters setX(), setY(), setZ() (lines 70–72) — another way to mutate
  - Math operations like add(), scale() return new Vector3D instances — immutable/functional style

  Why it's a problem: Three different mutation models in one class. Code using Vector3D has no consistent contract. If you see v.add(w), is v modified? No — it returns a new vector. But
  v.x += 1 mutates in place. The public fields make it impossible to enforce invariants (e.g., "this vector is always normalized").

  How it affects future features: If you want a NormalizedVector3D type that guarantees unit length — which is useful for normals and ray directions — you can't enforce it with public
  fields. Anyone can set .x = 99 and your normal is no longer a normal.

  How an experienced engine developer structures it: Pick a model:
  - Immutable: fields are final, no setters, all operations return new instances. Safe for concurrent rendering. You already have this style in the math methods.
  - Mutable with explicit in-place ops: methods like addTo(), scaleBy() that modify this. Faster (no allocation), but requires discipline.

  Most simple raytracers use immutable. Immutable is easier to reason about.

  Tradeoff: Immutable means more object allocations per frame. For a CPU raytracer rendering 960,000 pixels this matters — each pixel can generate dozens of vector operations. But for a
  learning project it's the right default. Don't optimize allocation until you can measure the problem.

  ---
  Issue 8: The renderer is fused to the display

  What's happening: Raytracer extends javafx.application.Application. The render loop lives inside a JavaFX start() method. The word "render" and the word "display" are the same class.

  Why it's a problem: You can't:
  - Test rendering correctness without launching a window
  - Save the image to a file without also showing it
  - Run a benchmark headlessly
  - Swap JavaFX for a different display (Swing, a game engine, a web server)

  How it affects future features: If you ever want to render to PNG, the renderer is buried inside JavaFX lifecycle code. To extract it you'd have to untangle application startup from
  pixel writing.

  How an experienced engine developer structures it: The renderer is a plain class with a method like:
  BufferedImage render(Scene scene)
  It has no UI dependency. The main() or application class calls it, then does something with the result — show it in a window, write it to disk, compare it to a reference image in a
  test. The renderer doesn't care.

  Tradeoff: One more class. But the testability benefit is significant — you can write a unit test that renders a scene with one sphere and one light, and checks that the center pixel is
  a specific color.

  ---
  Summary: the dependency chain

  Here is what is coupled to what right now, and why it matters:

  Raytracer (display + loop + raycasting + shading)
      └── Scene (camera + objects + lights mixed together)
      └── trace() calls obj.getNormal()  ← compile error
               └── Object3D has no getNormal()
                    └── because Intersection doesn't store it
                         └── because trace() does both finding AND shading

  The root of almost every issue traces back to trace() doing too much. If you split finding from shading, the Intersection naturally grows to carry the normal, Object3D no longer needs
  getNormal() in the caller, the type confusion between Object3D and IIntersectable resolves, and lighting becomes something you can extend without touching the intersection logic.

  The one fix that unblocks compilation and teaches the right pattern at the same time: add the normal to Intersection, compute it inside getIntersection() for each shape, and remove the
  obj.getNormal() call from trace(). That is a small, safe, contained change that represents a real architectural decision.

  

  1st Session:
  Start the step one, refactor the trace() method, I already delete the getnormal() method and add it to the getIntersection() method of sphere. Now finished deleting the getnormal() method and adding it to the getIntersection() one in triangle too.
  Also added the normal attribute to Intersection so i can save the vector3D there.

  Suggested step order:
    Step 1 — Normal into Intersection          (Issue 2)s
    Step 2 — Split trace() into two methods    (Issue 1)
    Step 3 — Resolve Object3D vs IIntersectable(Issue 3)
    Step 4 — Make Light abstract               (Issue 4)
    Step 5 — Model3D mesh grouping             (Issue 6)
    Step 6 — Camera look-at                    (Issue 5)
    Step 7 — Vector3D immutability             (Issue 7)
    Step 8 — Separate renderer from display    (Issue 8)

    ▎ Found a half-finished refactor: Raytracer.java line 105 had been changed to IIntersectable obj but IIntersectable has no getColor(). Rolled back to Object3D obj to keep Step 1 clean.
    ▎ The full Object3D vs IIntersectable decision is deferred to Step 3.

  2nd Session:
  Step 2 complete — trace() split into three methods:
    - findClosest(Ray, Scene, double near, double far) → Intersection
        Owns the intersection loop. No camera dependency — near/far passed as parameters.
    - shade(Intersection, Scene, Ray) → Color
        Owns the no-hit check (returns background color), normal flip, Lambert loop, and clamping.
    - trace(Ray, Scene, double near, double far) → Color
        Two-line coordinator. Calls findClosest() then shade(). Entry point for all ray types
        (primary, shadow, reflection) — keeps render() clean.
    - render() extracts near/far from camera once and passes them down the chain.

  Key decisions made:
    ▎ near/far moved out of findClosest() — render() owns camera knowledge, passes values down.
    ▎ No-hit check (background color) lives in shade(), not findClosest() — shade() answers
      "what color is this pixel?" and background is a valid answer.
    ▎ trace() kept as coordinator — future shadow/reflection rays will re-enter through trace(),
      not by calling findClosest() and shade() separately.

  Step 3 complete — Object3D vs IIntersectable resolved:
    - Object3D is now the single root. getIntersection(Ray) declared abstract there.
    - Sphere and Triangle drop implements IIntersectable — extends Object3D was already there.
    - Scene.objects changed from List<IIntersectable> to List<Object3D>.
    - Raytracer.findClosest() loop changed from IIntersectable to Object3D.
    - IIntersectable.java deleted — dead code removed.

  Step 5 complete — Model3D mesh grouping:
    - Model3D extends Object3D, stores List<Triangle>, implements getIntersection() internally.
    - getIntersection() loops its own triangles, finds closest hit, returns Intersection with
      this (Model3D) as the object — not the individual triangle.
    - ObjReader.loadModel() returns Model3D. loadTriangles() made private (implementation detail).
    - buildScene() loop removed — one scene.addObject(model) line replaces it.
    - Path changed from hardcoded absolute to relative: "Resources/Lowpoly_tree_sample.obj".

  Key decision: chose Option A (Object3D as root) over Option B (IIntersectable as root).
    ▎ Every intersectable object in the scene has a color — Option B's flexibility
      has no payoff until a full Material system exists. Option A is simpler and correct for now.

  Step 4 complete — Light made abstract:
    - Light.java is now abstract with only color, intensity, and abstract getNDotL(Intersection).
    - direction field moved out of Light and into DirectionalLight where it belongs.
    - Light constructor simplified to (Color, double intensity).
    - DirectionalLight: owns direction, normalizes at construction, implements getNDotL().
    - PointLight: owns position, computes direction per hit point in getNDotL().
    - shade() needed zero changes to support the new PointLight — Open/Closed Principle working.
    - Both lights added to buildScene() for visual testing.

  3rd Session — Bug fixes between Step 5 and Step 6:

  Bug A — Normal flip was in the wrong layer (Issue F from architecture analysis):
    - Original shade() flipped normal into a local variable but getNDotL(closest) read
      intersection.getNormal() directly — the unflipped original. The fix never reached lighting.
    - Symptom: triangles with reversed winding order rendered black even when facing the light.
    - Fix: moved the flip into Triangle.getIntersection() and Sphere.getIntersection().
      Each shape now checks if normal.dot(ray.getDirection()) > 0 and flips before storing
      the normal in the Intersection. Geometry layer owns a geometric decision.
    - shade() Ray parameter removed — no longer needed since shade() does no normal work.
    ▎ Side effect: triangles are now effectively double-sided (back faces visible). Root cause
      is inconsistent winding order in OBJ files — a data quality issue, not a code issue.
      Deferred: back-face culling or winding normalization at load time.

  Bug B — ObjReader silently dropped half the geometry for quad-face OBJ files:
    - ObjReader only parsed partes[1..3] from face lines. Quad faces (f v1 v2 v3 v4)
      had their 4th vertex ignored — one triangle of every quad was never created.
    - Symptom: faces missing entirely (not black — just absent) on models with quad geometry.
    - Fix: ObjReader now checks partes.length. Triangles (length == 4) create 1 triangle.
      Quads (length == 5) are split into 2 triangles using fan triangulation:
        Triangle 1: v1, v2, v3
        Triangle 2: v1, v3, v4
    - v4 scoped inside the quad branch only — no sentinel value needed.

  4th Session — Smooth shading pipeline (partial):

  Reference architecture (professor's repo) analyzed and compared. Key takeaways:
    ▎ Reference's Light extends Object3D is an LSP violation — our design is correct.
    ▎ Reference's Triangle.getIntersection() doesn't store the normal (Issue 2 we already fixed).
    ▎ Reference uses a separate Barycentric utility class — we avoided this by reusing u/v
      values already computed inside Möller-Trumbore. Responsibility stays inside Triangle.
    ▎ Reference shows smooth shading, vn parsing, smoothing groups, and PNG output as targets.

  Triangle — per-vertex normals added:
    - Two new fields: n0, n1, n2 (Vector3D, nullable).
    - Constructor without normals: sets n0/n1/n2 = null (flat shading fallback).
    - Constructor with normals: Triangle(v0,v1,v2,n0,n1,n2,color).
    - getIntersection() now branches on n0 != null:
        If vertex normals present: w = 1-u-v, smoothNormal = n0*w + n1*u + n2*v, normalized.
        If not: existing flat face normal (edge1 × edge2) used as before.
    - Normal flip applied to smooth normal too — consistent with flat normal behavior.
    - u, v barycentric weights come directly from Möller-Trumbore — no utility class needed.

  ObjReader — vn parsing added:
    - normals list added alongside vertices list.
    - vn lines parsed into normals list (same pattern as v lines).
    - f line parsing now checks partes[1].split("/").length >= 3 to detect normal indices.
    - if normals present: extracts n1/n2/n3 (and n4 for quads), uses smooth constructor.
    - else: uses flat constructor — safe for OBJ files with no vn data.
    - Both triangle and quad cases handled in both branches.
    ▎ Guard prevents ArrayIndexOutOfBoundsException on faces without normal indices.

  Pending visual verification:
    - Tested with teapot.obj but camera too far to confirm smooth shading visually.
    - Need to move camera closer OR implement Step 6 (look-at) first for proper framing.

  Next: Verify smooth shading visually, then Step 6 — Camera look-at (Issue 5).

  ---
  5th Session — Reference architecture comparison + shadow casting + light falloff:

  Reference architecture comparison (professor's repo @ 0f25e54):

  | Dimension                  | Reference                          | Ours                          | Better     |
  |----------------------------|------------------------------------|-------------------------------|------------|
  | Renderer / display         | Plain class, PNG output            | Extends Application           | Reference  |
  | Light hierarchy            | extends Object3D (LSP violation)   | Clean abstract                | Ours       |
  | Camera hierarchy           | extends Object3D (LSP violation)   | Standalone                    | Ours       |
  | Miss handling              | null returns                       | isHit() sentinel              | Ours       |
  | Clipping                   | Z-coordinate (breaks with look-at) | Parametric T                  | Ours       |
  | Vector3D math style        | Static, inside-out                 | Fluent, left-to-right         | Ours       |
  | Barycentric                | Second-pass utility class          | Reused from Möller-Trumbore   | Ours       |
  | Mesh position              | Baked destructively into vertices  | Stored separately             | Ours       |
  | Shadow infrastructure      | caster exclusion (unused)          | Per-light (implemented)       | Ours       |
  | OBJ smoothing groups       | Parses s N groups                  | Explicit vn only              | Reference  |
  | Triangle color             | None (geometrically correct)       | Object3D (pragmatic)          | Reference  |

  Shadow casting implemented:
    - Old code cast a single shadow ray along the surface normal (wrong direction) before the
      light loop. One result blocked or allowed all lights simultaneously.
    - Fix: shadow test moved inside the light loop. Each light casts its own shadow ray.
    - Shadow ray direction comes from light.getDirectionOfLight(hitPoint) — already existed
      on Light, just wasn't wired to the shadow code.
    - Shadow ray far limit comes from light.getMaxShadowDistance(hitPoint):
        DirectionalLight → POSITIVE_INFINITY (no position, rays go forever)
        PointLight → distance to light position (objects behind the light don't cast shadows)
    - getMaxShadowDistance() added as abstract on Light, implemented on both subclasses.
    - Floor plane added to scene as two Triangles (extends Object3D directly — no new class needed)
      to give shadows a receiver surface.

  Light falloff (Option B — separate method) implemented:
    - getAttenuation(Vector3D point) added as abstract on Light.
    - DirectionalLight returns 1.0 — no falloff, sun has no meaningful distance.
    - PointLight returns 1.0 / max(d, 1e-4) — linear falloff. Guard on max() prevents
      division by zero if surface is essentially touching the light.
    - shade() multiplies each light's contribution by attenuation as a separate factor:
        contribution = NdotL × intensity × attenuation
    - getNDotL() stays purely geometric (angular factor only). Attenuation is its own concern.
    ▎ With 1/d falloff, intensity must scale with distance. At distance ~12, intensity ~12
      produces similar brightness to intensity ~1 at distance ~1. Tune accordingly.

  Key architectural decisions this session:
    ▎ Shadow test inside the light loop is the only correct structure for multi-light scenes.
    ▎ getDirectionOfLight() on Light makes the shadow code light-type-agnostic — adding
      SpotLight later requires no changes to shade().
    ▎ Separate getAttenuation() keeps getNDotL() semantically clean. When SpotLight adds
      cone falloff, it goes into getAttenuation(), not into getNDotL().

  Next: Step 6 — Camera look-at (Issue 5).

