package com.smartmadsoft.xposed.aio.tweaks;

import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class K920KeysHapticFix {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int keyCode = ((KeyEvent) param.args[0]).getKeyCode();
                int flag = (int) param.args[1];

                if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK)
                    param.args[1] = flag | 2;
            }
        });
    }
}
