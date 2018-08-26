package de.kah2.mondtag.calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import de.kah2.mondtag.R;

/**
 * Shows (at the moment) only the app version.
 */
public class InfoDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final PackageInfo appInfo;

        try {
            appInfo = getActivity().getPackageManager().getPackageInfo("de.kah2.mondtag", 0);

            builder.setMessage( getString(R.string.app_name) + " " + appInfo.versionName );

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(InfoDialogFragment.class.getSimpleName(), "onCreateDialog: ", e);
        }

        builder.setPositiveButton(R.string.dialog_button_close, (dialog, id) -> dialog.dismiss());

        return builder.create();
    }
}
