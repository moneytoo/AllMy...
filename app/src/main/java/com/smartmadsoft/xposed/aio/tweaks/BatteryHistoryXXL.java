package com.smartmadsoft.xposed.aio.tweaks;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class BatteryHistoryXXL {
    static final int MIN_POWER_THRESHOLD_MILLI_AMP = 5;
    static final int MAX_ITEMS_TO_LIST = 30;
    static final int MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP = 10;
    static final int SECONDS_IN_HOUR = 60 * 60;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        @SuppressWarnings("unused")
        final Class<?> powerUsageSummary = XposedHelpers.findClass("com.android.settings.fuelgauge.PowerUsageSummary", lpparam.classLoader);
        final Class<?> batteryHistoryPreference = XposedHelpers.findClass("com.android.settings.fuelgauge.BatteryHistoryPreference", lpparam.classLoader);
        final Class<?> powerGaugePreference = XposedHelpers.findClass("com.android.settings.fuelgauge.PowerGaugePreference", lpparam.classLoader);

        findAndHookMethod("com.android.settings.fuelgauge.PowerUsageSummary", lpparam.classLoader, "refreshStats", new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {

                if (Build.VERSION.SDK_INT >= 21) {
                    Class<?> UserHandleClass = XposedHelpers.findClass("android.os.UserHandle", lpparam.classLoader);
                    Class<?> BatteryEntryClass = XposedHelpers.findClass("com.android.settings.fuelgauge.BatteryEntry", lpparam.classLoader);

                    Object mStatsHelper = XposedHelpers.getObjectField(param.thisObject, "mStatsHelper");
                    final Object stats = XposedHelpers.callMethod(mStatsHelper, "getStats");

                    // CM 12.1 since https://github.com/CyanogenMod/android_packages_apps_Settings/commit/2709976f77c513f90f5e55272afe6b4389c632d7
                    Object dockStats = null;
                    boolean isCM121new = false;

                    try {
                        dockStats = XposedHelpers.callMethod(mStatsHelper, "getDockStats");
                        isCM121new = true;
                    } catch (NoSuchMethodError e) {
                    }

                    PreferenceGroup mAppListGroup = (PreferenceGroup) XposedHelpers.getObjectField(param.thisObject, "mAppListGroup");
                    mAppListGroup.removeAll();
                    mAppListGroup.setOrderingAsAdded(false);

                    Object mHistPref;
                    if (isCM121new)
                        mHistPref = XposedHelpers.newInstance(batteryHistoryPreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), stats, dockStats, XposedHelpers.callMethod(mStatsHelper, "getBatteryBroadcast"));
                    else
                        mHistPref = XposedHelpers.newInstance(batteryHistoryPreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), stats, XposedHelpers.callMethod(mStatsHelper, "getBatteryBroadcast"));
                    XposedHelpers.callMethod(mHistPref, "setOrder", -1);
                    mAppListGroup.addPreference((Preference) mHistPref);
                    boolean addedSome = false;

                    //final Object powerProfile = XposedHelpers.callMethod(mStatsHelper, "getPowerProfile");
                    //final Object stats = XposedHelpers.callMethod(mStatsHelper, "getStats");

                    //final double averagePower = (Double) XposedHelpers.callMethod(powerProfile, "getAveragePower", "screen.full");
                    //if (averagePower >= MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP) {
                    {
                        Object mUm = XposedHelpers.getObjectField(param.thisObject, "mUm");
                        final List<?> profiles = (List<?>) XposedHelpers.callMethod(mUm, "getUserProfiles");

                        XposedHelpers.callMethod(mStatsHelper, "refreshStats", 0, profiles);
                        final List<?> usageList = (List<?>) XposedHelpers.callMethod(mStatsHelper, "getUsageList");

                        int mStatsType = 0;
                        final int dischargeAmount = (Integer) (stats != null ? XposedHelpers.callMethod(stats, "getDischargeAmount", mStatsType) : 0);
                        final int numSippers = usageList.size();
                        for (int i = 0; i < numSippers; i++) {
                            final Object sipper = usageList.get(i);
			                /*
			                if (((Double) XposedHelpers.getObjectField(sipper, "value") * SECONDS_IN_HOUR) < MIN_POWER_THRESHOLD_MILLI_AMP) {
			                    continue;
			                }
			                */
                            final double percentOfTotal = (((Double) XposedHelpers.getObjectField(sipper, "value") / (Double) XposedHelpers.callMethod(mStatsHelper, "getTotalPower")) * dischargeAmount);
			                /*
			                if (((int) (percentOfTotal + .5)) < 1) {
			                    continue;
			                }
			                Enum drainType = (Enum) XposedHelpers.getObjectField(sipper, "drainType");
			                //if (sipper.drainType == BatterySipper.DrainType.OVERCOUNTED) {
			                if (drainType.name().equals("OVERCOUNTED")) {
			                    // Don't show over-counted unless it is at least 2/3 the size of
			                    // the largest real entry, and its percent of total is more significant
			                    if ((Double) XposedHelpers.getObjectField(sipper, "value") < (((Double) XposedHelpers.callMethod(mStatsHelper, "getMaxRealPower")*2)/3)) {
			                        continue;
			                    }
			                    if (percentOfTotal < 10) {
			                        continue;
			                    }
			                    if ("user".equals(Build.TYPE) || "userdebug".equals(Build.TYPE)) {
			                        continue;
			                    }
			                }
			                if (drainType.name().equals("UNACCOUNTED")) {
			                //if (sipper.drainType == BatterySipper.DrainType.UNACCOUNTED) {
			                    // Don't show over-counted unless it is at least 1/2 the size of
			                    // the largest real entry, and its percent of total is more significant
			                    if ((Double) XposedHelpers.getObjectField(sipper, "value") < ((Double) XposedHelpers.callMethod(mStatsHelper, "getMaxRealPower")/2)) {
			                        continue;
			                    }
			                    if (percentOfTotal < 5) {
			                        continue;
			                    }
			                    if ("user".equals(Build.TYPE) || "userdebug".equals(Build.TYPE)) {
			                        continue;
			                    }
			                }
			                */
                            final Object userHandle = XposedHelpers.newInstance(UserHandleClass, XposedHelpers.callStaticMethod(UserHandleClass, "getUserId", XposedHelpers.callMethod(sipper, "getUid")));
                            final Object entry = XposedHelpers.newInstance(BatteryEntryClass, XposedHelpers.callMethod(param.thisObject, "getActivity"), XposedHelpers.getObjectField(param.thisObject, "mHandler"), mUm, sipper);
                            final Drawable badgedIcon = (Drawable) XposedHelpers.callMethod(mUm, "getBadgedIconForUser", XposedHelpers.callMethod(entry, "getIcon"), userHandle);
                            final CharSequence contentDescription = (CharSequence) XposedHelpers.callMethod(mUm, "getBadgedLabelForUser", XposedHelpers.callMethod(entry, "getLabel"), userHandle);
                            final Object pref = XposedHelpers.newInstance(powerGaugePreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), badgedIcon, contentDescription, entry);

                            final double percentOfMax = ((Double) XposedHelpers.getObjectField(sipper, "value") * 100) / (Double) XposedHelpers.callMethod(mStatsHelper, "getMaxPower");
                            XposedHelpers.setObjectField(sipper, "percent", percentOfTotal);
                            XposedHelpers.callMethod(pref, "setTitle", XposedHelpers.callMethod(entry, "getLabel"));
                            XposedHelpers.callMethod(pref, "setOrder", i + 1);
                            XposedHelpers.callMethod(pref, "setPercent", percentOfMax, percentOfTotal);
                            if (XposedHelpers.getObjectField(sipper, "uidObj") != null) {
                                XposedHelpers.callMethod(pref, "setKey", Integer.toString( (Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(sipper, "uidObj"), "getUid") ));
                            }
                            addedSome = true;
                            XposedHelpers.callMethod(mAppListGroup, "addPreference", pref);
                            if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST + 1)) {
                                break;
                            }
                        }
                    }
                    if (!addedSome) {
                        XposedHelpers.callMethod(param.thisObject, "addNotAvailableMessage");
                    }

                    XposedHelpers.callStaticMethod(BatteryEntryClass, "startRequestQueue");
                } else {
		            /*
					try {
						// TW 4.3
						if (XposedHelpers.getObjectField(param.thisObject, "mStats") == null)
							XposedHelpers.callMethod(param.thisObject, "load");
					} catch (Throwable t) {}
					*/

                    PreferenceGroup mAppListGroup = (PreferenceGroup) XposedHelpers.getObjectField(param.thisObject, "mAppListGroup");
                    mAppListGroup.removeAll();
                    mAppListGroup.setOrderingAsAdded(false);

                    try {
                        // TW
                        CheckBoxPreference mDisplayBatteryLevel = (CheckBoxPreference) XposedHelpers.getObjectField(param.thisObject, "mDisplayBatteryLevel");
                        mAppListGroup.addPreference(mDisplayBatteryLevel);
                        mDisplayBatteryLevel.setOrder(-3);
                    } catch (NoSuchFieldError e) {}

                    Preference mBatteryStatusPref = (Preference) XposedHelpers.getObjectField(param.thisObject, "mBatteryStatusPref");
                    mBatteryStatusPref.setOrder(-2);
                    mAppListGroup.addPreference(mBatteryStatusPref);

                    Object mStatsHelper = XposedHelpers.getObjectField(param.thisObject, "mStatsHelper");

                    Object hist;
                    try {
                        // AOSP, TW
                        hist = XposedHelpers.newInstance(batteryHistoryPreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), XposedHelpers.callMethod(mStatsHelper, "getStats"));
                    } catch (NoSuchMethodError e) {
                        // CM
                        hist = XposedHelpers.newInstance(batteryHistoryPreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), XposedHelpers.callMethod(mStatsHelper, "getStats"), XposedHelpers.callMethod(mStatsHelper, "getDockStats", XposedHelpers.callMethod(param.thisObject, "getActivity")));
                    }

                    XposedHelpers.callMethod(hist, "setOrder", -1);
                    XposedHelpers.callMethod(mAppListGroup, "addPreference", hist);

                    if ((Double) XposedHelpers.callMethod(XposedHelpers.callMethod(mStatsHelper, "getPowerProfile"), "getAveragePower", "screen.full") < 10.0) {
                        XposedHelpers.callMethod(param.thisObject, "addNotAvailableMessage");
                        return null;
                    }

                    try {
                        // AOSP, TW
                        XposedHelpers.callMethod(mStatsHelper, "refreshStats", false);
                    } catch (NoSuchMethodError e) {
                        // CM
                        XposedHelpers.callMethod(mStatsHelper, "refreshStats", XposedHelpers.callMethod(param.thisObject, "getActivity"), false);
                    }

                    @SuppressWarnings("unchecked")
                    List<Object> usageList = (List<Object>) XposedHelpers.callMethod(mStatsHelper, "getUsageList");

                    for (Object sipper : usageList) {
                        //if ((Double) XposedHelpers.callMethod(sipper, "getSortValue") < 5.0)
                        //continue;
                        final double percentOfTotal = ((Double) XposedHelpers.callMethod(sipper, "getSortValue") / (Double) XposedHelpers.callMethod(mStatsHelper, "getTotalPower") * 100);
                        //if (percentOfTotal < 1)
                        //continue;

                        Object pref = XposedHelpers.newInstance(powerGaugePreference, XposedHelpers.callMethod(param.thisObject, "getActivity"), XposedHelpers.callMethod(sipper, "getIcon"), sipper);

                        final double percentOfMax = ((Double) XposedHelpers.callMethod(sipper, "getSortValue") * 100) / (Double) XposedHelpers.callMethod(mStatsHelper, "getMaxPower");
                        XposedHelpers.setObjectField(sipper, "percent", percentOfTotal);

                        XposedHelpers.callMethod(pref, "setTitle", XposedHelpers.getObjectField(sipper, "name"));
                        XposedHelpers.callMethod(pref, "setOrder", (int) (Integer.MAX_VALUE - (Double) XposedHelpers.callMethod(sipper, "getSortValue")));
                        XposedHelpers.callMethod(pref, "setPercent", percentOfMax, percentOfTotal);


                        if (XposedHelpers.getObjectField(sipper, "uidObj") != null) {
                            XposedHelpers.callMethod(pref, "setKey", Integer.toString( (Integer) XposedHelpers.callMethod(XposedHelpers.getObjectField(sipper, "uidObj"), "getUid") ));
                        }

                        XposedHelpers.callMethod(mAppListGroup, "addPreference", pref);

                        if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST+1))
                            break;
                    }
                }

                return null;
            }
        });
    }
}
