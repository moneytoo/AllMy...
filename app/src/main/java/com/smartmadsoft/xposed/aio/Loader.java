package com.smartmadsoft.xposed.aio;

import android.content.res.XResources;

import com.smartmadsoft.xposed.aio.tweaks.NoExitConfirmation;
import com.smartmadsoft.xposed.aio.tweaks.DisableNetworkMonitoredNotification;
import com.smartmadsoft.xposed.aio.tweaks.GMapsMinimizedBar;
import com.smartmadsoft.xposed.aio.tweaks.cyanogenmod.AlwaysSoftwareMenu;
import com.smartmadsoft.xposed.aio.tweaks.BatteryHistoryXXL;
import com.smartmadsoft.xposed.aio.tweaks.cyanogenmod.BatteryLightDisabler;
import com.smartmadsoft.xposed.aio.tweaks.CompactVolumePanel;
import com.smartmadsoft.xposed.aio.tweaks.DisableAdbNotification;
import com.smartmadsoft.xposed.aio.tweaks.DisableHorizontalScrollAR;
import com.smartmadsoft.xposed.aio.tweaks.cyanogenmod.DisableSuIndicator;
import com.smartmadsoft.xposed.aio.tweaks.DisableUsbNotification;
import com.smartmadsoft.xposed.aio.tweaks.copycat.GentleHapticFeedback;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.GentleHapticFeedbackTouchWiz;
import com.smartmadsoft.xposed.aio.tweaks.copycat.HideNetworkIndicators;
import com.smartmadsoft.xposed.aio.tweaks.MediaKeys;
import com.smartmadsoft.xposed.aio.tweaks.MediaStreamDefault;
import com.smartmadsoft.xposed.aio.tweaks.MinimumBrightnessMX;
import com.smartmadsoft.xposed.aio.tweaks.NativeFreezer;
import com.smartmadsoft.xposed.aio.tweaks.copycat.NoOverlayWarning;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.LedOffDuringDnD;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.No2G;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.NoPasswordAfterBootTW;
import com.smartmadsoft.xposed.aio.tweaks.copycat.NoSafeVolumeWarning;
import com.smartmadsoft.xposed.aio.tweaks.cyanogenmod.NoToastIcons;
import com.smartmadsoft.xposed.aio.tweaks.cyanogenmod.OneWayBrightness;
import com.smartmadsoft.xposed.aio.tweaks.PocketFirst;
import com.smartmadsoft.xposed.aio.tweaks.ChromeTabsToolbarOnPhone;
import com.smartmadsoft.xposed.aio.tweaks.copycat.QuickUnlock;
import com.smartmadsoft.xposed.aio.tweaks.cyanogenmod.RemapVolume;
import com.smartmadsoft.xposed.aio.tweaks.obsolete.K920Cardboard;
import com.smartmadsoft.xposed.aio.tweaks.NoWakeOnCharge;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.RemapRedialToNextTrack;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S5TouchWizJunk;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S5ReaderMode;
import com.smartmadsoft.xposed.aio.tweaks.GMSDisabler;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S7AlwaysAllowMTP;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S7DetailedBatteryUsage;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S7MTPWithoutUnlocking;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S7TouchKeyLight;
import com.smartmadsoft.xposed.aio.tweaks.touchwiz.S7sRGBVideo;
import com.smartmadsoft.xposed.aio.tweaks.VolumeKeysCursorControl;
import com.smartmadsoft.xposed.aio.tweaks.onehandzoomenabler.AR;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Loader implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    private static XSharedPreferences prefs;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences("com.smartmadsoft.xposed.aio", "tweaks");
        prefs.makeWorldReadable();

        int brightnessValue = Integer.parseInt(prefs.getString("tweak_minimumbrightness_list", "-1"));
        if (brightnessValue > 0) {
            // from https://github.com/GravityBox/GravityBox
            XResources.setSystemWideReplacement("android", "integer", "config_screenBrightnessSettingMinimum", brightnessValue);
            XResources.setSystemWideReplacement("android", "integer", "config_screenBrightnessDim", brightnessValue);
        }
        if (prefs.getBoolean("tweak_chrometabstoolbaronphone", false))
            ChromeTabsToolbarOnPhone.init();
        if (prefs.getBoolean("tweak_s5twjunk", false)) {
            XResources.setSystemWideReplacement("android", "bool", "show_ongoing_ime_switcher", false);
            //XResources.setSystemWideReplacement("android", "bool", "config_intrusiveNotificationLed", true);
        }
        if (prefs.getBoolean("tweak_nowakeoncharge", false))
            XResources.setSystemWideReplacement("android", "bool", "config_unplugTurnsOnScreen", false);

        //XResources.setSystemWideReplacement("android", "bool", "config_showNavigationBar", true);
        //XResources.setSystemWideReplacement("com.android.settings", "bool", "config_showNavigationBar", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            if (prefs.getBoolean("tweak_nosafevolumewarning", false))
                NoSafeVolumeWarning.hook(lpparam);
            if (prefs.getBoolean("tweak_onewaybrightness", false))
                OneWayBrightness.hook(lpparam);
            if (prefs.getBoolean("tweak_batterylightdisabler", false))
                BatteryLightDisabler.hook(lpparam);
            if (prefs.getBoolean("tweak_alwayssoftwaremenu", false))
                AlwaysSoftwareMenu.hook(lpparam);
            int hapticValue = Integer.parseInt(prefs.getString("tweak_gentlehapticfeedback_list", "-1"));
            if (hapticValue > 0)
                GentleHapticFeedback.hook(lpparam, hapticValue);
            boolean tweakPlayPause = prefs.getBoolean("tweak_remapprevtoplaypause", false);
            boolean tweakTorch = prefs.getBoolean("tweak_remapnexttotorch", false);
            if (tweakPlayPause || tweakTorch)
                RemapVolume.hook(lpparam, tweakPlayPause, tweakTorch);
            if (prefs.getBoolean("tweak_nowakeoncharge", false))
                NoWakeOnCharge.hook(lpparam);
            if (prefs.getBoolean("tweak_s5readermode", false))
                S5ReaderMode.hook(lpparam);
            if (prefs.getBoolean("tweak_mediastreamdefault", false))
                MediaStreamDefault.hook(lpparam);
            if (prefs.getBoolean("tweak_t700gentlehapticfeedback", false))
                GentleHapticFeedbackTouchWiz.hook(lpparam);
            if (prefs.getBoolean("tweak_mediakeys", false))
                MediaKeys.hook(lpparam);
            if (prefs.getBoolean("tweak_disableusbnotification", false))
                DisableUsbNotification.hook(lpparam);
            if (prefs.getBoolean("tweak_disableadbnotification", false))
                DisableAdbNotification.hook(lpparam);
            if (prefs.getBoolean("tweak_k920cardboard", false))
                K920Cardboard.hookAndroid(lpparam);
            if (prefs.getBoolean("tweak_s7srgbvideo", false))
                S7sRGBVideo.hook(lpparam);
            if (prefs.getBoolean("tweak_disablenetworkmonitorednotification", false))
                DisableNetworkMonitoredNotification.hook(lpparam);
            boolean tweakLED = prefs.getBoolean("tweak_s7dndledoff", false);
            boolean tweakVibrator = prefs.getBoolean("tweak_s7dndvibratoroff", false);
            if (tweakLED || tweakVibrator)
                LedOffDuringDnD.hook(lpparam, tweakLED, tweakVibrator);
        }
        if (lpparam.packageName.equals("com.android.server.telecom")) {
            if (prefs.getBoolean("tweak_k920cardboard", false))
                K920Cardboard.hookTelecom(lpparam);
        }
        if (lpparam.packageName.equals("com.android.systemui")) {
            if (prefs.getBoolean("tweak_disablesuindicator", false))
                DisableSuIndicator.hook(lpparam);
            if (prefs.getBoolean("tweak_nowakeoncharge", false))
                NoWakeOnCharge.hookUI(lpparam);
            if (prefs.getBoolean("tweak_s5twjunk", false))
                S5TouchWizJunk.hookUI(lpparam);
            if (prefs.getBoolean("tweak_s5twjunk_dnd", false))
                S5TouchWizJunk.hookUIDND(lpparam);
            if (prefs.getBoolean("tweak_k920cardboard", false))
                K920Cardboard.hookUI(lpparam);
            if (prefs.getBoolean("tweak_twnopasswordafterboot", false))
                NoPasswordAfterBootTW.hook(lpparam);
            if (prefs.getBoolean("tweak_quickunlock", false))
                QuickUnlock.hook(lpparam);
        }
        if (lpparam.packageName.equals("com.android.settings")) {
            if (prefs.getBoolean("tweak_batteryhistoryxxl", false))
                BatteryHistoryXXL.hook(lpparam);
            if (prefs.getBoolean("tweak_nativefreezer", false))
                NativeFreezer.hook(lpparam);
            if (prefs.getBoolean("tweak_s5twjunk", false))
                S5TouchWizJunk.hookSettings(lpparam);
            if (prefs.getBoolean("tweak_k920cardboard", false))
                K920Cardboard.hookSettings(lpparam);
            if (prefs.getBoolean("tweak_s7touchkeylight", false))
                S7TouchKeyLight.hook(lpparam);
            if (prefs.getBoolean("tweak_s7detailedbatteryusage", false))
                S7DetailedBatteryUsage.hook(lpparam);
            if (prefs.getBoolean("tweak_disablebatteryprediction", false))
                DisableBatteryPrediction.hook(lpparam);
        }
        if (lpparam.packageName.equals("com.adobe.reader")) {
            if (prefs.getBoolean("tweak_onehandzoom_ar", false))
                AR.hook(lpparam);
            if (prefs.getBoolean("tweak_disablehorizontalscroll_ar", false))
                DisableHorizontalScrollAR.hook(lpparam);
        }
        if (prefs.getBoolean("tweak_notoasticons", false))
            NoToastIcons.hook(lpparam);
        if (prefs.getBoolean("tweak_pocketfirst", false))
            PocketFirst.hook(lpparam);
        if ((lpparam.packageName.equals("com.mxtech.videoplayer.pro") ||  lpparam.packageName.equals("com.mxtech.videoplayer.ad")) && prefs.getBoolean("tweak_minimumbrightness_mx", false))
            MinimumBrightnessMX.hook(lpparam);
        if (ChromeTabsToolbarOnPhone.isChrome(lpparam.packageName) && prefs.getBoolean("tweak_chrometabstoolbaronphone", false))
            ChromeTabsToolbarOnPhone.hook(lpparam);
        if (prefs.getBoolean("tweak_k920cardboard", false))
            K920Cardboard.hookAll(lpparam);
        if (lpparam.packageName.equals("com.sec.android.app.popupuireceiver") && prefs.getBoolean("tweak_s5twjunk", false))
            S5TouchWizJunk.hook(lpparam);
        if (lpparam.packageName.equals("com.google.android.gms") && prefs.getBoolean("tweak_gmslocationdialog", false))
            GMSDisabler.hook(lpparam);
        int volumeValue = Integer.parseInt(prefs.getString("tweak_volumekeyscursorcontrol_list", "-1"));
        if (volumeValue > 0)
            VolumeKeysCursorControl.hook(lpparam, volumeValue);
        if (prefs.getBoolean("tweak_nooverlaywarning", false) && lpparam.packageName.endsWith(".packageinstaller"))
            NoOverlayWarning.hook(lpparam);
        if (lpparam.packageName.equals("com.samsung.android.MtpApplication")) {
            if (prefs.getBoolean("tweak_s7alwaysallowmtp", false))
                S7AlwaysAllowMTP.hook(lpparam);
            if (prefs.getBoolean("tweak_s7mtpwithoutunlock", false))
                S7MTPWithoutUnlocking.hook(lpparam);
        }
        if (lpparam.packageName.equals("com.samsung.android.themecenter"))
            S5TouchWizJunk.hookThemes(lpparam);
        if (lpparam.packageName.equals("com.android.bluetooth") && prefs.getBoolean("tweak_s7remapredialtonext", false))
            RemapRedialToNextTrack.hook(lpparam);
        if (lpparam.packageName.equals("com.android.phone") && prefs.getBoolean("tweak_s7no2g", false))
            No2G.hook(lpparam);
        if (lpparam.packageName.equals("com.google.android.apps.maps") && prefs.getBoolean("tweak_gmminimizedbar", false))
            GMapsMinimizedBar.hook(lpparam);
        if (prefs.getBoolean("tweak_appbackuprestorenoexitconfirmation", false)) {
            if ((lpparam.packageName.startsWith("mobi.infolife.appbackup") || lpparam.packageName.equals("mobi.trustlab.appbackup")))
                NoExitConfirmation.hookBackup(lpparam);
            if (lpparam.packageName.equals("com.circlegate.tt.transit.android"))
                NoExitConfirmation.hookCG(lpparam);
            if (lpparam.packageName.equals("pl.solidexplorer2"))
                NoExitConfirmation.hookSolid(lpparam);
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam iprparam) throws Throwable {
        if (iprparam.packageName.equals("com.android.systemui")) {
            if (prefs.getBoolean("tweak_hidenetworkindicators", false))
                HideNetworkIndicators.hook(iprparam);
            if (prefs.getBoolean("tweak_compactvolumepanel", false))
                CompactVolumePanel.hook(iprparam);
        }
    }
}
