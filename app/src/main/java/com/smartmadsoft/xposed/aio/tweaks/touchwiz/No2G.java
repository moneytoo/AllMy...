package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class No2G {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.phone.TeleServiceFeature", lpparam.classLoader, "hasFeature", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    //XposedBridge.log("AIO: hasFeature: " + (String) param.args[0] + "=" + Boolean.toString((boolean)param.getResult()));
                    if (((String) param.args[0]).equals("cust_network_sel_menu4_add_ltewcdma"))
                        param.setResult(true);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
