package com.smartmadsoft.xposed.aio.tweaks;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoToastIcons {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.widget.Toast.TN", lpparam.classLoader), "trySendAccessibilityEvent", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    View mView = (View) XposedHelpers.getObjectField(param.thisObject, "mView");
                    RelativeLayout relativeLayout = (RelativeLayout) mView;
                    ImageView icon = (ImageView) relativeLayout.getChildAt(1);

                    WindowManager mWM = (WindowManager) XposedHelpers.getObjectField(param.thisObject, "mWM");
                    WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) XposedHelpers.getObjectField(param.thisObject, "mParams");

                    mWM.removeView(mView);
                    icon.setImageDrawable(null);
                    mWM.addView(mView, mParams);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
