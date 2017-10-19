package com.android.grafika;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.android.grafika.gles.GlUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements Renderer {

    private static short indices[];
    private final float[] mtrxProjection = new float[16];

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer uvBuffer;

    private Context mContext;
    private int program;
    private int textureTarget;


    GLRenderer(Context c) {
        mContext = c;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        Render();
    }

    private void Render() {

        // Set our shader programm
        GLES20.glUseProgram(program);


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // get handle to vertex shader's aPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(program, "a_texCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mtrxProjection, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(program, "s_texture");

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int) (float) width, (int) (float) height);

        // Simple orthographic projection, with (0,0) in lower-left corner.
        Matrix.orthoM(mtrxProjection, 0, 0, width, 0, height, -1, 1);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        textureTarget = GLES20.GL_TEXTURE_2D;

        // Create the triangles
        setupTriangle();
        // Create the image information
        setupImage();

        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        // Create the shaders, images
        String vs_Image = "uniform mat4 uMVPMatrix;" +
                "attribute vec4 aPosition;" +
                "attribute vec2 a_texCoord;" +
                "varying vec2 v_texCoord;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * aPosition;" +
                "  v_texCoord = a_texCoord;" +
                "}";

        String fs_Image = "precision mediump float;" +
                "varying vec2 v_texCoord;" +
                "uniform sampler2D s_texture;" +
                "void main() {" +
                "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                "}";

        program = GlUtil.createProgram(vs_Image, fs_Image);
    }

    private void setupImage() {
        // Create our UV coordinates.
        float[] uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        // The texture buffer
        uvBuffer = GlUtil.createFloatBuffer(uvs);


        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);

        loadTexture(bmp);
    }

    private void loadTexture(Bitmap bitmap) {
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(textureTarget, texturenames[0]);

        // Set filtering
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(textureTarget, 0, bitmap, 0);

        // We are done using the bitmap so we should recycle it.
        bitmap.recycle();

    }

    private void setupTriangle() {

        float[] vertices = new float[]{
                0.0f, 400.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                400.0f, 0.0f, 0.0f,
                400.0f, 400.0f, 0.0f,
        };

        vertexBuffer = GlUtil.createFloatBuffer(vertices);

        indices = new short[]{0, 1, 2, 0, 2, 3};

        drawListBuffer = GlUtil.createShortBuffer(indices);
    }
}
