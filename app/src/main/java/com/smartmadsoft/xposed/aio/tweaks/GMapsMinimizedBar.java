package com.smartmadsoft.xposed.aio.tweaks;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GMapsMinimizedBar {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.google.android.apps.gmm.base.app.GoogleMapsApplication", lpparam.classLoader), "attachBaseContext", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // >= 9.68.2
                    try {
                        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.google.android.apps.gmm.home.views.HomeBottomSheetView", lpparam.classLoader), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                // Black magic
                                XposedHelpers.setObjectField(param.thisObject, "l", 0);
                                XposedHelpers.setObjectField(param.thisObject, "m", 0);
                            }
                        });
                    } catch (Throwable t) {
                        XposedBridge.log(t);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
