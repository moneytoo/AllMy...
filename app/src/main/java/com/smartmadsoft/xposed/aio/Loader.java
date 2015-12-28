package com.smartmadsoft.xposed.aio;

import android.content.res.XResources;

import com.smartmadsoft.xposed.aio.tweaks.AlwaysSoftwareMenu;
import com.smartmadsoft.xposed.aio.tweaks.BatteryHistoryXXL;
import com.smartmadsoft.xposed.aio.tweaks.BatteryLightDisabler;
import com.smartmadsoft.xposed.aio.tweaks.CompactVolumePanel;
import com.smartmadsoft.xposed.aio.tweaks.DeskClockAlarm;
import com.smartmadsoft.xposed.aio.tweaks.DisableSuIndicator;
import com.smartmadsoft.xposed.aio.tweaks.GentleHapticFeedback;
import com.smartmadsoft.xposed.aio.tweaks.HideNetworkIndicators;
import com.smartmadsoft.xposed.aio.tweaks.K920KeysHapticFix;
import com.smartmadsoft.xposed.aio.tweaks.MinimumBrightnessMX;
import com.smartmadsoft.xposed.aio.tweaks.NativeFreezer;
import com.smartmadsoft.xposed.aio.tweaks.NoSafeVolumeWarning;
import com.smartmadsoft.xposed.aio.tweaks.NoToastIcons;
import com.smartmadsoft.xposed.aio.tweaks.OneWayBrightness;
import com.smartmadsoft.xposed.aio.tweaks.PocketFirst;
import com.smartmadsoft.xposed.aio.tweaks.onehandzoomenabler.AR;
import com.smartmadsoft.xposed.aio.tweaks.onehandzoomenabler.FF;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Loader implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    public static final String PACKAGE_NAME = Loader.class.getPackage().getName();
    private static XSharedPreferences prefs;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences(PACKAGE_NAME);
        prefs.makeWorldReadable();

        if (prefs.getBoolean("tweak_minimumbrightness", false)) {
            // from https://github.com/GravityBox/GravityBox
            XResources.setSystemWideReplacement("android", "integer", "config_screenBrightnessSettingMinimum", 1);
            XResources.setSystemWideReplacement("android", "integer", "config_screenBrightnessDim", 1);
        }
        if (prefs.getBoolean("tweak_gentlehapticfeedback", false)) {
            GentleHapticFeedback.hook();
        }
        if (prefs.getBoolean("tweak_nosafevolumewarning", false)) {
            NoSafeVolumeWarning.hook();
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals("com.android.systemui") && prefs.getBoolean("tweak_disablesuindicator", false))
            DisableSuIndicator.hook(lpparam);
        if (lpparam.packageName.equals("android") && prefs.getBoolean("tweak_alwayssoftwaremenu", false))
            AlwaysSoftwareMenu.hook(lpparam);
        if (prefs.getBoolean("tweak_notoasticons", false))
            NoToastIcons.hook(lpparam);
        if (prefs.getBoolean("tweak_pocketfirst", false))
            PocketFirst.hook(lpparam);
        if (prefs.getBoolean("tweak_deskclockalarm", false))
            DeskClockAlarm.hook(lpparam);
        if (lpparam.packageName.equals("android") && prefs.getBoolean("tweak_onewaybrightness", false))
            OneWayBrightness.hook(lpparam);
        if (lpparam.packageName.equals("android") && prefs.getBoolean("tweak_k920keyshapticfix", false))
            K920KeysHapticFix.hook(lpparam);
        if (lpparam.packageName.equals("android") && prefs.getBoolean("tweak_batterylightdisabler", false))
            BatteryLightDisabler.hook(lpparam);
        if ((lpparam.packageName.equals("org.mozilla.firefox") || lpparam.packageName.equals("org.mozilla.firefox_beta") || lpparam.packageName.equals("org.mozilla.fennec") ) && prefs.getBoolean("tweak_onehandzoom_ff", false))
            FF.hook(lpparam);
        if (lpparam.packageName.equals("com.adobe.reader") && prefs.getBoolean("tweak_onehandzoom_ar", false))
            AR.hook(lpparam);
        if (lpparam.packageName.equals("com.android.settings") && prefs.getBoolean("tweak_batteryhistoryxxl", false))
            BatteryHistoryXXL.hook(lpparam);
        if (lpparam.packageName.equals("com.android.settings") && prefs.getBoolean("tweak_nativefreezer", false))
            NativeFreezer.hook(lpparam);
        if ((lpparam.packageName.equals("com.mxtech.videoplayer.pro") ||  lpparam.packageName.equals("com.mxtech.videoplayer.ad")) && prefs.getBoolean("tweak_minimumbrightness_mx", false))
            MinimumBrightnessMX.hook(lpparam);
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam iprparam) throws Throwable {
        if (iprparam.packageName.equals("com.android.systemui") && prefs.getBoolean("tweak_hidenetworkindicators", false))
            HideNetworkIndicators.hook(iprparam);
        if (iprparam.packageName.equals("com.android.systemui") && prefs.getBoolean("tweak_compactvolumepanel", false))
            CompactVolumePanel.hook(iprparam);
    }
}
