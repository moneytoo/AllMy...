package com.smartmadsoft.xposed.aio.tweaks;

import android.util.DisplayMetrics;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class K920Cardboard {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("android.view.Display", lpparam.classLoader, "getRealMetrics", DisplayMetrics.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    DisplayMetrics dm = (DisplayMetrics) param.args[0];
                    dm.xdpi = dm.ydpi = dm.xdpi * 3.0f;
                    param.args[0] = dm;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
