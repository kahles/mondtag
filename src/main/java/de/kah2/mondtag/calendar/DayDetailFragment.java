package de.kah2.mondtag.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.time.Instant;
import java.time.LocalDate;

import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.MondtagActivity;
import de.kah2.mondtag.R;
import de.kah2.zodiac.libZodiac4A.Day;

/**
 * This is a {@link Fragment} to show daily information more verbose than the
 * {@link CardView}s of {@link CalendarFragment}.
 */
public class DayDetailFragment extends Fragment {

    public final static String TAG = DayDetailFragment.class.getSimpleName();

    private final static String BUNDLE_KEY_DATE =
            DayDetailFragment.class.getName() + ".Day";

    private Day day;

    /**
     * Sets a {@link Day} to display - must be set before fragment is created
     * and will be overwritten if fragment is restored from saved instance state.
     */
    public void setDay(Day day) {
        this.day = day;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_day_detail, container, false);

        this.restoreDayIfAvailable(savedInstanceState);

        DayDataDisplayer viewHolder = new DayDataDisplayer(
                view,
                ((Mondtag) getActivity().getApplicationContext()).getDataManager().getTimeZoneId() );
        viewHolder.setDayData(this.day, true);
        this.setLunarRiseSetDescriptions(view);

        this.setupReminderButton(view);

        this.setupActionBar();

        // Needed for #onOptionsItemSelected to work
        this.setHasOptionsMenu(true);

        return view;
    }

    /**
     * Restores this.day if a date was found in savedInstanceState and a corresponding
     * {@link Day}-object exists.
     *
     * If not, this method does nothing.
     */
    private void restoreDayIfAvailable(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreateView: loading savedInstanceState");

            final String dateStr = savedInstanceState.getString(BUNDLE_KEY_DATE);

            if (dateStr != null) {
                final LocalDate date = LocalDate.parse(dateStr);

                this.day = ((Mondtag) getActivity().getApplicationContext()).getDataManager()
                        .getCalendar().get(date);
            }
        }
    }

    private void setupReminderButton(View view) {
        final Button reminderButton = view.findViewById(R.id.buttonCreateReminder);
        reminderButton.setOnClickListener(v -> {
            Log.d(TAG, "reminderButton clicked");

            CalendarEvent event = new CalendarEvent(
                    getActivity().getApplicationContext(), day);
            DayDetailFragment.this.startActivity( event.toIntent() );
        });
    }

    private void setupActionBar() {

        final MondtagActivity mondtagActivity = (MondtagActivity) getActivity();

        mondtagActivity.getSupportActionBar().setSubtitle(
                ResourceMapper.formatLongDate( day.getDate() ) );

        mondtagActivity.setUpButtonVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setLunarRiseSetDescriptions(final View view) {
        final Instant rise = this.day.getPlanetaryData().getLunarRiseSet().getRise();
        final Instant set = this.day.getPlanetaryData().getLunarRiseSet().getSet();

        final TextView lunarRSFirstDescriptionTextView = view.findViewById(R.id.lunar_rise_set_first_description);
        final TextView lunarRSecondDescriptionTextView = view.findViewById(R.id.lunar_rise_set_second_description);

        if ( !day.getDate().isEqual(LocalDate.from(rise)) ) {

            // No rise today
            lunarRSFirstDescriptionTextView.setText(R.string.description_lunar_set);
            lunarRSecondDescriptionTextView.setText("");

        } else if (!day.getDate().isEqual(LocalDate.from(set))) {

            // No set today
            lunarRSecondDescriptionTextView.setText("");

        } else if (rise.isAfter(set)) {

            // Rise is after set
            lunarRSFirstDescriptionTextView.setText(R.string.description_lunar_set);
            lunarRSecondDescriptionTextView.setText(R.string.description_lunar_rise);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(BUNDLE_KEY_DATE, this.day.getDate().toString());

        super.onSaveInstanceState(outState);
    }
}
