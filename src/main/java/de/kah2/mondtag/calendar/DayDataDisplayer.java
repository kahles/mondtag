package de.kah2.mondtag.calendar;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.LinkedList;

import de.kah2.zodiac.libZodiac4A.Day;
import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.R;

/**
 * This class is used to capsule basic day-display functionality used by
 * {@link DayRecyclerViewAdapter.Item} and
 * {@link DayDetailFragment}.
 * Created by kahles on 21.03.17.
 */
class DayDataDisplayer {

    private final View dayView;

    /** Exists only in normal day-layout! */
    private TextView dateView;
    /** Exists only in normal day-layout! */
    private TextView dayOfWeekView;

    private final TextView solarRiseTextField;
    private final TextView solarSetTextView;
    private final ImageView lunarRiseSetFirstIcon;
    private final TextView lunarRiseSetFirstTextView;
    private final ImageView lunarRiseSetSecondIcon;
    private final TextView lunarRiseSetSecondDescription;
    private final TextView lunarRiseSetSecondTextView;
    private final ImageView lunarPhaseIcon;
    private final TextView lunarPhaseText;
    private final ImageView zodiacSignIcon;
    private final TextView  zodiacSignText;
    private final ImageView zodiacDirectionIcon;
    private final TextView zodiacDirectionText;
    private final ImageView zodiacElementIcon;
    private final TextView zodiacElementText;
    private final ImageView interpretationIcon;
    private final TextView interpretationAnnotationTextView;

    private boolean isDayDetailView;

    DayDataDisplayer(View dayView) {
        this.dayView = dayView;

        this.solarRiseTextField = dayView.findViewById(R.id.sun_rise_text);
        this.solarSetTextView = dayView.findViewById(R.id.sun_set_text);

        this.lunarRiseSetFirstIcon = dayView.findViewById(R.id.lunar_rise_set_first_icon);
        this.lunarRiseSetFirstTextView =
                dayView.findViewById(R.id.lunar_rise_set_first_text);
        this.lunarRiseSetSecondIcon = dayView.findViewById(R.id.lunar_rise_set_second_icon);
        this.lunarRiseSetSecondDescription = dayView.findViewById(
                R.id.lunar_rise_set_second_description);
        this.lunarRiseSetSecondTextView =
                dayView.findViewById(R.id.lunar_rise_set_second_text);

        this.lunarPhaseIcon = dayView.findViewById(R.id.lunar_phase_icon);
        this.lunarPhaseText = dayView.findViewById(R.id.lunar_phase_text);
        this.zodiacSignIcon = dayView.findViewById(R.id.zodiac_sign_icon);
        this.zodiacSignText = dayView.findViewById(R.id.zodiac_sign_text);
        this.zodiacDirectionIcon = dayView.findViewById(R.id.zodiac_direction_icon);
        this.zodiacDirectionText = dayView.findViewById(R.id.zodiac_direction_text);
        this.zodiacElementIcon = dayView.findViewById(R.id.zodiac_element_icon);
        this.zodiacElementText = dayView.findViewById(R.id.zodiac_element_text);
        this.interpretationIcon = dayView.findViewById(R.id.interpretation_icon);
        this.interpretationAnnotationTextView =
                dayView.findViewById(R.id.interpretation_annotation_text);
    }

    /**
     * This method fills in the values:
     * @param day The day object containing the data to display
     * @param isDayDetailView must be true if used in {@link DayDetailFragment}, false if used in
     *                        normal {@link CalendarFragment}
     */
    void setDayData(final Day day, boolean isDayDetailView) {

        this.isDayDetailView = isDayDetailView;

        this.initDateFields(day, isDayDetailView);

        this.initSolarRiseSetFields(day);

        this.initLunarRiseSetFields(day);

        if (day.getPlanetaryData().getLunarPhase() != null) {
            this.initFields(day.getPlanetaryData().getLunarPhase(), lunarPhaseIcon, lunarPhaseText);
        }

        this.initFields(day.getZodiacData().getDirection(), zodiacDirectionIcon, zodiacDirectionText);
        this.initFields(day.getZodiacData().getSign(), zodiacSignIcon, zodiacSignText);
        this.initFields(day.getZodiacData().getElement(), zodiacElementIcon, zodiacElementText);

        if (isDayDetailView) {

            this.initAllInterpretersList(day);

        } else {

            this.initSelectedInterpretationFields(day);
        }
    }

    private void initDateFields(Day day, boolean isDayDetailView) {

        // In DayDetailFragment this is shown in title
        if (!isDayDetailView) {

            this.dayOfWeekView = this.dayView.findViewById(R.id.day_of_week_text);
            this.dayOfWeekView.setText(ResourceMapper.formatDayOfWeek(day.getDate()));

            this.dateView = this.dayView.findViewById(R.id.date_text);
            this.dateView.setText(ResourceMapper.formatDate(day.getDate()));
        }
    }

    private void initSolarRiseSetFields(Day day) {

        this.solarRiseTextField.setText(
                ResourceMapper.formatTime( getContext(),
                        day.getPlanetaryData().getSolarRiseSet().getRise() ) );
        this.solarSetTextView.setText(
                ResourceMapper.formatTime( getContext(),
                        day.getPlanetaryData().getSolarRiseSet().getSet() ) );
    }

    private void initLunarRiseSetFields(Day day) {

        final Context context = this.getContext();

        final LocalDateTime rise = day.getPlanetaryData().getLunarRiseSet().getRise();
        final String localRise = ResourceMapper.formatTime(context, rise);

        final LocalDateTime set = day.getPlanetaryData().getLunarRiseSet().getSet();
        final String localSet = ResourceMapper.formatTime(context, set);

        // We can have four cases:
        if ( !day.getDate().isEqual(LocalDate.from(rise)) ) {

            // 1. No rise
            this.lunarRiseSetFirstIcon.setImageResource(R.drawable.lunar_set);
            this.lunarRiseSetFirstIcon.setContentDescription(getContext().getString(R.string.description_lunar_set));
            this.lunarRiseSetFirstTextView.setText(localSet);

            this.disableSecondLunarFields();

        } else if (!day.getDate().isEqual(LocalDate.from(set))) {

            // 2. No set
            this.lunarRiseSetFirstTextView.setText(localRise);
            this.lunarRiseSetFirstIcon.setImageResource(R.drawable.lunar_rise);
            this.lunarRiseSetFirstIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_rise));

            this.disableSecondLunarFields();

        } else if (rise.isBefore(set)) {

            // 3. First rise, then set
            this.lunarRiseSetFirstTextView.setText(localRise);
            this.lunarRiseSetFirstIcon.setImageResource(R.drawable.lunar_rise);
            this.lunarRiseSetFirstIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_rise));

            this.lunarRiseSetSecondTextView.setText(localSet);
            this.lunarRiseSetSecondIcon.setImageResource(R.drawable.lunar_set);
            this.lunarRiseSetSecondIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_set));

        } else {

            // 4. Set is before rise
            this.lunarRiseSetFirstIcon.setImageResource(R.drawable.lunar_set);
            this.lunarRiseSetFirstIcon.setContentDescription(
                    getContext().getString(R.string.description_lunar_set));
            this.lunarRiseSetFirstTextView.setText(localSet);

            this.lunarRiseSetSecondIcon.setImageResource(R.drawable.lunar_rise);
            this.lunarRiseSetSecondIcon.setContentDescription(getContext().getString(R.string.description_lunar_rise));
            this.lunarRiseSetSecondTextView.setText(localRise);
        }
    }

    private void disableSecondLunarFields() {

        if (isDayDetailView) {

            this.lunarRiseSetSecondIcon.setVisibility(View.GONE);
            this.lunarRiseSetSecondDescription.setVisibility(View.GONE);
            this.lunarRiseSetSecondTextView.setVisibility(View.GONE);

        } else {

            this.lunarRiseSetSecondIcon.setImageResource(0);
            this.lunarRiseSetSecondIcon.setContentDescription("");
            this.lunarRiseSetSecondTextView.setText("");
        }
    }

    private void initFields(Enum<?> e, ImageView imageView, TextView textView) {
        final Integer[] ids = ResourceMapper.getResourceIds(e);
        final String text = getContext().getString(ids[ResourceMapper.INDEX_STRING]);
        imageView.setImageResource(ids[ResourceMapper.INDEX_IMAGE]);
        imageView.setContentDescription(text);
        if (textView != null) textView.setText(text);
    }

    private void initSelectedInterpretationFields(Day day) {

        int qualityIcon = 0;
        String qualityText = "";
        String annotations = "";

        final MappedInterpreter interpreter =
                ((Mondtag) getContext().getApplicationContext()).getDataManager()
                        .getSelectedInterpreter();

        if (interpreter != null) {

            interpreter.interpret(day, getContext());

            qualityIcon = interpreter.getQualityIcon();
            qualityText = interpreter.getQualityText();
            annotations = interpreter.getAnnotations();
        }

        // Finally assign Strings to fields
        this.interpretationIcon.setImageResource( qualityIcon );
        this.interpretationIcon.setContentDescription( qualityText );

        this.interpretationAnnotationTextView.setText( annotations );
    }

    private void initAllInterpretersList(Day day) {

        final LinkedList<MappedInterpreter> interpreters =
                InterpreterManager.getAllInterpretations(day, getContext());

        final RecyclerView view = this.dayView.findViewById(R.id.interpretation_list);

        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager( getContext() );
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(linearLayoutManager);

        final DayDetailInterpretationListAdapter adapter =
                new DayDetailInterpretationListAdapter(interpreters);
        view.setAdapter(adapter);
    }

    /** Simple delegate to get a {@link Context}-object from the contained {@link View}. */
    Context getContext() {
        return this.dayView.getContext();
    }

    /**
     * Shortcut do get the subview of the contained View, that we don't have to handle with ids
     * outside of this class. Used for highlighting today or weekends.
     * <strong>May throw {@link NullPointerException}, when used on a view without date fields!
     * (e.g. {@link DayDetailFragment})</strong>
     */
    TextView getDayOfWeekView() {
        return dayOfWeekView;
    }
}
