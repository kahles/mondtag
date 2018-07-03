package de.kah2.mondtag.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;

import de.kah2.libZodiac.Day;
import de.kah2.mondtag.MondtagActivity;
import de.kah2.mondtag.R;

/**
 * Displays a scrollable calendar.
 *
 * Created by kahles on 09.11.16.
 */
public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarRecyclerViewAdapter.Element> {

    private final ArrayList<Day> days;

    private LocalDate today;

    private DayClickListener clickListener;

    CalendarRecyclerViewAdapter() {
        this.days = new ArrayList<>();
    }

    void setDays(Iterable<Day> daysIterable) {
        this.days.clear();
        for (Day day: daysIterable) {
            days.add(day);
        }
        today = LocalDate.now();
        this.notifyDataSetChanged();
    }

    @Override
    public Element onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate( viewType , parent, false);

        return new Element(inflatedView);
    }

    public int getItemViewType(int position) {
        if (position < this.days.size()) {
            return R.layout.day_layout;
        } else {
            return R.layout.extend_button;
        }
    }

    @Override
    public void onBindViewHolder(Element holder, int position) {
        Day day;

        if ( position < days.size() ) {
            // We have a "real" day
            day = days.get(position);
        } else {
            // We're at end and have the extend-button
            day = null;
        }

        holder.bindElement(day);
    }

    @Override
    public int getItemCount() {
        return days.size() + 1;
    }

    void setDayClickListener(DayClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * This is the sub-element of our {@link RecyclerView} to manage the days and the button to
     * extend data.
     */
    class Element extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        private final View view;

        private Day day;

        Element(View view) {
            super(view);
            this.view = view;
        }

        /**
         * If a day is passed as argument its data will be displayed using
         * {@link DayDataDisplayer}.
         */
        void bindElement(Day day) {
            this.day = day;

            if (day == null) {

                // the actual list element is the extend button
                view.findViewById(R.id.buttonExtendData).setOnClickListener((view)->{

                    Log.d(Element.class.getSimpleName(),
                            "clickListener: extendButton clicked");

                    ((MondtagActivity) view.getContext()).extendFuture();

                });

            } else {

                this.view.setOnClickListener(this);
                this.view.setOnLongClickListener(this);

                final DayDataDisplayer displayer = new DayDataDisplayer(view);
                displayer.setDayData(day, false);

                setDailyLayoutProperties(day, displayer);
            }
        }

        /** Manages highlighting of weekends and today */
        private void setDailyLayoutProperties(Day day, DayDataDisplayer displayer) {
            final DayOfWeek dayOfWeek = day.getDate().getDayOfWeek();

            final Context context = displayer.getContext();

            final boolean isToday = day.getDate().isEqual(CalendarRecyclerViewAdapter.this.today);

            // Elevate item, if it is TODAY

            if (isToday) {
                if (Build.VERSION.SDK_INT >= 21) {
                    this.view.setElevation(20);
                }
                this.view.setBackgroundColor(
                        ContextCompat.getColor( context, R.color.background_today) );

                displayer.getDayOfWeekView().setTypeface(null, Typeface.BOLD);

            } else {
                if (Build.VERSION.SDK_INT >= 21) {
                    this.view.setElevation(6);
                }
                this.view.setBackgroundColor(
                        ContextCompat.getColor( context, R.color.background_default) );
            }

            // Set day-of-week-color

            int color;
            if (dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY)) {

                if (isToday)
                    color = R.color.day_of_week_weekend_color_highlight;
                else
                    color = R.color.day_of_week_weekend_color;

            } else {

                if (isToday)
                    color = R.color.day_of_week_color_highlight;
                else
                    color = R.color.day_of_week_color;
            }
            displayer.getDayOfWeekView().setTextColor(
                    ContextCompat.getColor( context, color) );
        }

        @Override
        public void onClick(View v) {
            CalendarRecyclerViewAdapter.this.clickListener.onShortClick(this.day);
        }

        @Override
        public boolean onLongClick(View v) {
            return CalendarRecyclerViewAdapter.this.clickListener.onLongClick(this.day);
        }
    }

    /** Simple "adapter" interface to manage clicks on days */
    public interface DayClickListener {

        /**
         * Is called when a list element is short-clicked.
         * @param day Data of the day which got clicked, or null if clicked element was the extend
         *            button.
         */
        void onShortClick(Day day);

        /**
         * Is called when a list element is long-clicked.
         * @param day Data of the day which got clicked, or null if clicked element was the extend
         *            button.
         * @return true, if click was consumed.
         */
        @SuppressWarnings("SameReturnValue")
        boolean onLongClick(Day day);
    }
}
