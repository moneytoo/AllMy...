package com.smartmadsoft.xposed.aio.tweaks.onehandzoomenabler;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AR {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookConstructor("com.adobe.reader.viewer.ARPageView", lpparam.classLoader, Context.class, AttributeSet.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // I have no idea why it's needed as it's fine on other devices
                if (Build.MANUFACTURER.toLowerCase().contains("lenovo") && Build.VERSION.SDK_INT == 21)
                    Common.ignoreFirstScaleWorkaround = true;
                Common.mContext = (Context) param.args[0];
                Common.thisObject = param.thisObject;
                Common.mCurrTouchXform = XposedHelpers.getObjectField(param.thisObject, "mCurrTouchXform");
                Common.mScaleDetector = new ScaleGestureDetector(Common.mContext, new Common.ScaleListener());
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.adobe.reader.viewer.ARPageView", lpparam.classLoader), "onMultiTouchDragOrPinch", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.isInScale)
                    param.setResult(null);
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.adobe.reader.viewer.ARPageView", lpparam.classLoader), "onTouchEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                MotionEvent motionEvent = (MotionEvent) param.args[0];

                if (Common.mScaleDetector != null)
                    Common.mScaleDetector.onTouchEvent(motionEvent);

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (Common.executeDoubleTap)
                        XposedHelpers.callMethod(param.thisObject, "handleDoubleTap", Common.doubleTapParam1, Common.doubleTapParam2);
                }
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.adobe.reader.viewer.ARPageView", lpparam.classLoader), "handleDoubleTap", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.executeDoubleTap) {
                    Common.executeDoubleTap = false;
                } else {
                    Common.executeDoubleTap = true;
                    Common.doubleTapParam1 = (Double) param.args[0];
                    Common.doubleTapParam2 = (Double) param.args[1];
                    param.setResult(null);
                }
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.adobe.reader.viewer.ARPageViewGestureHandler", lpparam.classLoader), "handleLongPress", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (Common.isInScale)
                    param.setResult(null);
            }
        });
    }
}
