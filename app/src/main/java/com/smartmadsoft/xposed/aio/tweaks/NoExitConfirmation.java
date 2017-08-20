package com.smartmadsoft.xposed.aio.tweaks;

import android.os.SystemClock;
import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoExitConfirmation {
    public static void hookBackup(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("mobi.infolife.appbackup.ui.screen.mainpage.ActivityBrPage", lpparam.classLoader, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    if (((KeyEvent) param.args[1]).getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        XposedHelpers.callMethod(param.thisObject, "finish");
                        param.setResult(true);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookCG(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.circlegate.tt.transit.android.activity.MainActivity", lpparam.classLoader, "onBackPressedAfterListeners", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    XposedHelpers.setObjectField(param.thisObject, "backTimeStamp", SystemClock.elapsedRealtime());
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookSolid(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllConstructors(XposedHelpers.findClass("pl.solidexplorer.SolidExplorer", lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.setObjectField(param.thisObject, "b", true);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
