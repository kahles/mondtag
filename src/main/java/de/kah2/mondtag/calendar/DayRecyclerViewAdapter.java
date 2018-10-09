package de.kah2.mondtag.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class DayRecyclerViewAdapter extends RecyclerView.Adapter<DayRecyclerViewAdapter.Item> {

    private final ArrayList<Day> days;

    private LocalDate today;

    private ClickListener clickListener;

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
        View inflatedView = LayoutInflater.from(parent.getContext())
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

    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * This is the sub-element of our {@link RecyclerView} to manage the days and the button to
     * extend data.
     */
    class Item extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        private final View itemView;

        private Day day;

        Item(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        /**
         * If a day is passed as argument its data will be displayed using
         * {@link DayDataDisplayer}.
         */
        void bindElement(Day day) {
            this.day = day;

            if (day == null) {

                // the actual list element is the extend button
                itemView.findViewById(R.id.buttonExtendData).setOnClickListener((view)->{

                    Log.d(Item.class.getSimpleName(),
                            "clickListener: extendButton clicked");

                    ((MondtagActivity) view.getContext()).extendFuture();

                });

            } else {

                this.itemView.setOnClickListener(this);
                this.itemView.setOnLongClickListener(this);

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
            final ClickListener listener = DayRecyclerViewAdapter.this.clickListener;
            if (listener != null) {
                listener.onShortClick(this.day);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final ClickListener listener = DayRecyclerViewAdapter.this.clickListener;
            if (listener != null) {
                return listener.onLongClick(this.day);
            } else {
                return false;
            }
        }
    }

    /** Simple "adapter" interface to manage clicks on days */
    public interface ClickListener {

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