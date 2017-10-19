package com.android.grafika.thread;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.SurfaceHolder;

import com.android.grafika.R;
import com.android.grafika.common.BaseHandlerThread;
import com.android.grafika.gles.Drawable2d;
import com.android.grafika.gles.GlUtil;
import com.android.grafika.gles.Sprite2d;
import com.android.grafika.gles.Texture2dProgram;
import com.android.grafika.handler.MotionEventHandler;
import com.android.grafika.helper.CameraHelper;
import com.android.grafika.helper.TextureHelper;


public class RenderThread extends BaseHandlerThread implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = RenderThread.class.getSimpleName();

    private Sprite2d backgroundSprite;
    private Sprite2d triangleSprite;
    private Sprite2d bitmapSprite;

    private final Context context;

    private MotionEventHandler handler;

    // Receives the output from the camera preview.
    private SurfaceTexture surfaceTexture;

    private CameraHelper cameraHelper;

    public RenderThread(String name, Context context) {
        super(name);
        this.context = context;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new MotionEventHandler(this);
    }

    public void updatePosition(int x, int y) {
        backgroundSprite.setPosition(x, getSurfaceWindowHeight() - y);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        drawFrame();
    }

    /**
     * Handles the offscreenSurface-created callback from SurfaceView.  Prepares GLES and the Surface.
     */
    public void surfaceAvailable(SurfaceHolder holder) {
        initGLES();
        initWindowSurface(holder.getSurface());
        initGlProgram();
        initTextures();
        initCamera();
    }

    private void initCamera() {
        cameraHelper = new CameraHelper(surfaceTexture);
        cameraHelper.startCameraPreview();
    }

    private void initGlProgram() {
        program2D = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D);
        programExt = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT);
    }

    private void initTextures() {
        int textureId = programExt.createTextureObject();

        backgroundSprite = new Sprite2d(Drawable2d.Prefab.TRIANGLE);
        backgroundSprite.setTexture(textureId);

        triangleSprite = new Sprite2d(Drawable2d.Prefab.RECTANGLE);
        triangleSprite.setTexture(textureId);

        bitmapSprite = new Sprite2d(Drawable2d.Prefab.RECTANGLE);
        bitmapSprite.setTexture(TextureHelper.loadTexture(context, R.drawable.ic_launcher));

        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);
    }


    public void surfaceChanged(int width, int height) {
        windowUpdated(width, height);

        updateSprite(backgroundSprite);
        updateSprite(triangleSprite);
        updateSprite(bitmapSprite);
    }

    private void updateSprite(Sprite2d sprite) {
        int width = getSurfaceWindowWidth();
        int height = getSurfaceWindowHeight();

        float minDimen = Math.min(width, height);
        int angle = 0;

        if (sprite == backgroundSprite) {
            minDimen *= 0.25f;
        } else if (sprite == bitmapSprite) {
            minDimen *= 0.3f;
        } else {
            angle = height > width ? 90 : 0;
        }

        float scaleX = minDimen * cameraHelper.getCameraAspect();
        float scaleY = minDimen;

        sprite.setScale(scaleX, scaleY);
        sprite.setRotation(angle);

        // center of the screen
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        sprite.setPosition(centerX, centerY);
    }

    private void drawFrame() {
        surfaceTexture.updateTexImage();

        GlUtil.checkGlError("drawFrame start");

        draw(triangleSprite, programExt);
        draw(backgroundSprite, programExt);
        draw(bitmapSprite, program2D);

        swapBuffers();

        GlUtil.checkGlError("drawFrame done");
    }

    @Override
    public boolean quit() {
        releaseCamera();
        releaseGl();
        releaseEglCore();

        Log.d(TAG, "RenderThread quitting");

        return super.quit();
    }

    private void releaseCamera() {
        if (cameraHelper != null) {
            cameraHelper.releaseCamera();
            cameraHelper = null;
        }
    }

    public void sendPositionUpdated(int posX, int posY) {
        handler.sendPositionUpdated(posX, posY);
    }

    public void sendSurfaceCreated(SurfaceHolder holder) {
        handler.sendSurfaceCreated(holder);
    }

    public void sendSurfaceChanged(int width, int height) {
        handler.sendSurfaceChanged(width, height);
    }
}