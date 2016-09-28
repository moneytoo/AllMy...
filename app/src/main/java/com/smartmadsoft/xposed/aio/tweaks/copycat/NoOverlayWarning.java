/*
    from https://github.com/apsun/NoOverlayWarning
 */

package com.smartmadsoft.xposed.aio.tweaks.copycat;

import android.view.MotionEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoOverlayWarning {
    // New flag secretly added in Android 6.0.1 it seems
    // https://android.googlesource.com/platform/frameworks/native/+/03a53d1c7765eeb3af0bc34c3dff02ada1953fbf%5E!/
    private static final int FLAG_WINDOW_IS_PARTIALLY_OBSCURED = 0x2;

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(MotionEvent.class, "getFlags", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int flags = (Integer)param.getResult();
                    if ((flags & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {
                        flags &= ~MotionEvent.FLAG_WINDOW_IS_OBSCURED;
                        //XposedBridge.log("AIO: Cleared FLAG_WINDOW_IS_OBSCURED flag");
                    }

                    if ((flags & FLAG_WINDOW_IS_PARTIALLY_OBSCURED) != 0) {
                        flags &= ~FLAG_WINDOW_IS_PARTIALLY_OBSCURED;
                        //XposedBridge.log("AIO: Cleared FLAG_WINDOW_IS_PARTIALLY_OBSCURED flag");
                    }
                    param.setResult(flags);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
