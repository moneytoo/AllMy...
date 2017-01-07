package com.smartmadsoft.xposed.aio.tweaks;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NOgBoARd {
    public static boolean isPassword = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.google.android.apps.inputmethod.libs.framework.core.GoogleInputMethodService", lpparam.classLoader), "setKeyboardViewShown", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!isPassword && (int) XposedHelpers.callMethod(param.args[0], "ordinal") == 0) // KeyboardViewDef$Type.HEADER
                        param.args[1] = false;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }

        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.google.android.apps.inputmethod.libs.framework.core.GoogleInputMethodService", lpparam.classLoader), "getEditorInfo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    isPassword = isInputPassword(((EditorInfo) param.getResult()).inputType);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private static boolean isInputPassword(int inputType) {
        int i = inputType & InputType.TYPE_MASK_VARIATION;
        if ((inputType & InputType.TYPE_MASK_CLASS) == 0)
            return false;
        if (i == InputType.TYPE_TEXT_VARIATION_PASSWORD)
            return true;
        if (i == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            return true;
        if (i != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
            return false;
        return true;
    }
}
