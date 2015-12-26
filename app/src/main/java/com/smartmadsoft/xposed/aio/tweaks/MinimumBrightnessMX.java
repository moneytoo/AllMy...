package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MinimumBrightnessMX {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod("com.mxtech.videoplayer.widget.BrightnessBar", lpparam.classLoader, "a", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int n = (Integer) param.args[0];
                int n2 = (Integer) param.args[1];

                double d = 0.064 + 0.936 / (double)n * (double)n2;
                param.setResult(d*d);
            }
        });

        findAndHookMethod("com.mxtech.videoplayer.widget.BrightnessBar", lpparam.classLoader, "a", int.class, double.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                int n = (Integer) param.args[0];
                double d = (Double) param.args[1];

                param.setResult((int)Math.round((double)((Math.sqrt((double)d) - 0.064) * (double)n / 0.936)));
            }
        });
    }
}
