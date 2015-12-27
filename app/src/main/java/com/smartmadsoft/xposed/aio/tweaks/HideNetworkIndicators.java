package com.smartmadsoft.xposed.aio.tweaks;

import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class HideNetworkIndicators {
    public static void hook(XC_InitPackageResources.InitPackageResourcesParam iprparam) {
        iprparam.res.hookLayout("com.android.systemui", "layout", "signal_cluster_view", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ImageView wifi_inout = (ImageView) liparam.view.findViewById(liparam.res.getIdentifier("wifi_inout", "id", "com.android.systemui"));
                if (wifi_inout != null)
                    wifi_inout.setVisibility(View.GONE);
                ImageView mobile_inout = (ImageView) liparam.view.findViewById(liparam.res.getIdentifier("mobile_inout", "id", "com.android.systemui"));
                if (mobile_inout != null)
                    mobile_inout.setVisibility(View.GONE);
            }
        });

        iprparam.res.hookLayout("com.android.systemui", "layout", "mobile_signal_group", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                ImageView mobile_inout = (ImageView) liparam.view.findViewById(liparam.res.getIdentifier("mobile_inout", "id", "com.android.systemui"));
                if (mobile_inout != null)
                    mobile_inout.setVisibility(View.GONE);
            }
        });
    }
}
