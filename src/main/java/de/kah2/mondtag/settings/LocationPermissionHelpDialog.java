package de.kah2.mondtag.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import de.kah2.mondtag.R;

/**
 * Creates a {@link DialogFragment} which shows information to the user, why permission to access
 * location is needed.
 *
 * Created by kahles on 20.12.16.
 */

public class LocationPermissionHelpDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(R.string.dialog_button_close, (dialog, id) -> {
            dialog.dismiss();
            final SettingsFragment fragment = (SettingsFragment) getFragmentManager()
                    .findFragmentByTag(SettingsFragment.TAG);
            fragment.requestPermission();
       });
        return  builder.create();
    }
}
