package cz.muni.irtis.datacollector.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import cz.muni.irtis.datacollector.R;

public class SyncScreenFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sync_screen, rootKey);
    }
}
