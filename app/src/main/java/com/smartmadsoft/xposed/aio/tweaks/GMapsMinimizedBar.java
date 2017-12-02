package com.smartmadsoft.xposed.aio.tweaks;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GMapsMinimizedBar {
    private static boolean firstHit = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.google.android.apps.gmm.base.app.GoogleMapsApplication", lpparam.classLoader), "attachBaseContext", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    // <= 9.66.1
                    try {
                        XposedHelpers.findAndHookMethod("com.google.android.apps.gmm.home.views.HomeBottomSheetView", lpparam.classLoader, "c", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                XposedHelpers.setObjectField(param.thisObject, "b", 0.0f);
                            }
                        });
                    } catch (Throwable t) {}

                    // >= 9.67.1
                    try {
                        XposedHelpers.findAndHookMethod("com.google.android.apps.gmm.home.views.HomeBottomSheetView", lpparam.classLoader, "b", int.class, boolean.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (firstHit) {
                                    param.args[0] = 0;
                                    firstHit = false;
                                }
                            }
                        });
                    } catch (Throwable t) {
                        XposedBridge.log(t);
                    }


                    try {
                        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.google.android.apps.gmm.home.views.HomeBottomSheetView", lpparam.classLoader), new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                firstHit = true;
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
