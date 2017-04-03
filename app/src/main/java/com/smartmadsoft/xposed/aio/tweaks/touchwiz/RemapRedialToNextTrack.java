package com.smartmadsoft.xposed.aio.tweaks.touchwiz;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RemapRedialToNextTrack {

    @TargetApi(19)
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("com.android.bluetooth.hfp.HeadsetStateMachine", lpparam.classLoader, "processDialCall", String.class, BluetoothDevice.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(param.thisObject, "atResponseCodeNative", 0, 0, XposedHelpers.callMethod(param.thisObject, "getByteAddress", param.args[1]));

                    AudioManager mAudioManager = (AudioManager) XposedHelpers.getObjectField(param.thisObject, "mAudioManager");

                    long eventtime = SystemClock.uptimeMillis();
                    Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                    keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                    mAudioManager.dispatchMediaKeyEvent(keyEvent);

                    keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
                    keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                    mAudioManager.dispatchMediaKeyEvent(keyEvent);

                    param.setResult(null);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
