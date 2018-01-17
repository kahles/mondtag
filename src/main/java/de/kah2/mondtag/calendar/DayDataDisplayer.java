package de.kah2.mondtag.calendar;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import de.kah2.libZodiac.Day;
import de.kah2.mondtag.R;

/**
 * This class is used to capsule basic day-display functionality used by
 * {@link CalendarRecyclerViewAdapter.Element} and
 * {@link DayDetailFragment}.
 * Created by kahles on 21.03.17.
 */
class DayDataDisplayer {

    private final View dayView;

    private final TextView dayOfWeekView;
    private final TextView dateView;
    private final TextView solarRiseTextField;
    private final TextView solarSetTextView;
    private final ImageView lunarRiseSetFirstIcon;
    private final TextView lunarRiseSetFirstTextView;
    private final ImageView lunarRiseSetSecondIcon;
    private final TextView lunarRiseSetSecondTextView;
    private final ImageView lunarPhaseIcon;
    private final TextView lunarPhaseText;
    private final ImageView zodiacSignIcon;
    private final TextView  zodiacSignText;
    private final ImageView zodiacDirectionIcon;
    private final TextView zodiacDirectionText;
    private final ImageView zodiacElementIcon;
    private final TextView zodiacElementText;

    public DayDataDisplayer(View dayView) {
        this.dayView = dayView;

        this.dayOfWeekView = dayView.findViewById(R.id.dayOfWeekText);
        this.dateView = dayView.findViewById(R.id.dateText);

        this.solarRiseTextField = dayView.findViewById(R.id.sunRiseText);
        this.solarSetTextView = dayView.findViewById(R.id.sunSetText);

        this.lunarRiseSetFirstIcon = dayView.findViewById(R.id.lunarRiseSetFirstIcon);
        this.lunarRiseSetFirstTextView =
                dayView.findViewById(R.id.lunarRiseSetFirstText);
        this.lunarRiseSetSecondIcon = dayView.findViewById(R.id.lunarRiseSetSecondIcon);
        this.lunarRiseSetSecondTextView =
                dayView.findViewById(R.id.lunarRiseSetSecondText);

        this.lunarPhaseIcon = dayView.findViewById(R.id.lunarPhaseIcon);
        this.lunarPhaseText = dayView.findViewById(R.id.lunarPhaseText);
        this.zodiacSignIcon = dayView.findViewById(R.id.zodiacSignIcon);
        this.zodiacSignText = dayView.findViewById(R.id.zodiacSignText);
        this.zodiacDirectionIcon = dayView.findViewById(R.id.zodiacDirectionIcon);
        this.zodiacDirectionText = dayView.findViewById(R.id.zodiacDirectionText);
        this.zodiacElementIcon = dayView.findViewById(R.id.zodiacElementIcon);
        this.zodiacElementText = dayView.findViewById(R.id.zodiacElementText);
    }

    /** This method fills in the values of a {@link Day}. */
    void setDayData(final Day day) {
        dayOfWeekView.setText( ResourceMapper.formatDayOfWeek(day.getDate()) );

        dateView.setText( ResourceMapper.formatDate(day.getDate()) );

        solarRiseTextField.setText(
                ResourceMapper.formatTime( getContext(),
                        day.getPlanetaryData().getSolarRiseSet().getRise() ) );
        solarSetTextView.setText(
                ResourceMapper.formatTime( getContext(),
                        day.getPlanetaryData().getSolarRiseSet().getSet() ) );

        this.setLunarRiseSet(day);

        Integer[] ids;
        String text;

        if (day.getPlanetaryData().getLunarPhase() != null) {
            ids = ResourceMapper.getResourceIds(day.getPlanetaryData().getLunarPhase());
            text = getContext().getString(ids[ResourceMapper.INDEX_STRING]);
            lunarPhaseIcon.setImageResource(ids[ResourceMapper.INDEX_IMAGE]);
            lunarPhaseIcon.setContentDescription(text);
            lunarPhaseText.setText(text);
        }

        ids = ResourceMapper.getResourceIds( day.getZodiacData().getDirection() );
        text = getContext().getString(ids[ResourceMapper.INDEX_STRING]);
        zodiacDirectionIcon.setImageResource(ids[ResourceMapper.INDEX_IMAGE]);
        zodiacDirectionIcon.setContentDescription(text);
        zodiacDirectionText.setText(text);

        ids = ResourceMapper.getResourceIds( day.getZodiacData().getSign() );
        text = getContext().getString(ids[ResourceMapper.INDEX_STRING]);
        zodiacSignIcon.setImageResource(ids[ResourceMapper.INDEX_IMAGE]);
        zodiacSignIcon.setContentDescription(text);
        zodiacSignText.setText(text);

        ids = ResourceMapper.getResourceIds( day.getZodiacData().getElement() );
        text = getContext().getString(ids[ResourceMapper.INDEX_STRING]);
        zodiacElementIcon.setImageResource(ids[ResourceMapper.INDEX_IMAGE]);
        zodiacElementIcon.setContentDescription(text);
        zodiacElementText.setText(text);
    }

    private void setLunarRiseSet(Day day) {
        final LocalDateTime rise = day.getPlanetaryData().getLunarRiseSet().getRise();
        final LocalDateTime set = day.getPlanetaryData().getLunarRiseSet().getSet();

        final Context context = this.getContext();

        if ( !day.getDate().isEqual(LocalDate.from(rise)) ) {

            // No rise today
            this.lunarRiseSetFirstIcon.setImageResource(R.drawable.lunar_set);
            this.lunarRiseSetFirstIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_set));
            this.lunarRiseSetFirstTextView.setText( ResourceMapper.formatTime(context, set) );
            this.lunarRiseSetSecondIcon.setImageResource(0);
            this.lunarRiseSetSecondIcon.setContentDescription("");
            this.lunarRiseSetSecondTextView.setText("");

        } else if (!day.getDate().isEqual(LocalDate.from(set))) {

            // No set today
            this.lunarRiseSetFirstTextView.setText(ResourceMapper.formatTime(context, rise));
            this.lunarRiseSetSecondIcon.setImageResource(0);
            this.lunarRiseSetSecondIcon.setContentDescription("");
            this.lunarRiseSetSecondTextView.setText("");

        } else if (rise.isBefore(set)) {

            // Everything normal - just set times
            this.lunarRiseSetFirstTextView.setText(ResourceMapper.formatTime(context, rise));
            this.lunarRiseSetSecondTextView.setText(ResourceMapper.formatTime(context, set));

        } else {

            // Set is before rise
            this.lunarRiseSetFirstIcon.setImageResource(R.drawable.lunar_set);
            this.lunarRiseSetFirstIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_set));
            this.lunarRiseSetFirstTextView.setText(ResourceMapper.formatTime(context, set));
            this.lunarRiseSetSecondIcon.setImageResource(R.drawable.lunar_rise);
            this.lunarRiseSetSecondIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_rise));
            this.lunarRiseSetSecondTextView.setText(ResourceMapper.formatTime(context, rise));
        }
    }

    /** Simple delegate to get a {@link Context}-object from the contained {@link View}. */
    Context getContext() {
        return this.dayView.getContext();
    }

    /**
     * Shortcut do get the subview of the contained View, that we don't have to handle with ids
     * outside of this class.
     */
    TextView getDayOfWeekView() {
        return dayOfWeekView;
    }
}
