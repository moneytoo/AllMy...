package com.smartmadsoft.xposed.aio.tweaks;

import android.content.ComponentName;
import android.os.Build;

import java.util.LinkedHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PocketFirst {
    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.internal.app.ResolverComparator", lpparam.classLoader), "compute", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean modded = false;
                        @SuppressWarnings("unchecked")
                        LinkedHashMap<ComponentName, Object> map = (LinkedHashMap<ComponentName, Object>) XposedHelpers.getObjectField(param.thisObject, "mScoredTargets");
                        for (ComponentName key : map.keySet()) {
                            if (key.getPackageName().equals("com.ideashower.readitlater.pro")) {
                                Object target = map.get(key);
                                XposedHelpers.setObjectField(target, "score", Float.MAX_VALUE);
                                map.put(key, target);
                                modded = true;
                                break;
                            }
                        }
                        if (modded)
                            XposedHelpers.setObjectField(param.thisObject, "mScoredTargets", map);
                    }
                });
            } else {
                XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity.ResolverComparator", lpparam.classLoader, "getPackageTimeSpent", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0].equals("com.ideashower.readitlater.pro"))
                            param.setResult(Long.MAX_VALUE);
                    }
                });
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }
}
