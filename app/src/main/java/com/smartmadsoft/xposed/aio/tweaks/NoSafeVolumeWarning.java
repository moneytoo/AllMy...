/*
    from https://github.com/pylerSM/NoSafeVolumeWarning
 */

package com.smartmadsoft.xposed.aio.tweaks;

import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoSafeVolumeWarning {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String pkg = "com.android.server.audio";
            if (Build.VERSION.SDK_INT < 23)
                pkg = "android.media";

            XposedBridge.hookAllMethods(XposedHelpers.findClass(pkg + ".AudioService", lpparam.classLoader), "enforceSafeMediaVolume", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });

            XposedHelpers.findAndHookMethod(pkg + ".AudioService", lpparam.classLoader, "checkSafeMediaVolume", int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
