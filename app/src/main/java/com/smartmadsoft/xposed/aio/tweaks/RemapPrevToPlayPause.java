package com.smartmadsoft.xposed.aio.tweaks;

import android.os.Build;
import android.os.PowerManager;
import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RemapPrevToPlayPause {

    static boolean mIsLongPress = false;
    static PowerManager mPowerManager;

    static int keycode;

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String pkg = "com.android.internal.policy.impl";
            if (Build.VERSION.SDK_INT >= 23)
                pkg = "com.android.server.policy";

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    keycode = ((KeyEvent) param.args[0]).getKeyCode();

                    if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        mIsLongPress = (boolean) XposedHelpers.getObjectField(param.thisObject, "mIsLongPress");
                        if (mIsLongPress) {
                            if (mPowerManager == null)
                                mPowerManager = (PowerManager) XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
                            if (mPowerManager != null) {
                                PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AllMy_ButtonRemap");
                                wakeLock.acquire(1000);
                            }
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "scheduleLongPressKeyEvent", KeyEvent.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int keycode = (int) param.args[1];
                    if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                        param.args[1] = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                }
            });

            XposedHelpers.findAndHookMethod(pkg + ".PhoneWindowManager", lpparam.classLoader, "isMusicActive", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (mIsLongPress && keycode == KeyEvent.KEYCODE_VOLUME_DOWN)
                        param.setResult(true);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
