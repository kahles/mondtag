package de.kah2.mondtag.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import de.kah2.mondtag.R;

/**
 * Creates a {@link DialogFragment} which informs the user on first start about needed
 * configuration.
 *
 * Created by kahles
 */
public class SettingsHelpDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(getString(R.string.settings_info_dialog_message));

        builder.setPositiveButton(R.string.dialog_button_close, (dialog, id) -> dialog.dismiss());

        return builder.create();
    }
}
