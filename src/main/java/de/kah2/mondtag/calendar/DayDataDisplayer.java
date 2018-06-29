package de.kah2.mondtag.calendar;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import de.kah2.libZodiac.Day;
import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.mondtag.R;

/**
 * This class is used to capsule basic day-display functionality used by
 * {@link CalendarRecyclerViewAdapter.Element} and
 * {@link DayDetailFragment}.
 * Created by kahles on 21.03.17.
 */
class DayDataDisplayer {

    private final static String TAG = DayDataDisplayer.class.getSimpleName();

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
    private final ImageView interpretationIcon;
    private final TextView interpretationNameView;
    private final TextView interpretationQualityTextView;
    private final TextView interpretationQualityCategoryTextView;

    DayDataDisplayer(View dayView) {
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
        this.interpretationIcon = dayView.findViewById(R.id.interpretationIcon);
        this.interpretationNameView = dayView.findViewById(R.id.interpretationName);
        this.interpretationQualityTextView = dayView.findViewById(R.id.interpretationQualityText);
        this.interpretationQualityCategoryTextView=
                dayView.findViewById(R.id.interpretationQualityCategoryText);
    }

    /**
     * This method fills in the values:
     * @param day The day object containing the data to display
     * @param isVerboseView if true, the views R.id.interpretationName and
     *                      R.id.interpretationQualityText are also set - if false, only the icon
     *                      and its description will be set.
     */
    void setDayData(final Day day, boolean isVerboseView) {
        dayOfWeekView.setText( ResourceMapper.formatDayOfWeek(day.getDate()) );

        dateView.setText( ResourceMapper.formatDate(day.getDate()) );

        solarRiseTextField.setText(
                ResourceMapper.formatTime( getContext(),
                        day.getPlanetaryData().getSolarRiseSet().getRise() ) );
        solarSetTextView.setText(
                ResourceMapper.formatTime( getContext(),
                        day.getPlanetaryData().getSolarRiseSet().getSet() ) );

        this.initLunarRiseSetFields(day);

        if (day.getPlanetaryData().getLunarPhase() != null) {
            this.initFields(day.getPlanetaryData().getLunarPhase(), lunarPhaseIcon, lunarPhaseText);
        }

        this.initFields(day.getZodiacData().getDirection(), zodiacDirectionIcon, zodiacDirectionText);
        this.initFields(day.getZodiacData().getSign(), zodiacSignIcon, zodiacSignText);
        this.initFields(day.getZodiacData().getElement(), zodiacElementIcon, zodiacElementText);

        initInterpretationFields(day, isVerboseView);
    }

    private void initInterpretationFields(Day day, boolean isVerboseView) {

        int qualityIcon = 0;

        // Use empty Strings as default, if no values are set
        String qualityText = "";
        String qualityCategory = "";
        String interpreterName = "";


        if ( day.getInterpreter() != null ) { // An Interpretation is set

            interpreterName = getTranslatedInterpreterString(day);

            final Interpreter.Quality quality = day.getInterpreter().getQuality();

            // If quality isn't neutral, we display it
            if (quality != Interpreter.Quality.NEUTRAL) {

                final Integer[] qualityStringIds = ResourceMapper.getResourceIds( quality );

                qualityIcon = qualityStringIds[ResourceMapper.INDEX_IMAGE];
                qualityText = getContext().getString(qualityStringIds[ResourceMapper.INDEX_STRING]);
            }

            // If qualityCategory isn't empty, we fetch the String
            if (day.getInterpreter().getCategory() != null) {

                qualityCategory = getContext().getString(
                        ResourceMapper.getResourceIds(
                                day.getInterpreter().getCategory()
                        )[ResourceMapper.INDEX_STRING] );
            }
        }

        // Finally assign Strings to fields
        this.interpretationIcon.setImageResource(qualityIcon);
        this.interpretationIcon.setContentDescription(qualityText);

        this.interpretationQualityCategoryTextView.setText(qualityCategory);

        if (isVerboseView) {
            // These fields we only have in fragment_day_detail
            this.interpretationQualityTextView.setText(qualityText);
            this.interpretationNameView.setText(interpreterName);
        }
    }

    private String getTranslatedInterpreterString(Day day) {

        String interpreterName;

        final String interpreterKey =
                ResourceMapper.createInterpreterKey( day.getInterpreter() );

        // Try to get a translated String for created key and throw an Exception if String is
        // missing
        try {

            final int interpreterStringId = R.string.class
                    .getDeclaredField(interpreterKey).getInt(null);
            interpreterName = getContext().getString(interpreterStringId);

        } catch (NoSuchFieldException e) {
            Log.e(TAG, "initInterpretationFields: String id not found for "
                    + interpreterKey);
            interpreterName = interpreterKey;
        } catch (IllegalAccessException e) {
            // If the field isn't accessible - this shouldn't happen :)
            e.printStackTrace();
            return "";
        }

        return interpreterName;
    }

    private void initLunarRiseSetFields(Day day) {
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

    private void initFields(Enum<?> e, ImageView imageView, TextView textView) {
        final Integer[] ids = ResourceMapper.getResourceIds(e);
        final String text = getContext().getString(ids[ResourceMapper.INDEX_STRING]);
        imageView.setImageResource(ids[ResourceMapper.INDEX_IMAGE]);
        imageView.setContentDescription(text);
        if (textView != null) textView.setText(text);
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
