package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceScreen;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class S7DetailedBatteryUsage {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.settings.fuelgauge.BatteryHistoryPreference", lpparam.classLoader, "performClick", PreferenceScreen.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    Object mHelper = XposedHelpers.getObjectField(param.thisObject, "mHelper");
                    Bundle args = new Bundle();
                    args.putString("stats", "tmp_bat_history.bin");
                    args.putParcelable("broadcast", (Parcelable) XposedHelpers.callMethod(mHelper, "getBatteryBroadcast"));

                    Object sa = XposedHelpers.callMethod(param.thisObject, "getContext");
                    Resources resources = (Resources) XposedHelpers.callMethod(sa, "getResources");
                    int title = resources.getIdentifier("history_details_title", "string", "com.android.settings");
                    XposedHelpers.callMethod(sa, "startPreferencePanel", "com.android.settings.fuelgauge.BatteryHistoryDetail", args, title, null, null, 0);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
