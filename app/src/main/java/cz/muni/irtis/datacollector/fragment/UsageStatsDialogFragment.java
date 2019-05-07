package cz.muni.irtis.datacollector.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import cz.muni.irtis.datacollector.R;

public class UsageStatsDialogFragment extends DialogFragment {
    private static final String TAG = UsageStatsDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            Log.d(TAG, "getActivity() returned null. Dialog won't show.");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Usage data permission")
                .setIcon(R.drawable.ic_data_usage_green_24dp)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            builder.setMessage(Html.fromHtml("<b>DataCollector</b> requires that you allow the collection of usage data. Redirect to Usage access settings?", Html.FROM_HTML_MODE_LEGACY));
        } else {
            builder.setMessage(Html.fromHtml("<b>DataCollector</b> requires that you allow the collection of usage data. Redirect to Usage access settings?"));
        }
        return builder.create();
    }
}