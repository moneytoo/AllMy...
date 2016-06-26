/*
    parts based on https://github.com/GravityBox/GravityBox
 */

package com.smartmadsoft.xposed.aio.tweaks;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MediaKeys {
    //public final static int FLAG_INTERACTIVE = 0x20000000;

    static PowerManager mPowerManager;
    static AudioManager mAudioManager;
    static boolean mIsLongPress = false;

    //static KeyEvent lastDownKeyEvent;

    static String pkg;
    //static Class ClassMediaSessionLegacyHelper;

    static CameraManager mCameraManager;
    static String mCameraId;
    static boolean isTorchOn = false;

    @TargetApi(20)
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            //ClassMediaSessionLegacyHelper = XposedHelpers.findClass("android.media.session.MediaSessionLegacyHelper", lpparam.classLoader);

            pkg = "com.android.internal.policy.impl";
            if (Build.VERSION.SDK_INT >= 23)
                pkg = "com.android.server.policy";

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    KeyEvent event = (KeyEvent) param.args[0];
                    int keycode = event.getKeyCode();
                    /*
                    int policyFlags = (int) param.args[1];
                    final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
                    XposedBridge.log("AIO: interceptKeyBeforeQueueing:" + keycode + ", down=" + down + ", policyFlags=" + policyFlags + ", event.getFlags=" + event.getFlags());
                    */

                    if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN || keycode == KeyEvent.KEYCODE_VOLUME_UP) {
                        //if (down)
                            //lastDownKeyEvent = event;
                        if (mPowerManager == null)
                            mPowerManager = (PowerManager) XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
                        PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AllMy_MediaKeys");
                        wakeLock.acquire(1500);

                        Object telecomManager = XposedHelpers.callMethod(param.thisObject, "getTelecommService");
                        if (telecomManager != null) {
                            boolean isRinging = (boolean) XposedHelpers.callMethod(telecomManager, "isRinging");
                            if (isRinging)
                                return;
                        }

                        if (mAudioManager == null) {
                            Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                        }

                        boolean isShowingKeyguard = false;
                        try {
                            Object mKeyguardDelegate = XposedHelpers.getObjectField(param.thisObject, "mKeyguardDelegate");
                            isShowingKeyguard = (boolean) XposedHelpers.callMethod(mKeyguardDelegate, "isShowing");
                            //XposedBridge.log("AIO: isShowingKeyguard=" + isShowingKeyguard);
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }

                        if ((event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0 && (!mPowerManager.isInteractive() || isShowingKeyguard)) {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                mIsLongPress = false;
                                handleVolumeLongPress(param.thisObject, keycode);
                            } else {
                                handleVolumeLongPressAbort(param.thisObject);
                                if (!mIsLongPress) {
                                    //sendVolumeKeyEvent(param.thisObject, KeyEvent.KEYCODE_VOLUME_UP);
                                    //sendVolumeKeyEvent2(param.thisObject, lastDownKeyEvent);
                                    //dispatchVolKey(param.thisObject, lastDownKeyEvent);

                                    //AudioManager.FLAG_SHOW_UI
                                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, keycode == KeyEvent.KEYCODE_VOLUME_UP ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 0);
                                }
                            }
                            // do not pass to user
                            param.setResult(0);
                            return;
                        }
                    }
                }
                /*
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int res = (int) param.getResult();
                    XposedBridge.log("AIO: interceptKeyBeforeQueueing returns " + res);
                }
                */
            });

            XposedBridge.hookAllConstructors(XposedHelpers.findClass(pkg + ".PhoneWindowManager", lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    Runnable mVolumeUpLongPress = new Runnable() {
                        @Override
                        public void run() {
                            mIsLongPress = true;
                            if (mAudioManager != null && mAudioManager.isMusicActive())
                                sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_NEXT);
                            else
                                toggleFlashlight(param.thisObject);
                        };
                    };

                    Runnable mVolumeDownLongPress = new Runnable() {
                        @Override
                        public void run() {
                            mIsLongPress = true;
                            sendMediaButtonEvent(param.thisObject, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                        };
                    };

                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeUpLongPress", mVolumeUpLongPress);
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeDownLongPress", mVolumeDownLongPress);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private static void handleVolumeLongPress(Object phoneWindowManager, int keycode) {
        Handler mHandler = (Handler) XposedHelpers.getObjectField(phoneWindowManager, "mHandler");
        Runnable mVolumeUpLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress");
        Runnable mVolumeDownLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeDownLongPress");

        mHandler.postDelayed(keycode == KeyEvent.KEYCODE_VOLUME_UP ? mVolumeUpLongPress : mVolumeDownLongPress, ViewConfiguration.getLongPressTimeout());
    }

    private static void handleVolumeLongPressAbort(Object phoneWindowManager) {
        Handler mHandler = (Handler) XposedHelpers.getObjectField(phoneWindowManager, "mHandler");
        Runnable mVolumeUpLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress");
        Runnable mVolumeDownLongPress = (Runnable) XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeDownLongPress");

        mHandler.removeCallbacks(mVolumeUpLongPress);
        mHandler.removeCallbacks(mVolumeDownLongPress);
    }

    private static void sendMediaButtonEvent(Object phoneWindowManager, int code) {
        long eventtime = SystemClock.uptimeMillis();
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        dispatchMediaButtonEvent(keyEvent);

        keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        dispatchMediaButtonEvent(keyEvent);
    }

    @TargetApi(19)
    private static void dispatchMediaButtonEvent(KeyEvent keyEvent) {
        try {
            mAudioManager.dispatchMediaKeyEvent(keyEvent);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    /*
    private static void sendVolumeKeyEvent(Object phoneWindowManager, int keycode) {
        KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        Context mContext = (Context) XposedHelpers.getObjectField(phoneWindowManager, "mContext");

        Object helper = XposedHelpers.callStaticMethod(ClassMediaSessionLegacyHelper, "getHelper", mContext);
        XposedHelpers.callMethod(helper, "sendVolumeKeyEvent", newEvent, true);
    }

    private static void sendVolumeKeyEvent2(Object phoneWindowManager, KeyEvent event) {
        //KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        Context mContext = (Context) XposedHelpers.getObjectField(phoneWindowManager, "mContext");

        Object helper = XposedHelpers.callStaticMethod(ClassMediaSessionLegacyHelper, "getHelper", mContext);
        XposedHelpers.callMethod(helper, "sendVolumeKeyEvent", event, true);
    }

    private static void dispatchVolKey(Object phoneWindowManager, KeyEvent event) {
        XposedHelpers.callMethod(phoneWindowManager, "dispatchMediaKeyWithWakeLockToAudioService", event);
        XposedHelpers.callMethod(phoneWindowManager, "dispatchMediaKeyWithWakeLockToAudioService", KeyEvent.changeAction(event, KeyEvent.ACTION_UP));
    }
    */

    private static void toggleFlashlight(Object phoneWindowManager) {
        Context mContext = (Context) XposedHelpers.getObjectField(phoneWindowManager, "mContext");

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
            Intent intent = new Intent("com.smartmadsoft.xposed.aio.FLASHLIGHT_TOGGLE");
            mContext.sendBroadcast(intent);
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
}
