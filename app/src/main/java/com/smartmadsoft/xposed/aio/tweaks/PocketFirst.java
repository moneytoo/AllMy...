package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PocketFirst {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity.ResolverComparator", lpparam.classLoader, "getPackageTimeSpent", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0].equals("com.ideashower.readitlater.pro"))
                        param.setResult(Long.MAX_VALUE);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
