/*
 * Copyright (C) 2017 The ABC rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abc.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.abc.settings.preferences.ScreenshotEditPackageListAdapter;
import com.abc.settings.preferences.ScreenshotEditPackageListAdapter.PackageItem;

public class OtherSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String APPS_SECURITY = "apps_security";
    private static final String SMS_OUTGOING_CHECK_MAX_COUNT = "sms_outgoing_check_max_count";

    private static final int DIALOG_SCREENSHOT_EDIT_APP = 1;

    private ListPreference mSmsCount;
    private int mSmsCountValue;

    private Preference mScreenshotEditAppPref;
    private ScreenshotEditPackageListAdapter mPackageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.abc_other_settings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        PreferenceCategory appsSecCategory = (PreferenceCategory) findPreference(APPS_SECURITY);

        mSmsCount = (ListPreference) findPreference(SMS_OUTGOING_CHECK_MAX_COUNT);
        mSmsCountValue = Settings.Global.getInt(resolver,
                Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, 30);
        mSmsCount.setValue(Integer.toString(mSmsCountValue));
        mSmsCount.setSummary(mSmsCount.getEntry());
        mSmsCount.setOnPreferenceChangeListener(this);
        if (!Utils.isVoiceCapable(getActivity())) {
            appsSecCategory.removePreference(mSmsCount);
            prefScreen.removePreference(appsSecCategory);
        }

        mPackageAdapter = new ScreenshotEditPackageListAdapter(getActivity());
        mScreenshotEditAppPref = findPreference("screenshot_edit_app");
        mScreenshotEditAppPref.setOnPreferenceClickListener(this);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_SCREENSHOT_EDIT_APP: {
                Dialog dialog;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                final ListView list = new ListView(getActivity());
                list.setAdapter(mPackageAdapter);
                alertDialog.setTitle(R.string.profile_choose_app);
                alertDialog.setView(list);
                dialog = alertDialog.create();
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.SCREENSHOT_EDIT_USER_APP, info.packageName);
                        dialog.cancel();
                    }
                });
                return dialog;
            }
         }
        return super.onCreateDialog(dialogId);
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case DIALOG_SCREENSHOT_EDIT_APP:
                return MetricsEvent.ABC;
            default:
                return 0;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // Don't show the dialog if there are no available editor apps
        if (preference == mScreenshotEditAppPref && mPackageAdapter.getCount() > 0) {
            showDialog(DIALOG_SCREENSHOT_EDIT_APP);
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.screenshot_edit_app_no_editor),
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ABC;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSmsCount) {
            mSmsCountValue = Integer.valueOf((String) newValue);
            int index = mSmsCount.findIndexOfValue((String) newValue);
            mSmsCount.setSummary(
                    mSmsCount.getEntries()[index]);
            Settings.Global.putInt(resolver,
                    Settings.Global.SMS_OUTGOING_CHECK_MAX_COUNT, mSmsCountValue);
            return true;
        }
        return false;
    }
}
