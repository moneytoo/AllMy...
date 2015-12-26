package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BatteryLightDisabler {
    public static int mode;
    public static boolean screenOn = true;

    public static Object BatteryService;
    public static Object Led;

    public static Runnable runnable;

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        runnable = new Runnable() {
            public void run() {
                XposedHelpers.callMethod(Led, "updateLightsLocked");
            }
        };

        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.server.BatteryService", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                BatteryService = param.thisObject;
            }
        });

        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.server.BatteryService.Led", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Led = param.thisObject;
            }
        });


        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.BatteryService.Led", lpparam.classLoader), "updateLightsLocked", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (mode > 0 || screenOn)
                    XposedHelpers.setObjectField(BatteryService, "mLightEnabled", false);
                else
                    XposedHelpers.setObjectField(BatteryService, "mLightEnabled", true);
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.notification.ZenModeHelper", lpparam.classLoader), "dispatchOnZenModeChanged", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mode = (Integer) XposedHelpers.getObjectField(param.thisObject, "mZenMode");
                runnable.run();
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.power.PowerManagerService", lpparam.classLoader), "wakeUpNoUpdateLocked", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                screenOn = true;
                runnable.run();
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.power.PowerManagerService", lpparam.classLoader), "goToSleepNoUpdateLocked", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                screenOn = false;
                runnable.run();
            }
        });
    }
}
