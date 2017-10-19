package com.android.grafika.common;


import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.android.grafika.gles.EglCore;
import com.android.grafika.gles.GlUtil;
import com.android.grafika.gles.Sprite2d;
import com.android.grafika.gles.Texture2dProgram;
import com.android.grafika.gles.WindowSurface;

public abstract class BaseHandlerThread extends HandlerThread {

    private static final String TAG = BaseHandlerThread.class.getSimpleName();

    private EglCore eglCore;
    private WindowSurface windowSurface;

    // program handle for rendering "normal" 2D textures
    protected Texture2dProgram program2D;

    // program handle for rendering "external" 2D textures
    protected Texture2dProgram programExt;

    // eglSurface dimensions
    private int windowSurfaceHeight;
    private int windowSurfaceWidth;

    // Orthographic projection matrix.
    private float[] displayProjectionMatrix = new float[16];

    public BaseHandlerThread(String name) {
        super(name);
    }

    protected void initGLES() {
        // Prepare EGL and open the camera before we start handling messages.
        eglCore = new EglCore();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    protected void initWindowSurface(Surface surface) {
        windowSurface = new WindowSurface(eglCore, surface, false);
        windowSurface.makeCurrent();
    }

    /**
     * Releases most of the GL resources we currently hold (anything allocated by
     * surfaceAvailable()).
     * <p>
     * Does not release EglCore.
     */
    protected void releaseGl() {
        GlUtil.checkGlError("releaseGl start");

        if (windowSurface != null) {
            windowSurface.release();
            windowSurface = null;
        }

        if (programExt != null) {
            programExt.release();
            programExt = null;
        }

        GlUtil.checkGlError("releaseGl done");

        eglCore.makeNothingCurrent();

        Log.d(TAG, "Release Gl done");
    }

    protected void releaseEglCore() {
        if (eglCore != null) {
            eglCore.release();
            eglCore = null;
        }
        Log.d(TAG, "EGLCore release done");
    }

    protected void swapBuffers() {
        windowSurface.swapBuffers();
    }

    protected void windowUpdated(int width, int height) {
        windowSurfaceWidth = width;
        windowSurfaceHeight = height;

        // Use full window.
        GLES20.glViewport(0, 0, width, height);

        // Simple orthographic projection, with (0,0) in lower-left corner.
        Matrix.orthoM(displayProjectionMatrix, 0, 0, width, 0, height, -1, 1);
    }

    protected void draw(Sprite2d sprite, Texture2dProgram program) {
        sprite.draw(program, displayProjectionMatrix);
    }

    protected int getSurfaceWindowWidth() {
        return windowSurfaceWidth;
    }

    protected int getSurfaceWindowHeight() {
        return windowSurfaceHeight;
    }
}
