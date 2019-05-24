package cz.muni.irtis.datacollector.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.File;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;
import cz.muni.irtis.datacollector.R;
import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

/**
 * Root screen of the UI
 */
public class RootScreenFragment extends PreferenceFragmentCompat {
    private Preference.OnPreferenceChangeListener preferenceChangeListener;
    private Preference.OnPreferenceClickListener preferenceClickListener;
    private BroadcastReceiver broadcastReceiver;
    private static String lastRuntime = "00:00:00";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference onOf = findPreference("onOf");
        onOf.setOnPreferenceChangeListener(preferenceChangeListener);

        Preference sync = findPreference("lastSync");
        sync.setOnPreferenceClickListener(preferenceClickListener);

        Preference metrics = findPreference("metricsTaken");
        metrics.setOnPreferenceClickListener(preferenceClickListener);

        Preference storage = findPreference("localStorage");
        storage.setOnPreferenceClickListener(preferenceClickListener);


    }

    @Override
    public void onResume() {
        super.onResume();

        initBroadcastreceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                new IntentFilter("elapsed_time"));

        setRuntime(lastRuntime);
        setActionBar(false);
        setOnOfSwitchValue(SchedulerService.IS_RUNNING);
        setLocalFilesSize(getContext(), DatabaseHelper.getInstance(getContext()).getSize());

    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);

        super.onDestroyView();
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
        FilesAsyncTask task = new FilesAsyncTask();
        task.execute(folder, dbSize, findPreference("localStorage"));

    }



    private void setActionBar(boolean value) {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(value);
        }
    }

    private void initBroadcastreceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("elapsed_time".equals(intent.getAction())) {
                    lastRuntime = intent.getStringExtra("elapsed");
                    setRuntime(lastRuntime);
                }
            }
        };
    }

    private static class FilesAsyncTask extends AsyncTask<Object, Void, Void>{
        private long bits;
        private long dbSize;
        Preference toUpdate;

        @Override
        protected Void doInBackground(Object... params) {
            this.bits = getFolderSize((File) params[0]);
            this.dbSize = (long) params[1];
            this.toUpdate = (Preference) params[2];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            toUpdate.setSummary("Files: " + bitsToMB(bits) + " MB, Database: " + bitsToMB(dbSize) + " MB");
            super.onPostExecute(aVoid);
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

}
