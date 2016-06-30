package com.smartmadsoft.xposed.aio.tweaks.onehandzoomenabler;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import de.robv.android.xposed.XposedHelpers;

public class Common {
    public static ScaleGestureDetector mScaleDetector;
    private static float mScaleFactor = 1.f;
    public static boolean isInScale = false;
    public static boolean executeDoubleTap = false;
    public static double doubleTapParam1, doubleTapParam2;
    public static boolean ignoreFirstScaleWorkaround = false;

    public static MotionEvent doubleTapMotionEvent;

    public static Context mContext;
    public static Object mCurrTouchXform;
    public static Object thisObject;

    public static Class classSimpleScaleGestureDetector;

    public static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
            isInScale = true;
            executeDoubleTap = false;
            mScaleFactor = 1.f;
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isInScale = false;
            ignoreFirstScaleWorkaround = false;
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (ignoreFirstScaleWorkaround)
                return true;

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            Object mCurrTouchPoint = XposedHelpers.getObjectField(thisObject, "mCurrTouchPoint");
            float x = (Float) XposedHelpers.callMethod(mCurrTouchPoint, "getX") - detector.getFocusX()  * mScaleFactor;
            float y = (Float) XposedHelpers.callMethod(mCurrTouchPoint, "getY") - detector.getFocusY()  * mScaleFactor;

            XposedHelpers.callMethod(mCurrTouchXform, "set", x, y, mScaleFactor);
            XposedHelpers.callMethod(thisObject, "invalidate");

            return true;
        }
    }

    public static Class<? extends Enum> enumPanZoomState;
    public static Class<? extends Enum> enumFullScreenState;
    public static Class classGeckoAppShell;
    public static Class classGeckoEvent;
    public static Class classFloatUtils;

    public static class ScaleListenerFF extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isInScale = true;
            executeDoubleTap = false;

            float zoomFactor = (Float) XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getMetrics"), "zoomFactor");
            mScaleFactor = zoomFactor;

            Object mState = XposedHelpers.getObjectField(thisObject, "mState");
            if (mState == Enum.valueOf(enumPanZoomState, "ANIMATED_ZOOM"))
                return false;

            //if (!mTarget.getZoomConstraints().getAllowZoom()) // different on old
            Object mTarget = XposedHelpers.getObjectField(thisObject, "mTarget");
            if (! (Boolean) XposedHelpers.getObjectField(XposedHelpers.callMethod(mTarget, "getZoomConstraints"), "mAllowZoom"))
                return false;

            XposedHelpers.callMethod(thisObject, "setState", Enum.valueOf(enumPanZoomState, "PINCHING"));
            XposedHelpers.setObjectField(thisObject, "mLastZoomFocus", new PointF(detector.getFocusX(), detector.getFocusY()));
            XposedHelpers.callMethod(thisObject, "cancelTouch");

            XposedHelpers.callStaticMethod(classGeckoAppShell, "sendEventToGecko",
                    XposedHelpers.callStaticMethod(classGeckoEvent, "createNativeGestureEvent",
                            XposedHelpers.getStaticObjectField(classGeckoEvent, "ACTION_MAGNIFY_START"),
                            XposedHelpers.getObjectField(thisObject, "mLastZoomFocus"),
                            XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getMetrics"), "zoomFactor")
                    )
            );
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isInScale = false;

            Object mState = XposedHelpers.getObjectField(thisObject, "mState");
            if (mState == Enum.valueOf(enumPanZoomState, "ANIMATED_ZOOM"))
                return;

            XposedHelpers.callMethod(thisObject, "startTouch", detector.getFocusX(), detector.getFocusY(), detector.getEventTime());
            XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mTarget"), "forceRedraw", (Object) null);

            PointF point = new PointF(detector.getFocusX(), detector.getFocusY());
            Object event = XposedHelpers.callStaticMethod(classGeckoEvent, "createNativeGestureEvent",
                    XposedHelpers.getStaticObjectField(classGeckoEvent, "ACTION_MAGNIFY_END"),
                    point,
                    XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getMetrics"), "zoomFactor"));

            if (event == null)
                return;

            XposedHelpers.callStaticMethod(classGeckoAppShell, "sendEventToGecko", event);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.72f, Math.min(mScaleFactor, 4.0f));

            if (mScaleFactor < 0.72f)
                return true;

            Object mTarget = XposedHelpers.getObjectField(thisObject, "mTarget");
            if (XposedHelpers.callMethod(mTarget, "getFullScreenState") != Enum.valueOf(enumFullScreenState, "NONE"))
                return false;

            if (XposedHelpers.getObjectField(thisObject, "mState") != Enum.valueOf(enumPanZoomState, "PINCHING"))
                return false;

            float prevSpan = detector.getPreviousSpan();
            if ((Boolean) XposedHelpers.callStaticMethod(classFloatUtils, "fuzzyEquals", prevSpan, 0.0f))
                return true;

            Object mLastZoomFocus;

            synchronized (XposedHelpers.callMethod(mTarget, "getLock")) {
                mLastZoomFocus = XposedHelpers.getObjectField(thisObject, "mLastZoomFocus");

                XposedHelpers.callMethod(thisObject, "scrollBy",
                        (Float) XposedHelpers.getObjectField(mLastZoomFocus, "x") - detector.getFocusX(),
                        (Float) XposedHelpers.getObjectField(mLastZoomFocus, "y") - detector.getFocusY());

                XposedHelpers.callMethod(mLastZoomFocus, "set", detector.getFocusX(), detector.getFocusY());
                Object target = XposedHelpers.callMethod(XposedHelpers.callMethod(thisObject, "getMetrics"), "scaleTo", mScaleFactor, mLastZoomFocus);

                /*if ((Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mX"), "getOverScrollMode") == View.OVER_SCROLL_NEVER ||
                        (Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mY"), "getOverScrollMode") == View.OVER_SCROLL_NEVER ) {
                    //target = XposedHelpers.callMethod(thisObject, "getValidViewportMetrics", target);
                }*/
                XposedHelpers.callMethod(mTarget, "setViewportMetrics", target);
            }

            Object event = XposedHelpers.callStaticMethod(classGeckoEvent, "createNativeGestureEvent",
                    XposedHelpers.getStaticObjectField(classGeckoEvent, "ACTION_MAGNIFY"),
                    mLastZoomFocus,
                    XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getMetrics"), "zoomFactor"));

            XposedHelpers.callStaticMethod(classGeckoAppShell, "sendEventToGecko", event);
            return true;
        }
    }
}
