package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.os.Vibrator;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GentleHapticFeedbackTouchWiz {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.internal.policy.impl.sec.SamsungPhoneWindowManager", lpparam.classLoader), "performHapticFeedbackLw", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int n = (int) param.args[1];
                    //XposedBridge.log("AIO: SEC performHapticFeedbackLw n=" + n);
                    if (n == 14) {
                        Vibrator mVibrator = (Vibrator) XposedHelpers.getObjectField(param.thisObject, "mVibrator");
                        if (mVibrator.hasVibrator())
                            mVibrator.vibrate(10);
                        param.setResult(true);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
