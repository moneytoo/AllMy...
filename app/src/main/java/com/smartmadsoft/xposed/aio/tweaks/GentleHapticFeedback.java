/*
    from https://github.com/GravityBox/GravityBox
 */

package com.smartmadsoft.xposed.aio.tweaks;

import android.content.Context;
import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GentleHapticFeedback {
    private static Object mPhoneWindowManager;

    static long vibePattern;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam, int value) {
        try {
            vibePattern = value;

            String pkg = "com.android.internal.policy.impl";
            if (Build.VERSION.SDK_INT >= 23)
                pkg = "com.android.server.policy";

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(pkg + ".PhoneWindowManager", lpparam.classLoader), "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mPhoneWindowManager = param.thisObject;

                    if (mPhoneWindowManager == null)
                        return;

                    try {
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mVirtualKeyVibePattern", new long[]{vibePattern});
                        if (Build.MANUFACTURER.toLowerCase().contains("lenovo")) {
                            XposedHelpers.setObjectField(mPhoneWindowManager, "mVirtualKeyVibePatternDown", new long[]{vibePattern});
                            XposedHelpers.setObjectField(mPhoneWindowManager, "mKeyboardTapVibePattern", new long[]{vibePattern});
                            XposedHelpers.setObjectField(mPhoneWindowManager, "mLongPressVibePattern", new long[]{vibePattern*2});
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(t);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
