package com.android.grafika.handler;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import com.android.grafika.RenderThread;

import java.lang.ref.WeakReference;


public class MotionEventHandler extends Handler {

    private final int MSG_SURFACE_AVAILABLE = 0;
    private final int MSG_SURFACE_CHANGED = 1;
    private final int MSG_POSITION = 2;

    private final WeakReference<RenderThread> weakThread;

    public MotionEventHandler(RenderThread thread) {
        weakThread = new WeakReference<RenderThread>(thread);
    }

    @Override
    public void handleMessage(Message msg) {
        RenderThread renderThread = weakThread.get();

        if (renderThread == null) {
            return;
        }

        switch (msg.what) {
            case MSG_SURFACE_AVAILABLE:
                renderThread.surfaceAvailable((SurfaceHolder) msg.obj);
                break;
            case MSG_SURFACE_CHANGED:
                renderThread.surfaceChanged(msg.arg1, msg.arg2);
                break;
            case MSG_POSITION:
                renderThread.updatePosition(msg.arg1, msg.arg2);
                break;
            default:
                throw new RuntimeException("unknown message " + msg.what);
        }
    }

    public void sendPositionUpdated(int x, int y) {
        sendMessage(obtainMessage(MSG_POSITION, x, y));
    }

    public void sendSurfaceCreated(SurfaceHolder holder) {
        sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE, holder));
    }

    public void sendSurfaceChanged(int width, int height) {
        sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height));
    }
}
