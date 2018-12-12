package de.kah2.mondtag.calendar;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.threeten.bp.LocalDate;

import java.util.LinkedList;
import java.util.List;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.Day;
import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.MondtagActivity;
import de.kah2.mondtag.R;

/**
 * This {@link Fragment} is used to display the calendar.
 */
public class CalendarFragment extends Fragment {

    public final static String TAG = CalendarFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private DayRecyclerViewAdapter dayRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        this.recyclerView = view.findViewById(R.id.day_recycler_view);
        this.recyclerView.setLayoutManager( this.createLayoutManager() );

        this.dayRecyclerViewAdapter = new DayRecyclerViewAdapter();
        recyclerView.setAdapter(dayRecyclerViewAdapter);
        this.dayRecyclerViewAdapter.setClickListener( this.createDayClickListener() );

        updateCalendar();

        return view;
    }

    private RecyclerView.LayoutManager createLayoutManager() {
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return linearLayoutManager;
    }

    private DayRecyclerViewAdapter.ClickListener createDayClickListener() {
        return new DayRecyclerViewAdapter.ClickListener() {

            @Override
            public void onShortClick(Day day) {
                Log.d(TAG, "onShortClick: " + day.getDate());

                ((MondtagActivity) getActivity()).activateDayDetailView(day);
            }

            @Override
            public boolean onLongClick(Day day) {
                Log.d(TAG, "onLongClick: " + day.getDate());

                final CalendarEvent event = new CalendarEvent(
                        CalendarFragment.this.getActivity().getApplicationContext(), day );
                CalendarFragment.this.startActivity( event.toIntent() );
                return true;
            }
        };
    }

    private void updateCalendar() {

        final LinkedList<Day> days = this.getDaysToDisplay();

        this.dayRecyclerViewAdapter.setDays(days);

        this.scrollToToday(days);
    }

    public void scrollToToday() {

        this.scrollToToday( this.getDaysToDisplay() );
    }

    private void scrollToToday(List<Day> days) {
        final LinearLayoutManager layoutManager =
                (LinearLayoutManager) this.recyclerView.getLayoutManager();

        int count = 0;
        final LocalDate today = LocalDate.now();

        for (Day day : days) {
            if ( day.getDate().isEqual(today) ) break;
            else count++;
        }

        layoutManager.scrollToPositionWithOffset(count, 25);
    }

    private LinkedList<Day> getDaysToDisplay() {

        final Calendar calendar =
                ((Mondtag) getActivity().getApplicationContext()).getDataManager().getCalendar();

        return calendar.getValidDays();
    }
}
