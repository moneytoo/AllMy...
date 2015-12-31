package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeskClockAlarm {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            if (lpparam.packageName.equals("com.android.deskclock")) {
                XposedHelpers.findAndHookMethod("com.android.deskclock.DeskClock", lpparam.classLoader, "initViews", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedHelpers.setObjectField(param.thisObject, "mSelectedTab", 0);
                    }
                });
            } else if (lpparam.packageName.equals("com.google.android.deskclock")) {
                XposedHelpers.findAndHookMethod("com.android.deskclock.DeskClock", lpparam.classLoader, "D", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // Clock  4.2.1
                        XposedHelpers.setObjectField(param.thisObject, "dF", 0);
                    }
                });
            } else if (lpparam.packageName.equals("com.lenovo.deskclock")) {
                XposedHelpers.findAndHookMethod("com.lenovo.deskclock.DeskClock", lpparam.classLoader, "initViews", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        XposedHelpers.setObjectField(param.thisObject, "mSelectedTab", 0);
                    }
                });
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
