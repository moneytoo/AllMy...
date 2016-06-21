package com.smartmadsoft.xposed.aio.tweaks;

import android.util.DisplayMetrics;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class K920Cardboard {
    static boolean fakeAirplaneMode = false;

    public static void hookAll(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Fixes DPI for Google Cardboard
            XposedHelpers.findAndHookMethod("android.view.Display", lpparam.classLoader, "getRealMetrics", DisplayMetrics.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    DisplayMetrics dm = (DisplayMetrics) param.args[0];
                    dm.xdpi = dm.ydpi = dm.xdpi * 3.0f;
                    param.args[0] = dm;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookAndroid(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Disables virtual key tap up feedback/vibration
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader), "performHapticFeedbackLw", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int effectId = (int) param.args[1];
                    if (effectId == 7) // mVirtualKeyVibePatternUp
                        param.setResult(null);
                }
            });

            // Disables dialog offering to end airplane mode on power up
            XposedHelpers.findAndHookMethod("com.android.server.StartShutdownService", lpparam.classLoader, "loadSettings", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    fakeAirplaneMode = true;
                }
            });

            XposedHelpers.findAndHookMethod("com.android.server.StartShutdownService", lpparam.classLoader, "isAirplaneModeOn", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    if (fakeAirplaneMode) {
                        fakeAirplaneMode = false;
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookUI(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Disable automatic notification panel expansion if there are no notifications
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader), "isNeadToOpenQSPanel", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookTelecom(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Fixes SIP calls - requires setting "Make calls with = Ask first" and using Google UI
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.telecom.CallsManager", lpparam.classLoader), "phoneAccountSelected", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("AIO: phoneAccountSelected");

                    Object call = param.args[0];
                    Object account = param.args[1];

                    Object mCalls = XposedHelpers.getObjectField(param.thisObject, "mCalls");
                    if (!(boolean)XposedHelpers.callMethod(mCalls, "contains", call)) {
                        //Log.i(this, "Attempted to add account to unknown call %s", call);
                    } else {
                        //Log.i(this, "phoneAccountSelected , id = %s", account.getId());
                        XposedHelpers.callMethod(param.thisObject, "updateLchStatus", XposedHelpers.callMethod(account, "getId"));
                        XposedHelpers.callMethod(call, "setTargetPhoneAccount", account);
                        if ((boolean)XposedHelpers.callMethod(param.thisObject, "makeRoomForOutgoingCall", call, false)) {
                            XposedHelpers.callMethod(call, "startCreateConnection", XposedHelpers.getObjectField(param.thisObject, "mPhoneAccountRegistrar"));
                        } else {
                            XposedHelpers.callMethod(call, "disconnect");
                        }
                    }
                    param.setResult(null);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
