package com.smartmadsoft.xposed.aio;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class SettingsActivity extends PreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    static boolean showWarning = false;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        //sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, preference.getContext().getSharedPreferences("tweaks", Context.MODE_WORLD_READABLE ).getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //migratePrefs();

        setupActionBar();

        showWarning = true;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || TweaksPreferenceFragment.class.getName().equals(fragmentName)
                || ExperimentalPreferenceFragment.class.getName().equals(fragmentName)
                || CopycatsPreferenceFragment.class.getName().equals(fragmentName)
                || S5PreferenceFragment.class.getName().equals(fragmentName)
                || ThirdPartyPreferenceFragment.class.getName().equals(fragmentName)
                || CyanogenModPreferenceFragment.class.getName().equals(fragmentName);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_icon:
                toggleIcon();
                return true;
            case android.R.id.home:
                finish();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TweaksPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            getPreferenceManager().setSharedPreferencesName("tweaks");

            addPreferencesFromResource(R.xml.pref_tweaks);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("tweak_volumekeyscursorcontrol_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ExperimentalPreferenceFragment extends PreferenceFragment {
        Preference openCallFeaturesSetting;
        Preference openPowerUsageSummary;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            getPreferenceManager().setSharedPreferencesName("tweaks");

            addPreferencesFromResource(R.xml.pref_experimental);
            setHasOptionsMenu(true);

            openCallFeaturesSetting = findPreference("open_callfeaturessetting");
            openCallFeaturesSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName("com.android.phone","com.android.phone.settings.PhoneAccountSettingsActivity"));
                    try {
                        startActivity(intent);
                    } catch (Exception x) {
                        try {
                            intent.setComponent(new ComponentName("com.android.phone","com.android.phone.CallFeaturesSetting"));
                            startActivity(intent);
                        } catch (Exception xx) {
                            Toast.makeText(getActivity(), "No activity found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });

            openPowerUsageSummary = findPreference("open_powerusagesummary");
            openPowerUsageSummary.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName("com.android.settings","com.android.settings.fuelgauge.PowerUsageSummary"));
                    try {
                        startActivity(intent);
                    } catch (Exception x) {
                        Toast.makeText(getActivity(), "No activity found", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CopycatsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            getPreferenceManager().setSharedPreferencesName("tweaks");

            addPreferencesFromResource(R.xml.pref_copycats);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("tweak_minimumbrightness_list"));
            bindPreferenceSummaryToValue(findPreference("tweak_gentlehapticfeedback_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class S5PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            getPreferenceManager().setSharedPreferencesName("tweaks");

            addPreferencesFromResource(R.xml.pref_s5);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ThirdPartyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            getPreferenceManager().setSharedPreferencesName("tweaks");

            addPreferencesFromResource(R.xml.pref_thirdparty);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CyanogenModPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            getPreferenceManager().setSharedPreferencesName("tweaks");

            addPreferencesFromResource(R.xml.pref_cyanogenmod);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (!isXposedPresent())
            menu.findItem(R.id.toggle_icon).setVisible(false);

        return true;
    }

    void migratePrefs() {
        final String PACKAGE_NAME = SettingsActivity.class.getPackage().getName();
        // use system paths instead of /data/data...
        File prefsFileOld = new File("/data/data/"+PACKAGE_NAME+"/shared_prefs", PACKAGE_NAME+"_preferences.xml");
        File prefsFileNew = new File("/data/data/"+PACKAGE_NAME+"/shared_prefs", "tweaks.xml");
        if (!prefsFileNew.exists() && prefsFileOld.exists()) {
            prefsFileOld.renameTo(prefsFileNew);
            prefsFileNew.setReadable(true, false);
        }
    }

    void toggleIcon() {
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, "com.smartmadsoft.xposed.aio.Launch");
        int newComponentState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        String text = "Launcher icon has been hidden";
        if (packageManager.getComponentEnabledSetting(componentName) == newComponentState) {
            newComponentState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            text = "Launcher icon has been restored";
        }
        packageManager.setComponentEnabledSetting(componentName, newComponentState, PackageManager.DONT_KILL_APP);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    boolean isXposedPresent() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("de.robv.android.xposed.installer"))
                return true;
        }
        return false;
    }

    void detectAndShowXposedDialog() {
        if (isXposedPresent())
            return;

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Xposed framework not found");
        builder.setMessage("This app is a Xposed module and requires Xposed framework to work");
        builder.setPositiveButton("Ok, quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNeutralButton("Find out more", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = "http://repo.xposed.info/module/de.robv.android.xposed.installer";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showWarning)
            detectAndShowXposedDialog();
    }
}
