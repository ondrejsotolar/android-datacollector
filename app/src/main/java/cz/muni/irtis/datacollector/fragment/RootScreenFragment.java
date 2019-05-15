package cz.muni.irtis.datacollector.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.File;
import java.util.List;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;
import cz.muni.irtis.datacollector.R;

/**
 * Root screen of the UI
 */
public class RootScreenFragment extends PreferenceFragmentCompat {
    private Preference.OnPreferenceChangeListener preferenceChangeListener;
    private Preference.OnPreferenceClickListener preferenceClickListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference onOf = findPreference("onOf");
        onOf.setOnPreferenceChangeListener(preferenceChangeListener);

        Preference sync = findPreference("lastSync");
        sync.setOnPreferenceClickListener(preferenceClickListener);

        Preference metrics = findPreference("metricsTaken");
        metrics.setOnPreferenceClickListener(preferenceClickListener);
    }

    /**
     * Set preference changed preferenceChangeListener. Must be called before onCreatePreferences.
     * @param listener
     */
    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener listener) {
        this.preferenceChangeListener = listener;
    }

    public void setPreferenceClickListener(Preference.OnPreferenceClickListener preferenceClickListener) {
        this.preferenceClickListener = preferenceClickListener;
    }

    /**
     * Set ON/OFF switch value. Doesn't raise event.
     * @param value
     */
    public void setOnOfSwitchValue(boolean value) {
        Preference preference = findPreference("onOf");
        if (preference instanceof TwoStatePreference) {
            TwoStatePreference onOfSwitch = (TwoStatePreference) preference;
            onOfSwitch.setOnPreferenceChangeListener(null);
            onOfSwitch.setChecked(value);
            onOfSwitch.setOnPreferenceChangeListener(preferenceChangeListener);
        }
    }

    public void setRuntime(String value) {
        Preference preference = findPreference("runtime");
        if (preference != null) {
            preference.setSummary(value);
        }
    }

    public void setCollectedMetrics(List<String> names) {
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

    public void setLocalFilesSize(final Context context, final long dbSize) {
        final File folder = context.getExternalFilesDir(null);
        AsyncTask task = new AsyncTask() {
            long bits;

            @Override
            protected Object doInBackground(Object[] objects) {
                this.bits = getFolderSize(folder) + dbSize;
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                Preference preference = findPreference("localStorage");
                preference.setSummary(String.valueOf(bitsToMB(bits)) + " MB");
            }
        };
        task.execute();

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
