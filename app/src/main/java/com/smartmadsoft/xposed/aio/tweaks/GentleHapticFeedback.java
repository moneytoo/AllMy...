/*
    from https://github.com/GravityBox/GravityBox
 */

package com.smartmadsoft.xposed.aio.tweaks;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class GentleHapticFeedback {
    private static Object mPhoneWindowManager;

    static long vibePattern;

    public static void hook(int value) {
        try {
            vibePattern = value;

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", null), "init", Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mPhoneWindowManager = param.thisObject;

                    if (mPhoneWindowManager == null)
                        return;

                    try {
                        XposedHelpers.setObjectField(mPhoneWindowManager, "mVirtualKeyVibePattern", new long[]{vibePattern});
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
