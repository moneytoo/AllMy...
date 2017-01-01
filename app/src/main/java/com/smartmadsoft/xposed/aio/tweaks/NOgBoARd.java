package com.smartmadsoft.xposed.aio.tweaks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NOgBoARd {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.google.android.apps.inputmethod.libs.framework.core.GoogleInputMethodService", lpparam.classLoader), "setKeyboardViewShown", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if ((int) XposedHelpers.callMethod(param.args[0], "ordinal") == 0) // KeyboardViewDef$Type.HEADER
                        param.args[1] = false;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
