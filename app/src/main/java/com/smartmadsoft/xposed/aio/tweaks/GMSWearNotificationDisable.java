package com.smartmadsoft.xposed.aio.tweaks;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GMSWearNotificationDisable {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // Prevent notification containing text "Disconnected. Touch to reconnect."
                    // Find class containing intent "com.google.android.gms.wearable.node.connection.NOTIFICATION_DISMISSED"

                    try {
                        XposedHelpers.findAndHookMethod("aats", lpparam.classLoader, "a", CharSequence.class, CharSequence.class, PendingIntent.class, boolean.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.setResult(null);
                            }
                        });
                        return;
                    } catch (Throwable t) {}

                    // For gms_9.0.83_238
                    XposedHelpers.findAndHookMethod("abog", lpparam.classLoader, "a", CharSequence.class, CharSequence.class, PendingIntent.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null);
                        }
                    });
                    /*
                    // For gms_8.4.89_238
                    XposedHelpers.findAndHookMethod("com.google.android.gms.wearable.node.a.b", lpparam.classLoader, "a", CharSequence.class, CharSequence.class, PendingIntent.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null);
                        }
                    });
                    */
                }
            });

        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
