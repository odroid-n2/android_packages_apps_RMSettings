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

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NotificationDrawerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    ListPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.abc_notification_drawer_settings);

        int qpmode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1, UserHandle.USER_CURRENT);
        mQuickPulldown = (ListPreference) findPreference("status_bar_quick_qs_pulldown");
        mQuickPulldown.setValue(String.valueOf(qpmode));
        mQuickPulldown.setOnPreferenceChangeListener(this);

    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ABC;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value,
                    UserHandle.USER_CURRENT);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            mQuickPulldown.setSummary(
                    mQuickPulldown.getEntries()[index]);
            return true;
        }

        return false;
    }
}
