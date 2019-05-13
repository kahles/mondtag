package de.kah2.mondtag.calendar;

import android.util.Log;

import de.kah2.libZodiac.Day;
import de.kah2.mondtag.MondtagActivity;

/**
 * This is the listener to manage clicks on days at
 * {@link de.kah2.mondtag.calendar.CalendarFragment} / {@link DayRecyclerViewAdapter}
 */
class DayClickListener {

    public static final String TAG = DayClickListener.class.getSimpleName();

    private MondtagActivity activity;

    DayClickListener(MondtagActivity mondtagActivity) {
        this.activity = mondtagActivity;
    }

    /**
     * Is called when a list element is short-clicked.
     * @param day Data of the day which got clicked, or null if clicked element was the extend
     *            button.
     */
    void onShortClick(Day day) {

        if (day == null) {

            activity.extendFuture();

        } else {

            Log.d(TAG, "onShortClick: " + day.getDate());
            activity.activateDayDetailView(day);
        }
    }

    /**
     * Is called when a list element is long-clicked.
     * @param day Data of the day which got clicked, or null if clicked element was the extend
     *            button.
     * @return true, if click was consumed.
     */
    @SuppressWarnings("SameReturnValue")
    boolean onLongClick(Day day) {

        if (day == null) {

            activity.extendFuture();

        } else {
            Log.d(TAG, "onLongClick: " + day.getDate());

            final CalendarEvent event = new CalendarEvent(activity.getApplicationContext(), day);

            activity.startActivity(event.toIntent());
        }

        return true;
    }
}
