package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OneWayBrightness {
    public static float lastLux = 0.0f;

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader), "handleLightSensorEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                float lux = (float) param.args[1];
                //XposedBridge.log("onewaybrightness: handleLightSensorEvent: " + Float.toString(lux));
                if (lux < lastLux)
                    param.setResult(null);
                else
                    lastLux = lux;
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader), "setLightSensorEnabled", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                boolean enable = (boolean) param.args[0];
                //XposedBridge.log("onewaybrightness: setLightSensorEnabled: " + Boolean.toString(enable));
                if (!enable)
                    lastLux = 0.0f;
            }
        });
    }
}
