package com.smartmadsoft.xposed.aio.tweaks;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NOgBoARd {
    private static int hideType = 0;

    private static boolean isPassword = false;
    private static boolean flagNoSuggestions = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam, int value) {
        hideType = value;

        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.google.android.apps.inputmethod.libs.framework.core.GoogleInputMethodService", lpparam.classLoader), "setKeyboardViewShown", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (((hideType == 5 && flagNoSuggestions) || (hideType >= 10)) && !isPassword && (int) XposedHelpers.callMethod(param.args[0], "ordinal") == 0) // KeyboardViewDef$Type.HEADER
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
                    int inputType = ((EditorInfo) param.getResult()).inputType;
                    isPassword = isInputPassword(inputType);
                    flagNoSuggestions = 0 != (inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
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

    // from https://github.com/Clam-/TraditionalT9/blob/master/src/org/nyanya/android/traditionalt9/Utils.java
    /*
    private static void printFlags(int inputType) {
        if ((inputType & InputType.TYPE_CLASS_DATETIME) == InputType.TYPE_CLASS_DATETIME)
            XposedBridge.log("AIO: TYPE_CLASS_DATETIME");
        if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER)
            XposedBridge.log("AIO: TYPE_CLASS_NUMBER");
        if ((inputType & InputType.TYPE_CLASS_PHONE) == InputType.TYPE_CLASS_PHONE)
            XposedBridge.log("AIO: TYPE_CLASS_PHONE");
        if ((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT)
            XposedBridge.log("AIO: TYPE_CLASS_TEXT");
        if ((inputType & InputType.TYPE_DATETIME_VARIATION_DATE) == InputType.TYPE_DATETIME_VARIATION_DATE)
            XposedBridge.log("AIO: TYPE_DATETIME_VARIATION_DATE");
        if ((inputType & InputType.TYPE_DATETIME_VARIATION_NORMAL) == InputType.TYPE_DATETIME_VARIATION_NORMAL)
            XposedBridge.log("AIO: TYPE_DATETIME_VARIATION_NORMAL");
        if ((inputType & InputType.TYPE_DATETIME_VARIATION_TIME) == InputType.TYPE_DATETIME_VARIATION_TIME)
            XposedBridge.log("AIO: YPE_DATETIME_VARIATION_TIME");
        if ((inputType & InputType.TYPE_NULL) == InputType.TYPE_NULL)
            XposedBridge.log("AIO: TYPE_NULL");
        if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL)
            XposedBridge.log("AIO: TYPE_NUMBER_FLAG_DECIMAL");
        if ((inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) == InputType.TYPE_NUMBER_FLAG_SIGNED)
            XposedBridge.log("AIO: TYPE_NUMBER_FLAG_SIGNED");
        if ((inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) == InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_AUTO_COMPLETE");
        if ((inputType & InputType.TYPE_TEXT_FLAG_AUTO_CORRECT) == InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_AUTO_CORRECT");
        if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) == InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_CAP_CHARACTERS");
        if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) == InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_CAP_SENTENCES");
        if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_WORDS) == InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_CAP_WORDS");
        if ((inputType & InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_IME_MULTI_LINE");
        if ((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_MULTI_LINE)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_MULTI_LINE");
        if ((inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            XposedBridge.log("AIO: TYPE_TEXT_FLAG_NO_SUGGESTIONS");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_EMAIL_ADDRESS");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT) == InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_EMAIL_SUBJECT");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_FILTER) == InputType.TYPE_TEXT_VARIATION_FILTER)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_FILTER");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE) == InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_LONG_MESSAGE");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_NORMAL) == InputType.TYPE_TEXT_VARIATION_NORMAL)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_NORMAL");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_PASSWORD");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_PERSON_NAME) == InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_PERSON_NAME");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_PHONETIC) == InputType.TYPE_TEXT_VARIATION_PHONETIC)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_PHONETIC");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS) == InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_POSTAL_ADDRESS");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE) == InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_SHORT_MESSAGE");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_URI) == InputType.TYPE_TEXT_VARIATION_URI)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_URI");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_VISIBLE_PASSWORD");
        if ((inputType & InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT) == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT)
            XposedBridge.log("AIO: TYPE_TEXT_VARIATION_WEB_EDIT_TEXT");
        XposedBridge.log("AIO: =============================================");
    }
    */
}
