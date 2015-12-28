package com.smartmadsoft.xposed.aio.tweaks;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class CompactVolumePanel {
    public static final String PKG = "com.android.systemui";

    public static void hook(XC_InitPackageResources.InitPackageResourcesParam iprparam) {
        iprparam.res.hookLayout(PKG, "layout", "volume_panel_item", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable
            {
                final int streamIcon = liparam.res.getIdentifier("stream_icon", "id", PKG);
                final ImageView imageStreamIcon = (ImageView)liparam.view.findViewById(streamIcon);
                imageStreamIcon.getLayoutParams().height = imageStreamIcon.getLayoutParams().height / 3 * 2;

                final int furtherOptions = liparam.res.getIdentifier("further_options", "id", PKG);
                final ImageView imageFurtherOptions = (ImageView)liparam.view.findViewById(furtherOptions);
                imageFurtherOptions.getLayoutParams().height = imageFurtherOptions.getLayoutParams().height / 3 * 2;
            }
        });

        iprparam.res.hookLayout(PKG, "layout", "volume_panel", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable
            {
                final int sliderPanel = liparam.res.getIdentifier("slider_panel", "id", PKG);
                final LinearLayout layoutSliderPanel = (LinearLayout)liparam.view.findViewById(sliderPanel);
                layoutSliderPanel.setPadding(0, 0, 0, 0);
            }
        });

        iprparam.res.hookLayout(PKG, "layout", "zen_mode_panel", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable
            {
                int zenModePanelBgContainer = liparam.res.getIdentifier("zen_mode_panel_bg_container", "id", PKG);
                FrameLayout layoutZenModePanelBgContainer = (FrameLayout)liparam.view.findViewById(zenModePanelBgContainer);
                if (layoutZenModePanelBgContainer == null) {
                    zenModePanelBgContainer = liparam.res.getIdentifier("zen_buttons_container", "id", PKG);
                    layoutZenModePanelBgContainer = (FrameLayout)liparam.view.findViewById(zenModePanelBgContainer);
                }
                layoutZenModePanelBgContainer.setMinimumHeight(0);

                final int zenButtons = liparam.res.getIdentifier("zen_buttons", "id", PKG);
                final LinearLayout layoutZenButtons = (LinearLayout)liparam.view.findViewById(zenButtons);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layoutZenButtons.getLayoutParams();
                //layoutParams.setMargins(layoutParams.leftMargin, 0, layoutParams.rightMargin, 0);
                layoutParams.setMargins(0, 0, 0, 0);
                layoutZenButtons.setLayoutParams(layoutParams);

                final int zenSubheadCollapsed = liparam.res.getIdentifier("zen_subhead_collapsed", "id", PKG);
                final TextView textZenSubheadCollapsed = (TextView)liparam.view.findViewById(zenSubheadCollapsed);

                final int zenSubhead = liparam.res.getIdentifier("zen_subhead", "id", PKG);
                final RelativeLayout layoutZenSubhead = (RelativeLayout)liparam.view.findViewById(zenSubhead);
                layoutZenSubhead.getLayoutParams().height = textZenSubheadCollapsed.getLayoutParams().height;

                final int zenConditions = liparam.res.getIdentifier("zen_conditions", "id", PKG);
                final LinearLayout layoutZenConditions = (LinearLayout)liparam.view.findViewById(zenConditions);
                layoutZenConditions.setPadding(0, 0, 0, 0);
            }
        });
    }
}
