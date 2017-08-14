package com.smartmadsoft.xposed.aio;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class FlashlightService extends Service {

    Camera camera;
    Camera.Parameters params;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (camera == null) {
                camera = Camera.open();
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
            } else {
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                camera.stopPreview();
                camera.release();
                camera = null;
                stopSelf();
            }
        } catch (RuntimeException e) {
            // Camera is used by other process
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
