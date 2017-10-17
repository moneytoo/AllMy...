package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class S5TouchWizJunk {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            if (Build.DEVICE.startsWith("klte")) {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.sec.android.app.popupuireceiver.PopupuiReceiver", lpparam.classLoader), "showBatteryCoverPopup", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

                if (Build.VERSION.SDK_INT < 23) {
                    XposedHelpers.findAndHookMethod("com.sec.android.app.popupuireceiver.PopupuiService", lpparam.classLoader, "showUSBCDetacheddDialog", Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            param.setResult(null);
                        }
                    });
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookUI(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "showHideQConnectLayout", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
        } catch (Throwable t) {}

        try {
            XposedHelpers.findAndHookMethod("com.android.systemui.qs.tiles.AirplaneModeTile", lpparam.classLoader, "showConfirmPopup", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(param.thisObject, "setEnabled", param.args[0]);
                    param.setResult(null);
                }
            });
        } catch (Throwable t) {}

        try {
            // Disable "Search phone and scan for nearby devices"
            XposedHelpers.findAndHookMethod("com.android.systemui.qs.QSSFinderView", lpparam.classLoader, "getBarVisibility", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(false);
                }
            });
        } catch (Throwable t) {}
    }

    public static void hookUIDND(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // disable priority mode notification
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, "updateVolumeZen", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookSettings(final XC_LoadPackage.LoadPackageParam lpparam) {
        String secPkg = "";
        if (Build.VERSION.SDK_INT >= 24)
            secPkg = ".samsung";

        try {
            XposedHelpers.findAndHookMethod("com" + secPkg + ".android.settings.bluetooth.BluetoothScanDialog", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    ((Activity)param.thisObject).finish();
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }

        try {
            XposedHelpers.findAndHookMethod("com" + secPkg + ".android.settings.wifi.WifiPickerDialog", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    ((Activity)param.thisObject).finish();
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void hookThemes(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            if (!Build.DEVICE.startsWith("klte")) {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.samsung.android.thememanager.ThemeManager", lpparam.classLoader), "startTimerForTrial", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
