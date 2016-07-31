package com.smartmadsoft.xposed.aio.tweaks;

import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class VolumeKeysCursorControl {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    KeyEvent event = (KeyEvent) param.args[1];
                    int keyCode = event.getKeyCode();

                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        boolean isInputViewShown = (boolean) XposedHelpers.callMethod(param.thisObject, "isInputViewShown");
                        if (isInputViewShown) {
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                                XposedHelpers.callMethod(param.thisObject, "sendDownUpKeyEvents", KeyEvent.KEYCODE_DPAD_LEFT);
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                                XposedHelpers.callMethod(param.thisObject, "sendDownUpKeyEvents", KeyEvent.KEYCODE_DPAD_RIGHT);
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
}
