package com.smartmadsoft.xposed.aio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class Deluxe {
    public static final String UNLOCK_PKG = "com.smartmadsoft.unlock.all";

    public static boolean hasUnlockKey(Context context) {
        return context.getPackageManager().checkSignatures(context.getPackageName(), UNLOCK_PKG) == PackageManager.SIGNATURE_MATCH || BuildConfig.DEBUG;
    }

    public static boolean isOnline(Activity activity) {
        ConnectivityManager conMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable())
            return false;

        return true;
    }

    public static void openPlayStore(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + UNLOCK_PKG));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean showUpgradeMenu(Context context) {
        if (hasUnlockKey(context))
            return false;
        return true;
    }

    public static boolean showBottomAd(Context context, Activity activity) {
        if (showUpgradeMenu(context) && isOnline(activity))
            return true;
        return false;
    }
}
