package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.UserHandle;

import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class S7MTPWithoutUnlocking {
    private static String postfix = "";

    @TargetApi(22)
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.samsung.android.MtpApplication.MtpReceiver$2", lpparam.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    Message m = (Message) param.args[0];
                    // opening mtp session
                    if (m.what == 14) {
                        Context mContext = (Context) XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.samsung.android.MtpApplication.MtpReceiver", lpparam.classLoader), "mContext");
                        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);

                        if (!keyguardManager.isDeviceLocked()) {
                            Intent intent = new Intent("android.intent.action.USER_PRESENT." + postfix);
                            UserHandle userHandle = (UserHandle) XposedHelpers.getStaticObjectField(UserHandle.class, "ALL");
                            XposedHelpers.callMethod(mContext, "sendBroadcastAsUser", intent, userHandle);
                            /*
                            // shorter but potentially dangerously messing with other receivers
                            final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x08000000;
                            Intent USER_PRESENT_INTENT = new Intent(Intent.ACTION_USER_PRESENT).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING | FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
                            XposedHelpers.callMethod(mContext, "sendBroadcastAsUser", USER_PRESENT_INTENT, userHandle);
                            */
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod("com.samsung.android.MtpApplication.MtpService$4", lpparam.classLoader, "onReceive", Context.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    Intent intent = (Intent) param.args[1];
                    //XposedBridge.log("AIO: mtp onReceive action=" + intent.getAction());
                    if (intent.getAction().equals("android.intent.action.USER_PRESENT." + postfix))
                        param.args[1] = new Intent("android.intent.action.USER_PRESENT");
                }
            });

            XposedHelpers.findAndHookMethod("com.samsung.android.MtpApplication.MtpService", lpparam.classLoader, "registerBroadCastuserPresentRec", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    //XposedBridge.log("AIO: mtp registerBroadCastuserPresentRec");
                    postfix = UUID.randomUUID().toString();

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.intent.action.USER_PRESENT");
                    intentFilter.addAction("android.intent.action.USER_PRESENT." + postfix);
                    BroadcastReceiver mtpUserPresentReceiver = (BroadcastReceiver) XposedHelpers.getObjectField(param.thisObject, "mtpUserPresentReceiver");
                    UserHandle userHandle = (UserHandle) XposedHelpers.getStaticObjectField(UserHandle.class, "ALL");
                    XposedHelpers.callMethod(param.thisObject, "registerReceiverAsUser", mtpUserPresentReceiver, userHandle, intentFilter, null, null);
                    XposedHelpers.setObjectField(param.thisObject, "userPresentRegistered", true);
                    param.setResult(null);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
