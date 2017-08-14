package com.smartmadsoft.xposed.aio.tweaks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XResources;
import android.util.DisplayMetrics;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ChromeTabsToolbarOnPhone {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("org.chromium.content.browser.DeviceUtils", lpparam.classLoader, "addDeviceSpecificUserAgentSwitch", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object commandLineInstance = XposedHelpers.callStaticMethod(XposedHelpers.findClass("org.chromium.base.CommandLine", lpparam.classLoader), "getInstance");
                    XposedHelpers.callMethod(commandLineInstance, "appendSwitch", "use-mobile-user-agent");
                }
            });

            /*
            // https://codereview.chromium.org/1948143003
            // not enough, toolbar is all white now...
            final Class ClassToolbarLayout = XposedHelpers.findClass("org.chromium.chrome.browser.toolbar.ToolbarLayout", lpparam.classLoader);
            XposedBridge.hookAllMethods(XposedHelpers.findClass("org.chromium.chrome.browser.toolbar.ToolbarControlContainer", lpparam.classLoader), "getProgressBarDrawingInfo", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object mToolbar = XposedHelpers.getObjectField(param.thisObject, "mToolbar");
                    Object progressBar = XposedHelpers.callMethod(ClassToolbarLayout.cast(mToolbar), "getProgressBar");
                    if (progressBar == null)
                        param.setResult(null);
                }
            });
            */
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void init() {
        try {
            // from https://github.com/rovo89/XposedAppSettings
            XposedHelpers.findAndHookMethod(Resources.class, "updateConfiguration", Configuration.class, DisplayMetrics.class, "android.content.res.CompatibilityInfo", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0] == null)
                        return;

                    String packageName;
                    Resources res = ((Resources) param.thisObject);
                    if (res instanceof XResources) {
                        packageName = ((XResources) res).getPackageName();
                    } else {
                        try {
                            packageName = XResources.getPackageNameDuringConstruction();
                        } catch (IllegalStateException e) {
                            return;
                        }
                    }

                    if (isChrome(packageName) || isChrome(AndroidAppHelper.currentPackageName())) {
                        Configuration newConfig = new Configuration((Configuration) param.args[0]);

                        if (param.args[1] != null) {
                            DisplayMetrics newMetrics;
                            newMetrics = new DisplayMetrics();
                            newMetrics.setTo((DisplayMetrics) param.args[1]);
                            param.args[1] = newMetrics;
                        }

                        newConfig.smallestScreenWidthDp = 600;
                        newConfig.screenWidthDp = 600;
                        newConfig.screenHeightDp = 1024;

                        param.args[0] = newConfig;
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static boolean isChrome(String pkg) {
        if (pkg.equals("com.android.chrome") || pkg.equals("com.chrome.beta") || pkg.equals("com.chrome.dev") || pkg.equals("com.chrome.canary"))
            return true;
        return false;
    }
}
