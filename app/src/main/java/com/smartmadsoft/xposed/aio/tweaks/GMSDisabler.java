package com.smartmadsoft.xposed.aio.tweaks;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GMSDisabler {

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // alternatively just disable an activity:
                    // adb shell su -c "pm disable com.google.android.gms/com.google.android.location.settings.LocationSettingsCheckerActivity"
                    XposedHelpers.findAndHookMethod("com.google.android.location.settings.LocationSettingsCheckerChimeraActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedHelpers.callMethod(param.thisObject, "finish");
                        }
                    });
                }
            });

        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
