package de.kah2.mondtag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

/**
 * Shows the app version and basic information.
 */
public class InfoDialogFragment extends DialogFragment {

    private static final String TAG = InfoDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return this.createAlertDialog( getActivity() );
    }

    public void show(FragmentManager fragmentManager) {

        super.show(fragmentManager, TAG);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Needed for links to be "activated"
        final TextView messageView = getDialog().findViewById(android.R.id.message);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private AlertDialog createAlertDialog(Activity activity) {

        return new AlertDialog.Builder(activity)
                .setTitle( createTitle(activity) )
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_button_close, null)
                .setMessage( createMessage(activity) )
                .create();
    }

    private String createTitle(Context context) {

        final PackageManager manager = context.getApplicationContext().getPackageManager();
        String versionName;

        try {

            final PackageInfo appInfo =
                    manager.getPackageInfo("de.kah2.mondtag", 0);
            versionName = appInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {

            Log.e( TAG, "createTitle: ", e);
            versionName = "";
        }

        return context.getString(R.string.app_name) + " " + versionName;
    }

    private SpannableString createMessage(Context context) {

        final SpannableString messageString =
                new SpannableString(context.getText(R.string.info_dialog_message));

        Linkify.addLinks(messageString, Linkify.WEB_URLS);

        return messageString;
    }
}
