/*
    from https://github.com/GravityBox/GravityBox
 */

package com.smartmadsoft.xposed.aio.tweaks;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class GentleHapticFeedback {
    private static final String CLASS_PHONE_WINDOW_MANAGER = "com.android.internal.policy.impl.PhoneWindowManager";
    private static final String CLASS_IWINDOW_MANAGER = "android.view.IWindowManager";
    private static final String CLASS_WINDOW_MANAGER_FUNCS = "android.view.WindowManagerPolicy.WindowManagerFuncs";

    private static Object mPhoneWindowManager;

    public static void hook() {
        final Class<?> classPhoneWindowManager = XposedHelpers.findClass(CLASS_PHONE_WINDOW_MANAGER, null);

        XposedHelpers.findAndHookMethod(classPhoneWindowManager, "init", Context.class, CLASS_IWINDOW_MANAGER, CLASS_WINDOW_MANAGER_FUNCS, phoneWindowManagerInitHook);
    }

    private static XC_MethodHook phoneWindowManagerInitHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            mPhoneWindowManager = param.thisObject;

            setVirtualKeyVibePattern();
        }
    };

    private static void setVirtualKeyVibePattern() {
        if (mPhoneWindowManager == null) return;

        try {
            XposedHelpers.setObjectField(mPhoneWindowManager, "mVirtualKeyVibePattern", new long[] { 10 });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
