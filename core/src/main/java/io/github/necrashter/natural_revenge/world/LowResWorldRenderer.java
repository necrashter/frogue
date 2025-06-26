package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LowResWorldRenderer implements GameWorldRenderer {
    public GameWorld world;

    //    public int resolutionX = 1280/2;
//    public int resolutionY = 720/2;
    private int resolutionX = 1280;
    private int resolutionY = 720;
    FrameBuffer worldFrameBuffer, viewFrameBuffer;
    SpriteBatch spriteBatch = new SpriteBatch();
    Camera camera;
    Viewport viewport;

    private float renderX, renderY, renderWidth, renderHeight;

    public LowResWorldRenderer(GameWorld world) {
        this.world = world;

        worldFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, resolutionX, resolutionY, true);
        worldFrameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        viewFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, resolutionX, resolutionY, true);
        viewFrameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        camera = new OrthographicCamera(resolutionX, resolutionY);
        viewport = new StretchViewport(resolutionX, resolutionY);
//        viewport = new FitViewport(resolutionX, resolutionY, camera);
//        viewport = new ScreenViewport();

        renderX = -resolutionX / 2.0f;
        renderY = -resolutionY / 2.0f;
        renderWidth = resolutionX;
        renderHeight = resolutionY;
    }

    @Override
    public void render() {
        worldFrameBuffer.begin();
        world.render();
        worldFrameBuffer.end();

        viewFrameBuffer.begin();
        world.renderViewModel();
        viewFrameBuffer.end();

        viewport.apply(true);
        ScreenUtils.clear(0, 0, 0, 1, true);

//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1);

//        Gdx.gl.glViewport(0, 0, resolutionX, resolutionY);
//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(worldFrameBuffer.getColorBufferTexture(), renderX, renderY, renderWidth, renderHeight, 0, 0, 1, 1);
        spriteBatch.draw(viewFrameBuffer.getColorBufferTexture(), renderX, renderY, renderWidth, renderHeight, 0, 0, 1, 1);
//        spriteBatch.draw(fbo.getColorBufferTexture(), 0, 0, resolutionX, resolutionY, 0, 0, 1, 1);
//        spriteBatch.draw(fbo.getColorBufferTexture(), 0, 0, resolutionX*2, resolutionY*2, 0, 0, 1, 1);
//        spriteBatch.draw(fbo.getColorBufferTexture(), 0, 0, viewport.getScreenWidth(), viewport.getScreenHeight(), 0, 0, 1, 1);
//        spriteBatch.draw(fbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
//        spriteBatch.draw(fbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), 0, 0, 1, 1);
        spriteBatch.end();
    }

    @Override
    public void screenResize(int width, int height) {
        viewport.update(width, height, true);
        world.screenResize(resolutionX, resolutionY);
    }

    @Override
    public void dispose() {
        worldFrameBuffer.dispose();
        viewFrameBuffer.dispose();
        world.dispose();
    }
}
