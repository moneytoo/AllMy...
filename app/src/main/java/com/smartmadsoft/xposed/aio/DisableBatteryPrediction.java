package com.smartmadsoft.xposed.aio;

import android.content.Intent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DisableBatteryPrediction {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class batteryStats = XposedHelpers.findClass("android.os.BatteryStats", lpparam.classLoader);

            XposedHelpers.findAndHookMethod("com.android.settings.fuelgauge.BatteryHistoryChart", lpparam.classLoader, "setStats", batteryStats, Intent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    long mHistDataEnd = (long) XposedHelpers.getObjectField(param.thisObject, "mHistDataEnd");
                    long mEndDataWallTime = (long) XposedHelpers.getObjectField(param.thisObject, "mEndDataWallTime");

                    XposedHelpers.setObjectField(param.thisObject, "mHistEnd", mHistDataEnd);
                    XposedHelpers.setObjectField(param.thisObject, "mEndWallTime", mEndDataWallTime);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
