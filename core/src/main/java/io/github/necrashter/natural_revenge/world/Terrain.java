package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.Perlin;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.geom.GridRayIt;

public class Terrain implements Disposable {
    private static final GridRayIt gridRayIt = new GridRayIt();
    private static final Vector3 tmp1 = new Vector3();
    public Mesh mesh;
    public DefaultShader shader;
    public Renderable renderable;
    public float[] vertices;
    public float width, height;
    public float halfWidth, halfHeight;
    public int verticesWidth, verticesHeight, vertexCount;
    int gridWidth, gridHeight;
    public Texture texture;
    public TextureAttribute textureAttribute;
    RandomXS128 random;

    public Terrain(Environment environment, Perlin perlin, int w, int h, RandomXS128 random) {
        width = w;
        height = h;
        halfWidth = width * 0.5f;
        halfHeight = height * 0.5f;
        gridWidth = w;
        gridHeight = h;
        this.random = random;
        mesh = createMesh(perlin);

        texture = Main.assets.get("textures/grass.png", Texture.class);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        textureAttribute = TextureAttribute.createDiffuse(texture);
        textureAttribute.scaleU = 0.1f;
        textureAttribute.scaleV = 0.1f;
        MeshPart meshPart = new MeshPart("", mesh, 0, gridWidth * gridHeight *6, GL20.GL_TRIANGLES);
        NodePart nodePart = new NodePart(meshPart, new Material(
//                ColorAttribute.createSpecular(Color.WHITE),
                textureAttribute,
                ColorAttribute.createAmbient(Color.GREEN)
        ));
        renderable = new Renderable();
        renderable.environment = environment;
        renderable.worldTransform.idt();
        nodePart.setRenderable(renderable);

        String vert = Gdx.files.internal("shaders/terrain.vert").readString();
        String frag = Gdx.files.internal("shaders/terrain.frag").readString();
        shader = new DefaultShader(renderable, new DefaultShader.Config(vert, frag));

//        shader = new DefaultShader(renderable);

        shader.init();
    }

    public float randomX(float margin) {
        return (random.nextFloat() - 0.5f) * (gridWidth - margin);
    }

    public float randomZ(float margin) {
        return (random.nextFloat() - 0.5f) * (gridHeight - margin);
    }

    public Vector3 randomPoint(float margin) {
        float x = randomX(margin);
        float z = randomZ(margin);
        return tmp1.set(x, getHeight(x, z), z);
    }

    public Vector3 getPoint(float x, float z) {
        return tmp1.set(x, getHeight(x, z), z);
    }

    public float getVertexHeight(int w, int h) {
        w = w < 0 ? 0 : w >= verticesWidth ? verticesWidth-1 : w;
        h = h < 0 ? 0 : h >= verticesHeight ? verticesHeight-1 : h;
        return vertices[6*(w + verticesWidth *h) + 1];
    }

    public float getHeight(float x, float z) {
        int w = MathUtils.floor(x += halfWidth);
        int h = MathUtils.floor(z += halfHeight);
        x -= w;
        z -= h;
        return MathUtils.lerp(
                MathUtils.lerp(
                        getVertexHeight(w, h),
                        getVertexHeight(w, h+1),
                        z
                ),
                MathUtils.lerp(
                        getVertexHeight(w+1, h),
                        getVertexHeight(w+1, h+1),
                        z
                ),
                x
        );
    }

    public void getRandomPoint(Vector3 p, float margin) {
        p.set(
                randomX(margin),
                0,
                randomZ(margin)
        );
        p.y = getHeight(p.x, p.z);
    }
    public void getRandomPoint(Vector3 p) {
        getRandomPoint(p, 2.0f);
    }

    /**
     * NOTE: Cannot detect intersection if ray originates from outside the terrain.
     * @param ray ray
     * @return Parameter t at which ray intersects terrain, or float +infinity
     */
    public float intersectRay(Ray ray) {
        gridRayIt.rayStart.set(ray.origin).add(verticesWidth /2.0f, 0, verticesHeight /2.0f);
        gridRayIt.rayDir.set(ray.direction);
        gridRayIt.prepare();
        while (gridRayIt.gridX >= 0 && gridRayIt.gridX+1 < verticesWidth && gridRayIt.gridY >= 0 && gridRayIt.gridY+1 < verticesHeight) {
            float sh = vertices[6*(gridRayIt.gridX + verticesWidth * gridRayIt.gridY) + 1];
            if (sh >= gridRayIt.rayStart.y) return gridRayIt.t;
            // Plane-Ray Intersection
            // this has problems, particularly while checking whether intersection is in cell
            // plane normal
//            tmp1.set(
//                            0,
//                            vertices[6*(gridRayIt.gridX + vw*(gridRayIt.gridY+1)) + 1] - sh,
//                            1
//                    )
//                    .crs(
//                            1,
//                            vertices[6*(gridRayIt.gridX+1 + vw* gridRayIt.gridY) + 1] - sh,
//                            0
//                    )
//                    .nor();
//            float div = tmp1.dot(ray.direction);
//            if (Math.abs(div) > 1e-6) {
//                float t = tmp2.set(gridRayIt.gridX, sh, gridRayIt.gridY).sub(gridRayIt.rayStart).dot(tmp1) / div;
//                tmp1.set(gridRayIt.rayStart).mulAdd(gridRayIt.rayDir, t);
//                if (tmp1.x >= gridRayIt.gridX && tmp1.x <= gridRayIt.gridX+1 && tmp1.z >= gridRayIt.gridY && tmp1.z <= gridRayIt.gridY+1)
//                    return t;
//            }
            if (!gridRayIt.next()) break;
        }
        return Float.POSITIVE_INFINITY;
    }

    public Mesh createMesh(Perlin perlin) {
        verticesWidth = gridWidth+1;
        verticesHeight = gridHeight+1;
        vertexCount = verticesWidth * verticesHeight;
        vertices = new float[vertexCount*6];
        int i;

        final float cellWidth = width / (float)gridWidth;
        final float cellHeight = height / (float)gridHeight;

        i = 0;
        for (int j = 0; j <= gridWidth; ++j) {
            for (int k = 0; k <= gridHeight; ++k) {
                float x = (float) k * cellWidth - width / 2.0f;
                float y = (float) j * cellHeight - height / 2.0f;
                float ph = perlin.perlin(x, y);
                vertices[i++] = x;
                vertices[i++] = ph * perlin.terrainHeight;
                vertices[i++] = y;

                Vector3 tan = perlin.perlinTan(x, y, ph);
                Vector3 bitan = perlin.perlinBitan(x, y, ph);
                Vector3 normal = bitan.crs(tan);
                normal.nor();

                vertices[i++] = normal.x;
                vertices[i++] = normal.y;
                vertices[i++] = normal.z;

//                vertices[i++] = y;
//                vertices[i++] = x;
            }
        }

        short[] indices = new short[gridWidth*gridHeight*6];
        i = 0;
        short col = (short) 1;
        short row = (short) ((gridHeight+1)*col);
        for (short x = 0; x < gridWidth; ++x) {
            for (short y = 0; y < gridHeight; ++y) {
                short topLeft = (short) ((y*col) + (x*row));
                indices[i++] = topLeft;
                indices[i++] = (short) (topLeft + row);
                indices[i++] = (short) (topLeft + col);

                indices[i++] = (short) (topLeft + col);
                indices[i++] = (short) (topLeft + row);
                indices[i++] = (short) (topLeft + row + col);
            }
        }

        Mesh mesh = new Mesh(true, vertexCount, indices.length,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)
//                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0")
        );
        mesh.setVertices(vertices);
        mesh.setIndices(indices, 0 , indices.length);

        return mesh;
    }

    public void render(Camera cam, RenderContext renderContext) {
        shader.begin(cam, renderContext);
        shader.render(renderable);
        shader.end();
    }

    public float clampX(float x, float margin) {
        return MathUtils.clamp(x, -halfWidth+margin, halfWidth-margin);
    }

    public float clampZ(float z, float margin) {
        return MathUtils.clamp(z, -halfHeight+margin, halfHeight-margin);
    }

    public boolean isInBoundary(Vector2 point, float margin) {
        return point.x >= -halfWidth + margin && point.x <= halfWidth - margin &&
            point.y >= -halfHeight + margin && point.y <= halfHeight - margin;
    }


    @Override
    public void dispose() {
        mesh.dispose();
        shader.dispose();
    }

    // Check boundary
    public boolean moveCheck(GameEntity.MoveCheck moveCheck) {
        float rad = moveCheck.hitBox.radius;
        Vector3 pos = moveCheck.hitBox.position;
        Vector3 normal = moveCheck.normal.setZero();
        if (pos.x < rad-halfWidth) {
            normal.x = 1;
        } else if (pos.x > halfWidth-rad) {
            normal.x = -1;
        }
        if (pos.z < rad-halfHeight) {
            normal.z = 1;
        } else if (pos.z > halfHeight-rad) {
            normal.z = -1;
        }
        if (normal.x == 0 && normal.z == 0) {
            return false;
        }
        moveCheck.boundary = true;
        return true;
    }

    public class Circle {
        public static final float margin = 1.0f;
        public Vector2 position = new Vector2();
        public final float radius;

        public Circle(float radius) {
            this.radius = radius;
        }

        public Circle(float x, float y, float radius) {
            this.position.set(x, y);
            this.radius = radius;
        }

        void randomPosition() {
            position.x = (random.nextFloat() - 0.5f) * (gridWidth - radius - margin);
            position.y = (random.nextFloat() - 0.5f) * (gridHeight - radius - margin);
        }

        public boolean intersects(Circle other) {
            float dx = other.position.x - position.x;
            float dy = other.position.y - position.y;
            float totalRadii = radius + other.radius;
            return (dx*dx + dy*dy) < (totalRadii * totalRadii);
        }
    }

    public class CircleAreas {
        public Array<Circle> circles = new Array<>();

        /**
         * Try to generate a non-intersecting circle
         * @param radius
         * @return Circle or null if failed
         */
        public Circle generateCircle(float radius) {
            Circle newCircle = new Circle(radius);
            for (int i = 0; i < 100; ++i) {
                newCircle.randomPosition();
                if (!intersects(newCircle)) {
                    return newCircle;
                }
            }
            return null;
        }

        private boolean intersects(Circle circle) {
            for (Circle other: circles) {
                if (circle.intersects(other)) return true;
            }
            return false;
        }

        public void generateCircles(int amount, float radius) {
            for (int i = 0; i < amount; ++i) {
                Circle newCircle = generateCircle(radius);
                if (newCircle == null) return;
                circles.add(newCircle);
            }
        }

        public void add(float x, float y, float radius) {
            circles.add(new Circle(x, y, radius));
        }

        public Vector3 getCenterOnTerrain(int i) {
            Circle circle = circles.get(i);
            return tempTerrainPoint.set(
                    circle.position.x,
                    getHeight(circle.position.x, circle.position.y),
                    circle.position.y
            );
        }
    }
    private static final Vector3 tempTerrainPoint = new Vector3();

    public CircleAreas newCircleAreas() {
        return new CircleAreas();
    }
}
