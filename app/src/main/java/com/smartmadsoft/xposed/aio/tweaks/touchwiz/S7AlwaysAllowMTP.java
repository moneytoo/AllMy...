package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.app.AlertDialog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class S7AlwaysAllowMTP {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.samsung.android.MtpApplication.USBConnection", lpparam.classLoader, "showDiaglog", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    //XposedBridge.log("AIO: showDiaglog");
                    AlertDialog dialog = (AlertDialog) XposedHelpers.getObjectField(param.thisObject, "dialog");
                    Object mReceiver = XposedHelpers.getObjectField(param.thisObject, "mReceiver");
                    dialog.dismiss();
                    XposedHelpers.callMethod(mReceiver, "changeMtpMode");
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
