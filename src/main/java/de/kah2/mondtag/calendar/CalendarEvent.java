package de.kah2.mondtag.calendar;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import de.kah2.mondtag.Mondtag;
import de.kah2.mondtag.R;
import de.kah2.zodiac.libZodiac4A.Day;

/**
 * This class is used to build an {@link Intent} to create a calendar event and fill it with data of
 * a {@link Day}.
 */
class CalendarEvent {

    private final static String TAG = CalendarEvent.class.getSimpleName();
    private final static int DEFAULT_TITLE = R.string.event_default_name;

    private final Context context;
    private final long dateInMs;
    private final static int EVENT_DURATION_IN_MS = 86399000;
    private final String description;
    private final int titleId;

    /** Instantiates the object and prepares the needed data. */
    CalendarEvent( final Context context, final Day day ) {
        this.context = context;
        this.dateInMs = this.getDateInMillis( day.getDate() );
        this.titleId = this.getTitleId();
        this.description = this.createDescription( day );
    }

    private int getTitleId() {
        final MappedInterpreter interpreter =
                ((Mondtag) this.context).getDataManager().getSelectedInterpreter();

        if (interpreter == null) {
            return DEFAULT_TITLE;
        } else {
            return interpreter.getId();
        }
    }

    /** Generates an {@link android.content.Intent} to start a calendar app and add the event. */
    Intent toIntent() {

        return new Intent( Intent.ACTION_INSERT )
                .setData( CalendarContract.Events.CONTENT_URI)
                .putExtra( CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        this.dateInMs)
                .putExtra( CalendarContract.EXTRA_EVENT_END_TIME,
                        this.dateInMs + EVENT_DURATION_IN_MS)
                .putExtra( CalendarContract.EXTRA_EVENT_ALL_DAY, true )
                .putExtra( CalendarContract.Events.TITLE,
                        context.getString(this.titleId) )
                .putExtra( CalendarContract.Events.DESCRIPTION, this.description )
                .putExtra( CalendarContract.Events.AVAILABILITY,
                        CalendarContract.Events.AVAILABILITY_FREE )
                .putExtra(CalendarContract.Events.HAS_ALARM, 0);
    }

    private long getDateInMillis(LocalDate date) {
        final ZoneId timeZoneId = ((Mondtag) context).getDataManager().getZoneId();

        final ZonedDateTime zonedDateTime = ZonedDateTime.of(
                date, LocalTime.MIDNIGHT, timeZoneId );

        return Instant.from(zonedDateTime).toEpochMilli();
    }

    private String createDescription(Day day) {
        return getDayDataString( R.string.description_lunar_phase, day.getPlanetaryData().getLunarPhase() )
                + getDayDataString( R.string.description_zodiac_sign, day.getZodiacData().getSign() )
                + getDayDataString( R.string.description_zodiac_direction, day.getZodiacData().getDirection() )
                + getDayDataString( R.string.description_zodiac_element, day.getZodiacData().getElement() );
    }

    private String getDayDataString(final int descriptionId, final Enum<?> key) {
        final int id = ResourceMapper.getResourceIds( key )[ResourceMapper.INDEX_STRING];
        return context.getString(descriptionId) + ": " + context.getString(id) + "\n";
    }
}
