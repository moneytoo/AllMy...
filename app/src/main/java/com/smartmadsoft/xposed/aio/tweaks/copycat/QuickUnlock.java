/*
    from https://github.com/GravityBox/GravityBox
 */

package com.smartmadsoft.xposed.aio.tweaks.copycat;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QuickUnlock {

    private static Object mMonitor;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.statusbar.policy.KeyguardMonitor", lpparam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        mMonitor = param.thisObject;
                    }
                });
            }

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.keyguard.KeyguardPasswordView", lpparam.classLoader), "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    final TextView passwordEntry = (TextView) XposedHelpers.getObjectField(param.thisObject, "mPasswordEntry");
                    if (passwordEntry == null) return;

                    passwordEntry.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            doQuickUnlock(param.thisObject, passwordEntry.getText().toString());
                        }
                        @Override
                        public void beforeTextChanged(CharSequence arg0,int arg1, int arg2, int arg3) { }
                        @Override
                        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
                    });
                }
            });

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.keyguard.KeyguardPINView", lpparam.classLoader), "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    final View passwordEntry = (View) XposedHelpers.getObjectField(param.thisObject, "mPasswordEntry");
                    if (passwordEntry != null) {
                        XposedHelpers.setAdditionalInstanceField(passwordEntry, "gbPINView", param.thisObject);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("com.android.keyguard.PasswordTextView", lpparam.classLoader), "append", char.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    Object pinView = XposedHelpers.getAdditionalInstanceField(param.thisObject, "gbPINView");
                    if (pinView != null) {
                        String entry = (String) XposedHelpers.getObjectField(param.thisObject, "mText");
                        doQuickUnlock(pinView, entry);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private static void doQuickUnlockMM(final Object securityView, final String entry) {
        if (entry.length() != 4) return;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Object lockPatternUtils = XposedHelpers.getObjectField(securityView, "mLockPatternUtils");
                    final Object lockSettings = XposedHelpers.callMethod(lockPatternUtils, "getLockSettings");
                    //final int userId = mKgMonitor.getCurrentUserId();
                    final int userId = XposedHelpers.getIntField(mMonitor, "mCurrentUser");

                    Object response;
                    if (Build.VERSION.SDK_INT >= 25)
                        response = XposedHelpers.callMethod(lockSettings, "checkPassword", entry, userId, null);
                    else
                        response = XposedHelpers.callMethod(lockSettings, "checkPassword", entry, userId);

                    final int code = (int)XposedHelpers.callMethod(response, "getResponseCode");
                    if (code == 0) {
                        final Object callback = XposedHelpers.getObjectField(securityView, "mCallback");
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (Build.VERSION.SDK_INT >= 24)
                                        XposedHelpers.callMethod(callback, "reportUnlockAttempt", userId, true, 0);
                                    else
                                        XposedHelpers.callMethod(callback, "reportUnlockAttempt", true, userId);
                                    XposedHelpers.callMethod(callback, "dismiss", true);
                                } catch (Throwable t) {
                                    //log("Error dimissing keyguard: " + t.getMessage());
                                }
                            }
                        });
                    }
                } catch (Throwable t) {
                    XposedBridge.log(t);;
                }
            }
        });
    }

    private static void doQuickUnlockLP(Object securityView, String entry) {
        try {
            final Object callback = XposedHelpers.getObjectField(securityView, "mCallback");
            final Object lockPatternUtils = XposedHelpers.getObjectField(securityView, "mLockPatternUtils");

            if (callback != null && lockPatternUtils != null && entry.length() > 3 && (Boolean) XposedHelpers.callMethod(lockPatternUtils, "checkPassword", entry)) {
                XposedHelpers.callMethod(callback, "reportUnlockAttempt", true);
                XposedHelpers.callMethod(callback, "dismiss", true);
            }
        } catch (Throwable t) {
            XposedBridge.log(t);;
        }
    }

    private static void doQuickUnlock(Object securityView, String entry) {
        if (Build.VERSION.SDK_INT >= 23)
            doQuickUnlockMM(securityView, entry);
        else
            doQuickUnlockLP(securityView, entry);
    }
}
