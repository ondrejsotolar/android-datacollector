package cz.muni.irtis.datacollector;

import android.content.Context;
import android.os.Bundle;

import java.io.File;
import java.util.List;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;
import cz.muni.irtis.datacollector.database.DatabaseHelper;

/**
 * Root screen of the UI
 */
public class RootScreenFragment extends PreferenceFragmentCompat {
    private Preference.OnPreferenceChangeListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference onOf = findPreference("onOf");
        onOf.setOnPreferenceChangeListener(listener);
    }

    /**
     * Set preference changed listener. Must be called before onCreatePreferences.
     * @param listener
     */
    void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Set ON/OFF switch value. Doesn't raise event.
     * @param value
     */
    void setOnOfSwitchValue(boolean value) {
        Preference preference = findPreference("onOf");
        if (preference instanceof TwoStatePreference) {
            TwoStatePreference onOfSwitch = (TwoStatePreference) preference;
            onOfSwitch.setOnPreferenceChangeListener(null);
            onOfSwitch.setChecked(value);
            onOfSwitch.setOnPreferenceChangeListener(listener);
        }
    }

    void setRuntime(String value) {
        Preference preference = findPreference("runtime");
        if (preference != null) {
            preference.setSummary(value);
        }
    }

    void setCollectedMetrics(List<String> names) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            sb.append(names.get(i));
            if (sb.length() < 30) {
                sb.append(", ");
            } else {
                sb.append("...");
                break;
            }
        }
        Preference preference = findPreference("metricsTaken");
        preference.setSummary(sb.toString());
    }

    void setLocalFilesSize(Context context, long dbSize) {
        File folder = context.getExternalFilesDir(null);
        long bits = getFolderSize(folder) + dbSize;
        Preference preference = findPreference("localStorage");
        preference.setSummary(String.valueOf(bitsToMB(bits)) + " MB");
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        int count = files.length;
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            }
            else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    private long bitsToMB(long bits) {
        int mb = 1024*1024;
        return bits < mb ? 0 : bits / mb;
    }

}
