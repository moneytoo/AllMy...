package com.smartmadsoft.xposed.aio.tweaks;

import android.content.Context;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.WindowManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class VolumeKeysCursorControl {
    private static boolean volUpLeft = true;
    private static boolean orientationAware = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam, int type) {
        if (type % 2 == 0)
            volUpLeft = false;
        if (type > 2)
            orientationAware = true;

        try {
            XposedHelpers.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    KeyEvent event = (KeyEvent) param.args[1];
                    int keyCode = event.getKeyCode();

                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        boolean isInputViewShown = (boolean) XposedHelpers.callMethod(param.thisObject, "isInputViewShown");
                        if (isInputViewShown) {
                            XposedHelpers.callMethod(param.thisObject, "sendDownUpKeyEvents", getKeyForVolume((keyCode == KeyEvent.KEYCODE_VOLUME_UP ? true : false), (Context) param.thisObject));
                            param.setResult(true);
                        } else
                            param.setResult(false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "onKeyUp", int.class, KeyEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    KeyEvent event = (KeyEvent) param.args[1];
                    int keyCode = event.getKeyCode();

                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        boolean isInputViewShown = (boolean) XposedHelpers.callMethod(param.thisObject, "isInputViewShown");
                        if (isInputViewShown) {
                            param.setResult(true);
                        } else
                            param.setResult(false);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private static int getKeyForVolume(boolean eventVolumeUp, Context context) {
        boolean volUpLeftNow = volUpLeft;

        if (orientationAware) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            int rotation = wm.getDefaultDisplay().getRotation();

            switch (rotation) {
                case Surface.ROTATION_90:
                    volUpLeftNow = true;
                    break;
                case Surface.ROTATION_180:
                    volUpLeftNow = !volUpLeftNow;
                    break;
                case Surface.ROTATION_270:
                    volUpLeftNow = false;
                    break;
            }
        }

        if (eventVolumeUp == volUpLeftNow)
            return KeyEvent.KEYCODE_DPAD_LEFT;
        else
            return KeyEvent.KEYCODE_DPAD_RIGHT;
    }
}
