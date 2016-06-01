package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MediaStreamDefault {
    public static final int STREAM_MUSIC = 3;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("android.media.AudioSystem", lpparam.classLoader, "isStreamActive", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if ((int) param.args[0] == STREAM_MUSIC)
                        param.setResult(true);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
