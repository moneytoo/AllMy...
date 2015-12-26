package com.smartmadsoft.xposed.aio.tweaks;

import android.os.Build;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AlwaysSoftwareMenu {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        String methodName = "hasMenuKeyEnabled";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            methodName = "hasPermanentMenuKey";

        XposedHelpers.findAndHookMethod("com.android.server.wm.WindowManagerService", lpparam.classLoader, methodName, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return false;
            }
        });
    }
}
