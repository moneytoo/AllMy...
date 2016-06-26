package com.smartmadsoft.xposed.aio.tweaks;

import android.media.AudioManager;
import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MediaStreamDefault {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String pkg = "com.android.server.audio";
            if (Build.VERSION.SDK_INT < 23)
                pkg = "android.media";

            XposedHelpers.findAndHookMethod(pkg + ".AudioService", lpparam.classLoader, "getActiveStreamType", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int suggestedStreamType = (int) param.args[0];
                    int result = (int) param.getResult();
                    if (suggestedStreamType == AudioManager.USE_DEFAULT_STREAM_TYPE && (result == AudioManager.STREAM_RING || result == AudioManager.STREAM_NOTIFICATION))
                        param.setResult(AudioManager.STREAM_MUSIC);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
