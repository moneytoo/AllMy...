package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class LedOffDuringDnD {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.server.notification.ZenModeHelper", lpparam.classLoader, "dispatchOnZenModeChanged", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    handleZenMode(param);
                }
            });

            XposedHelpers.findAndHookMethod("com.android.server.notification.ZenModeHelper", lpparam.classLoader, "onSystemReady", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    handleZenMode(param);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private static void handleZenMode(XC_MethodHook.MethodHookParam param) {
        int mZenMode = (int) XposedHelpers.getObjectField(param.thisObject, "mZenMode");
        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

        String prefs[]= { "led_indicator_charing", "led_indicator_low_battery", "led_indicator_missed_event", "led_indicator_voice_recording" };
        for (String pref : prefs) {
            int newVal = (mZenMode > 0 ? 0 : 1);
            XposedHelpers.callStaticMethod(Settings.System.class, "putIntForUser", mContext.getContentResolver(), pref, newVal, XposedHelpers.getStaticIntField(UserHandle.class, "USER_CURRENT"));
        }
    }
}
