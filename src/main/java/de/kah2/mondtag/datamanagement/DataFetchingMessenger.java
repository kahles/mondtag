package de.kah2.mondtag.datamanagement;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import de.kah2.zodiac.libZodiac4A.ProgressListener;

/**
 * <p>This class is used to update the UI about status of data retrieving. It collects information
 * from {@link DataManager} and {@link DataFetcher}/libZodiac and creates notifications for
 * {@link DataFetchingFragment}.</p>
 */
public class DataFetchingMessenger implements ProgressListener {

    private final static String TAG = DataFetchingMessenger.class.getSimpleName();

    private final static int MESSAGE_STATE_CHANGE = 0;
    private final static int MESSAGE_UPDATE_PROGRESS = 1;

    private final Handler messageHandler;

    private ProgressListener displayer;

    DataFetchingMessenger() {
        this.messageHandler = this.createMessageHandler();
    }

    public void setDisplayer(ProgressListener displayer) {
        this.displayer = displayer;
    }

    /**
     * Creates a {@link Handler} for managing progress information of data generation.
     */
    private Handler createMessageHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (DataFetchingMessenger.this.displayer != null) {
                    final ProgressListener displayer = DataFetchingMessenger.this.displayer;

                    switch (msg.what) {
                        case MESSAGE_UPDATE_PROGRESS:
                            displayer.onCalculationProgress((float) msg.obj);
                            break;
                        case MESSAGE_STATE_CHANGE:
                            displayer.onStateChanged((State) msg.obj);
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            }
        };
    }

    @Override
    public void onStateChanged(State state) {

        String stateStr;

        if (state == null)
            stateStr = "reset";
        else
            stateStr = state.toString();

        Log.d(TAG, "onStateChanged: " + stateStr);

        final Message message = this.messageHandler.obtainMessage(MESSAGE_STATE_CHANGE, state);

        message.sendToTarget();
    }

    @Override
    public void onCalculationProgress(float percent) {
        Message message = this.messageHandler.obtainMessage(
                MESSAGE_UPDATE_PROGRESS, percent );
        message.sendToTarget();
    }
}
