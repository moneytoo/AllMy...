package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class S7sRGBVideo {
    public static boolean wasSetBasic = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.samsung.android.mdnie.AdaptiveDisplayColorService", lpparam.classLoader, "monitorForegroundBrowser", String.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    String pkg = (String) param.args[0];
                    //XposedBridge.log("AIO: monitorForegroundBrowser: " + pkg);

                    if (pkg.startsWith("com.mxtech.videoplayer") || pkg.startsWith("com.google.android.youtube") || pkg.startsWith("com.google.android.videos")) {
                        // Display modes:
                        // 0 = AMOLED cinema
                        // 1 = AMOLED photo
                        // 2 = Basic
                        // 4 = Adaptive display

                        XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.sec.android.hardware.SecHardwareInterface", lpparam.classLoader), "setmDNIeUserMode", 2);
                        wasSetBasic = true;
                    } else if (wasSetBasic) {
                        XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.sec.android.hardware.SecHardwareInterface", lpparam.classLoader), "setmDNIeUserMode", 4);
                        wasSetBasic = false;
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
