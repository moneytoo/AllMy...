package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoWakeOnCharge {

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "shouldWakeUpWhenPluggedOrUnpluggedLocked", boolean.class, int.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(false);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookUI(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.power.PowerNotificationWarnings", lpparam.classLoader, "playSound", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    int type = (int) param.args[0];
                    if (type == 1) // SOUND_TYPE_CHARGER_CONNECTION
                        param.setResult(null);
                }
            });

            XposedHelpers.findAndHookMethod("com.android.systemui.power.PowerNotificationWarnings", lpparam.classLoader, "showWirelessChargingNotice", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });


            XposedHelpers.findAndHookMethod("com.android.systemui.power.PowerNotificationWarnings", lpparam.classLoader, "showFullBatteryNotice", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    //XposedBridge.log("showFullBatteryNotice");
                    param.setResult(null);
                }
            });

            /*
            XposedHelpers.findAndHookMethod("com.android.systemui.power.PowerNotificationWarnings", lpparam.classLoader, "showFullBatteryNotification", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    XposedBridge.log("showFullBatteryNotification");
                    //param.setResult(null);
                }
            });
            */
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
