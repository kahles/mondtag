package de.kah2.mondtag.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;

import de.kah2.mondtag.R;
import de.kah2.zodiac.libZodiac4A.Day;

/**
 * Displays a scrollable calendar.
 *
 * Created by kahles on 09.11.16.
 */
public class DayRecyclerViewAdapter extends RecyclerView.Adapter<DayRecyclerViewAdapter.Item> {

    private final ArrayList<Day> days;

    private LocalDate today;

    private DayClickListener clickListener;

    DayRecyclerViewAdapter() {
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

    @NonNull
    @Override
    public Item onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate( viewType , parent, false);

        return new Item(inflatedView);
    }

    public int getItemViewType(int position) {
        if (position < this.days.size()) {
            return R.layout.day_layout;
        } else {
            return R.layout.extend_button;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Item holder, int position) {
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
        // +1 because we add the extend-range-Button
        return days.size() + 1;
    }

    void setClickListener(DayClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * This is the sub-element of our {@link RecyclerView} to manage the days and the button to
     * extend data.
     */
    class Item extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        private Day day;

        Item(View itemView) {
            super(itemView);
        }

        /**
         * If a day is passed as argument its data will be displayed using
         * {@link DayDataDisplayer}.
         */
        void bindElement(Day day) {

            this.day = day;

            super.itemView.setOnClickListener(this);
            super.itemView.setOnLongClickListener(this);

            // if we have a day and not the "extend future"-buton
            if (day != null) {
                final DayDataDisplayer displayer = new DayDataDisplayer(itemView);
                displayer.setDayData(day, false);

                setDailyLayoutProperties(day, displayer);
            }
        }

        /** Manages highlighting of weekends and today */
        private void setDailyLayoutProperties(Day day, DayDataDisplayer displayer) {
            final DayOfWeek dayOfWeek = day.getDate().getDayOfWeek();

            final Context context = displayer.getContext();

            final boolean isToday = day.getDate().isEqual(DayRecyclerViewAdapter.this.today);

            // Elevate item, if it is TODAY

            if (isToday) {
                if (Build.VERSION.SDK_INT >= 21) {
                    this.itemView.setElevation(20);
                }
                this.itemView.setBackgroundColor(
                        ContextCompat.getColor( context, R.color.background_today) );

                displayer.getDayOfWeekView().setTypeface(null, Typeface.BOLD);

            } else {
                if (Build.VERSION.SDK_INT >= 21) {
                    this.itemView.setElevation(6);
                }
                this.itemView.setBackgroundColor(
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
            final DayClickListener listener = DayRecyclerViewAdapter.this.clickListener;
            if (listener != null) {
                listener.onShortClick(this.day);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final DayClickListener listener = DayRecyclerViewAdapter.this.clickListener;
            if (listener != null) {
                return listener.onLongClick(this.day);
            } else {
                return false;
            }
        }
    }
}