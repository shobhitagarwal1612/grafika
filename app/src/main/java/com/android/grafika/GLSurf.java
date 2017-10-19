package com.android.grafika;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GLSurf extends GLSurfaceView {

    public GLSurf(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        GLRenderer mRenderer = new GLRenderer(context);
        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
