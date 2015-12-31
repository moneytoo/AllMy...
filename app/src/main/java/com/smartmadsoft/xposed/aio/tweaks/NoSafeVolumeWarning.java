/*
    from https://github.com/pylerSM/NoSafeVolumeWarning
 */

package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class NoSafeVolumeWarning {
    public static void hook() {
        try {
            final Class<?> AudioService = XposedHelpers.findClass("android.media.AudioService", null);

            XposedHelpers.findAndHookMethod(AudioService, "enforceSafeMediaVolume", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });

            XposedHelpers.findAndHookMethod(AudioService, "checkSafeMediaVolume", int.class, int.class, int.class, new XC_MethodHook() {
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
