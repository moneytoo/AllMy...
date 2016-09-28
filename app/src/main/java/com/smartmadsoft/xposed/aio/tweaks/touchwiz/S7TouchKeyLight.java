package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class S7TouchKeyLight {
    public static boolean spoofATT = false;
    
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.settings.DisplaySettings", lpparam.classLoader), "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    spoofATT = true;
                }

                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    spoofATT = false;
                }
            });

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.settings.Utils", lpparam.classLoader), "readSalesCode", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    if (spoofATT)
                        param.setResult("ATT");
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
