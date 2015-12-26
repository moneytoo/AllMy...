package com.smartmadsoft.xposed.aio.tweaks.onehandzoomenabler;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FF {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.hookAllConstructors(XposedHelpers.findClass("org.mozilla.gecko.gfx.JavaPanZoomController", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.args[1];
                Common.mContext = view.getContext();
                Common.thisObject = param.thisObject;

                Common.classSimpleScaleGestureDetector = XposedHelpers.findClass("org.mozilla.gecko.gfx.SimpleScaleGestureDetector", lpparam.classLoader);

                Common.enumPanZoomState = (Class<? extends Enum>) XposedHelpers.findClass("org.mozilla.gecko.gfx.JavaPanZoomController$PanZoomState", lpparam.classLoader);
                Common.enumFullScreenState = (Class<? extends Enum>) XposedHelpers.findClass("org.mozilla.gecko.gfx.FullScreenState", lpparam.classLoader);
                Common.classGeckoAppShell = XposedHelpers.findClass("org.mozilla.gecko.GeckoAppShell", lpparam.classLoader);
                Common.classGeckoEvent = XposedHelpers.findClass("org.mozilla.gecko.GeckoEvent", lpparam.classLoader);
                Common.classFloatUtils = XposedHelpers.findClass("org.mozilla.gecko.util.FloatUtils", lpparam.classLoader);

                Common.mScaleDetector = new ScaleGestureDetector(Common.mContext, new Common.ScaleListenerFF());
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("org.mozilla.gecko.gfx.JavaPanZoomController", lpparam.classLoader), "onTouchEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                MotionEvent motionEvent = (MotionEvent) param.args[0];

                if (Common.mScaleDetector != null)
                    Common.mScaleDetector.onTouchEvent(motionEvent);

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (Common.executeDoubleTap) {
                        Common.executeDoubleTap = false;
                        XposedHelpers.callMethod(Common.thisObject, "sendPointToGecko", "Gesture:DoubleTap", Common.doubleTapMotionEvent);
                    }
                }
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("org.mozilla.gecko.gfx.JavaPanZoomController", lpparam.classLoader), "onDoubleTap", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Common.isInScale) {
                    Common.doubleTapMotionEvent = (MotionEvent) param.args[0];
                    Common.executeDoubleTap = true;
                }
                param.setResult(true);
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("org.mozilla.gecko.gfx.JavaPanZoomController", lpparam.classLoader), "onLongPress", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Common.isInScale)
                    param.setResult(null);
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("org.mozilla.gecko.gfx.SimpleScaleGestureDetector", lpparam.classLoader), "sendScaleGesture", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });
    }
}
