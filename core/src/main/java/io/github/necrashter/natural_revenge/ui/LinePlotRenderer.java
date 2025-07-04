package io.github.necrashter.natural_revenge.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ScreenUtils;
public class LinePlotRenderer {
    private final int width;
    private final int height;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final FrameBuffer frameBuffer;
    private final TextureRegion textureRegion;

    private static final float margin = 32f;

    public LinePlotRenderer(int w, int h) {
        this.width = w;
        this.height = h;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        camera = new OrthographicCamera(w, h);
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        textureRegion = new TextureRegion(frameBuffer.getColorBufferTexture());
        textureRegion.flip(false, true);
    }

    public void begin() {
        frameBuffer.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    public void addGrid(int cellCountX, int cellCountY) {
        if (cellCountX <= 0 || cellCountY <= 0) return;

        float drawableWidth = width - 2 * margin;
        float drawableHeight = height - 2 * margin;

        float stepX = drawableWidth / cellCountX;
        float stepY = drawableHeight / cellCountY;

        float startX = -width / 2f + margin;
        float startY = -height / 2f + margin;
        float endX = startX + drawableWidth;
        float endY = startY + drawableHeight;

        Color transparentWhite = new Color(1f, 1f, 1f, 0.2f); // semi-transparent white

        shapeRenderer.setColor(transparentWhite);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Vertical lines
        for (int i = 0; i <= cellCountX; i++) {
            float x = startX + i * stepX;
            shapeRenderer.line(x, startY, x, endY);
        }

        // Horizontal lines
        for (int i = 0; i <= cellCountY; i++) {
            float y = startY + i * stepY;
            shapeRenderer.line(startX, y, endX, y);
        }

        shapeRenderer.end();
    }

    public void addAxes() {
        float drawableWidth = width - 2 * margin;
        float drawableHeight = height - 2 * margin;

        float startX = -width / 2f + margin;
        float startY = -height / 2f + margin;
        float endX = startX + drawableWidth;
        float endY = startY + drawableHeight;

        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float thickness = 4f;
        float radius = thickness / 2f;

        // Vertical lines
        for (int i = 0; i < 1; i++) {
            float x = startX + i * drawableWidth;
            shapeRenderer.rectLine(x, startY - radius, x, endY + radius, thickness);
        }

        // Horizontal lines
        for (int i = 0; i < 1; i++) {
            float y = startY + i * drawableHeight;
            shapeRenderer.rectLine(startX - radius, y, endX + radius, y, thickness);
        }

        shapeRenderer.end();
    }

    public void end() {
        frameBuffer.end();
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    private float findMax(FloatArray values) {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < values.size; ++i) {
            float v = values.get(i);
            if (v > max) max = v;
        }
        return max > 0 ? max : 1f;
    }

    public void plot(FloatArray xArray, FloatArray yArray, Color color) {
        float drawableWidth = width - 2 * margin;
        float drawableHeight = height - 2 * margin;

        float maxX = findMax(xArray);
        float stepX = maxX > 0 ? drawableWidth / maxX : 0;
        float maxY = findMax(yArray);
        float stepY = maxY > 0 ? drawableHeight / maxY : 0;

        float cornerX = -width / 2f + margin;
        float cornerY = -height / 2f + margin;

        float thickness = 4f;
        float radius = thickness / 2f;

        shapeRenderer.setColor(color);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 1; i < xArray.size; ++i) {
            float x1 = xArray.get(i - 1) * stepX + cornerX;
            float y1 = yArray.get(i - 1) * stepY + cornerY;
            float x2 = xArray.get(i) * stepX + cornerX;
            float y2 = yArray.get(i) * stepY + cornerY;

            shapeRenderer.rectLine(x1, y1, x2, y2, thickness, color, color);
            shapeRenderer.circle(x2, y2, radius);
        }

        shapeRenderer.end();
    }

    public void plot(FloatArray yArray, Color color) {
        float drawableWidth = width - 2 * margin;
        float drawableHeight = height - 2 * margin;

        float stepX = drawableWidth / (yArray.size-1);
        float maxY = findMax(yArray);
        float stepY = maxY > 0 ? drawableHeight / maxY : 0;

        float cornerX = -width / 2f + margin;
        float cornerY = -height / 2f + margin;

        float thickness = 4f;
        float radius = thickness / 2f;

        shapeRenderer.setColor(color);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 1; i < yArray.size; ++i) {
            float x1 = (i - 1) * stepX + cornerX;
            float y1 = yArray.get(i - 1) * stepY + cornerY;
            float x2 = (i) * stepX + cornerX;
            float y2 = yArray.get(i) * stepY + cornerY;

            if (Float.isNaN(y1) || Float.isNaN(y2)) continue;

            shapeRenderer.rectLine(x1, y1, x2, y2, thickness, color, color);
            shapeRenderer.circle(x2, y2, radius);
        }

        shapeRenderer.end();
    }
}
