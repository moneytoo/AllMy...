package com.smartmadsoft.xposed.aio.tweaks.cyanogenmod;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RemapVolume {

    static PowerManager mPowerManager;
    static Context mContext;
    static int MSG_DISPATCH_VOLKEY_WITH_WAKE_LOCK = -1;

    static CameraManager mCameraManager;
    static String mCameraId;
    static Object mTorchManager;
    static boolean isTorchOn = false;

    static String pkg;
    static boolean tweakTorch = false;
    static boolean tweakPlayPause = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam, boolean enablePlayPause, boolean enableTorch) {
        try {
            tweakPlayPause = enablePlayPause;
            tweakTorch = enableTorch;

            pkg = "com.android.internal.policy.impl";
            if (Build.VERSION.SDK_INT >= 23)
                pkg = "com.android.server.policy";

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("interceptKeyBeforeQueueing");
                    int keycode = ((KeyEvent) param.args[0]).getKeyCode();

                    if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN || keycode == KeyEvent.KEYCODE_VOLUME_UP) {
                        if (mPowerManager == null)
                            mPowerManager = (PowerManager) XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
                        if (mPowerManager != null) {
                            PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AllMy_ButtonRemap");
                            wakeLock.acquire(1500);
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "scheduleLongPressKeyEvent", KeyEvent.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("scheduleLongPressKeyEvent");

                    if (tweakPlayPause) {
                        int keycode = (int) param.args[1];
                        if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                            param.args[1] = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                    }

                    if (mContext == null)
                        mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                }
            });

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "isMusicActive", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("isMusicActive");
                    param.setResult(true);
                }
            });

            if (tweakTorch) {
                XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager$PolicyHandler", lpparam.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("handleMessage");
                        if (tweakTorch) {
                            Message msg = (Message) param.args[0];
                            if (MSG_DISPATCH_VOLKEY_WITH_WAKE_LOCK < 0) {
                                MSG_DISPATCH_VOLKEY_WITH_WAKE_LOCK = XposedHelpers.getStaticIntField(XposedHelpers.findClass(pkg + ".PhoneWindowManager", lpparam.classLoader), "MSG_DISPATCH_VOLKEY_WITH_WAKE_LOCK");
                            }
                            if (msg.what == MSG_DISPATCH_VOLKEY_WITH_WAKE_LOCK) {
                                KeyEvent event = (KeyEvent) msg.obj;

                                if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) {
                                    if (!isMusicActive(mContext)) {
                                        toggleTorch(lpparam, param);
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                    }
                });
            }

        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    static void toggleTorch(final XC_LoadPackage.LoadPackageParam lpparam, XC_MethodHook.MethodHookParam param) {
        XposedBridge.log("toggleTorch");
        if (Build.VERSION.SDK_INT >= 23) {
            if (mCameraManager == null) {
                mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                String cameraId = null;
                try {
                    cameraId = getCameraId(mCameraManager);
                } catch (Throwable e) {
                    return;
                } finally {
                    mCameraId = cameraId;
                }
            }
            try {
                isTorchOn = !isTorchOn;
                mCameraManager.setTorchMode(mCameraId, isTorchOn);
            } catch (Throwable e) {
                return;
            }
        } else {
            if (mTorchManager == null)
                mTorchManager = XposedHelpers.callMethod(mContext, "getSystemService", XposedHelpers.getStaticObjectField(XposedHelpers.findClass("android.content.Context", lpparam.classLoader), "TORCH_SERVICE"));
            XposedHelpers.callMethod(mTorchManager, "toggleTorch");
        }
    }

    static String getCameraId(CameraManager cameraManager) throws CameraAccessException {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        }
        return null;
    }

    static boolean isMusicActive(Context context) {
        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null)
            return false;
        return am.isMusicActive();
    }
}
