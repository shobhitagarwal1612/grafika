package com.android.grafika;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DoublePictureActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = DoublePictureActivity.class.getSimpleName();

    private RenderThread renderThread;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);

        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        Log.d(TAG, "onCreate complete: " + this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        renderThread = new RenderThread("RenderThread", this);
        renderThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (renderThread == null) {
            Log.e(TAG, "Render thread null. Ignoring touch events");
            return false;
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int posX = (int) e.getX();
                int posY = (int) e.getY();
                renderThread.sendPositionUpdated(posX, posY);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        renderThread.quit();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        if (surfaceHolder != null) {
            throw new RuntimeException("surfaceHolder is already set");
        }

        if (renderThread != null) {
            renderThread.sendSurfaceCreated(holder);
        }

        surfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged holder=" + holder);
        if (renderThread != null) {
            renderThread.sendSurfaceChanged(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed holder=" + holder);
        surfaceHolder = null;
    }
}