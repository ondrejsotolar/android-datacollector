package cz.muni.irtis.datacollector;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

public class RootScreenFragment extends PreferenceFragmentCompat {
    Preference.OnPreferenceChangeListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference onOf = findPreference("onOf");
        onOf.setOnPreferenceChangeListener(listener);
    }

    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener listener) {
        this.listener = listener;
    }

    public void setOnOfSwitchValue(boolean value) {
        Preference preference = findPreference("onOf");
        if (preference instanceof TwoStatePreference) {
            TwoStatePreference onOfSwitch = (TwoStatePreference) preference;
            onOfSwitch.setOnPreferenceChangeListener(null);
            onOfSwitch.setChecked(value);
            onOfSwitch.setOnPreferenceChangeListener(listener);
        }
    }

    public void setRuntime(String value) {
        Preference preference = findPreference("runtime");
        if (preference != null) {
            preference.setSummary(value);
        }
    }



}
