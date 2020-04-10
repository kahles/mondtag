package de.kah2.mondtag.calendar;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import de.kah2.zodiac.libZodiac4A.Calendar;
import de.kah2.zodiac.libZodiac4A.Day;
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
        this.dayRecyclerViewAdapter.setClickListener( 
                new DayClickListener((MondtagActivity) this.getActivity()) );

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
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        return linearLayoutManager;
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

        int itemId = item.getItemId();
        if (itemId == R.id.action_info) {
            ((MondtagActivity) this.getActivity()).showInfo();
            return true;
        } else if (itemId == R.id.action_settings) {
            Log.d(TAG, "Showing settings ...");
            ((MondtagActivity) getActivity()).activateConfiguration();
            return true;
        } else if (itemId == R.id.action_scroll_to_today) {
            this.scrollToToday();
            return true;
        } else if (itemId == R.id.menu_interpretations) {// Nothing to do
            return true;
        }

        if (this.interpretationMenuManager.onMenuItemClick(item)) {
            // click got handled by InterpretationMenuManager
            return true;
        } else {
            Log.e(TAG, "Unknown Action: "
                    + item.getTitle() + " / " + item.getItemId());
            return super.onOptionsItemSelected(item);
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

    private DataManager getDataManager() {
        return ((Mondtag) getActivity().getApplicationContext()).getDataManager();
    }
}