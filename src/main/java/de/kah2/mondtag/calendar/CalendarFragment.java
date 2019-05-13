package de.kah2.mondtag.calendar;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.threeten.bp.LocalDate;

import java.util.LinkedList;
import java.util.List;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.Day;
import de.kah2.mondtag.InfoDialogFragment;
import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.MondtagActivity;
import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.DataManager;

/**
 * This {@link Fragment} is used to display the calendar.
 */
public class CalendarFragment extends Fragment
        implements InterpretationMenuManager.InterpretationChangeListener {

    public final static String TAG = CalendarFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private DayRecyclerViewAdapter dayRecyclerViewAdapter;

    private final InterpretationMenuManager interpretationMenuManager =
            new InterpretationMenuManager();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        this.recyclerView = view.findViewById(R.id.day_recycler_view);
        this.recyclerView.setLayoutManager( this.createLayoutManager() );

        this.dayRecyclerViewAdapter = new DayRecyclerViewAdapter();
        recyclerView.setAdapter(dayRecyclerViewAdapter);
        this.dayRecyclerViewAdapter.setClickListener( this.createDayClickListener() );

        this.setHasOptionsMenu(true);

        // When we return from DayDetailFragment or SettingsFragment, we need to disable the back button
        ((MondtagActivity) getActivity()).setUpButtonVisible(false);

        updateCalendar();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.interpretationMenuManager.setInterpretationChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.interpretationMenuManager.resetInterpretationChangeListener();
    }

    private RecyclerView.LayoutManager createLayoutManager() {
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return linearLayoutManager;
    }

    private DayRecyclerViewAdapter.ClickListener createDayClickListener() {
        return new DayRecyclerViewAdapter.ClickListener() {

            // TODO move logic for extending future here

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: showing main menu");

        // clear - otherwise we get duplicate menu entries
        menu.clear();

        inflater.inflate(R.menu.menu, menu);

        final Menu interpretationsMenu = menu.findItem(R.id.menu_interpretations).getSubMenu();

        this.interpretationMenuManager.addInterpreters( interpretationsMenu );
    }

    /**
     * Called when the user clicks on a {@link MenuItem}.
     * Can open the info dialog or the settings.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " selected.");

        switch (item.getItemId()) {
            case R.id.action_info:
                this.showInfo();
                return true;
            case R.id.action_settings:
                Log.d(TAG, "Showing settings ...");
                ((MondtagActivity) getActivity()).activateConfiguration();
                return true;
            case R.id.action_scroll_to_today:
                this.scrollToToday();
                return true;
            case R.id.menu_interpretations:
                // Nothing to do
                return true;
            default:
                if ( this.interpretationMenuManager.onMenuItemClick(item) ) {
                    // click got handled by InterpretationMenuManager
                    return true;
                } else {
                    Log.e(TAG, "Unknown Action: "
                            + item.getTitle() + " / " + item.getItemId());
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    private void updateCalendar() {

        final LinkedList<Day> days = this.getDaysToDisplay();
        this.dayRecyclerViewAdapter.setDays(days);
        this.scrollToToday(days);

        final ActionBar actionBar = ((MondtagActivity) this.getActivity()).getSupportActionBar();
        actionBar.setSubtitle( this.getDataManager().getSelectedInterpreterNameId() );
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

        final LinkedList<Day> days = calendar.getValidDays();

        return days;
    }

    @Override
    public void onInterpreterChanged(MappedInterpreter interpreter) {

        if (interpreter == null) {

            Log.d(TAG, "onInterpreterChanged: mapping is null - removing interpreter");
            this.getDataManager().setSelectedInterpreter(null);

        } else {

            Log.d(TAG, "onInterpreterChanged: setting interpreter: "
                    + getActivity().getApplicationContext().getString( interpreter.getId() ) );
            this.getDataManager().setSelectedInterpreter(interpreter);
        }

        this.updateCalendar();
    }

    private void showInfo() {

        Log.d(TAG, "Showing info ...");
        InfoDialogFragment infoDialog = new InfoDialogFragment();
        infoDialog.show( getFragmentManager() );
    }

    private DataManager getDataManager() {
        return ((Mondtag) getActivity().getApplicationContext()).getDataManager();
    }
}