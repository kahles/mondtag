package de.kah2.mondtag.calendar;

import android.content.Context;
import android.text.format.DateFormat;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.format.TextStyle;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;

import de.kah2.libZodiac.ProgressListener;
import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.libZodiac.planetary.LunarPhase;
import de.kah2.libZodiac.zodiac.ZodiacDirection;
import de.kah2.libZodiac.zodiac.ZodiacElement;
import de.kah2.libZodiac.zodiac.ZodiacSign;
import de.kah2.mondtag.R;

/**
 * This class is used to map keys of libZodiac to string- or image-resources, and formats dates and
 * times.
 *
 * Created by kahles on 11.11.16.
 */

public class ResourceMapper {

    public final static int INDEX_IMAGE = 0;
    public final static int INDEX_STRING = 1;

    private final static Locale LOCALE = Locale.getDefault();

    private final static Hashtable<String, Integer[]> mappings = new Hashtable<>();

    static {
        // data fetching activities
        mappings.put(ProgressListener.State.IMPORTING.toString(),
                new Integer[]{-1, R.string.status_importing});
        mappings.put(ProgressListener.State.GENERATING.toString(),
                new Integer[]{-1, R.string.status_generating});
        mappings.put(ProgressListener.State.EXTENDING_PAST.toString(),
                new Integer[]{-1, R.string.status_extending});
        mappings.put(ProgressListener.State.EXTENDING_FUTURE.toString(),
                new Integer[]{-1, R.string.status_extending});
        mappings.put(ProgressListener.State.COUNTING.toString(),
                new Integer[]{-1, R.string.status_counting});

        // Lunar phases
        mappings.put(LunarPhase.FULL_MOON.toString(),
                new Integer[]{R.drawable.moon_full, R.string.full_moon});
        mappings.put(LunarPhase.INCREASING.toString(),
                new Integer[]{R.drawable.moon_waxing, R.string.increasing});
        mappings.put(LunarPhase.DECREASING.toString(),
                new Integer[]{R.drawable.moon_waning, R.string.decreasing});
        mappings.put(LunarPhase.NEW_MOON.toString(),
                new Integer[]{R.drawable.moon_new, R.string.new_moon});

        // Zodiac directions
        mappings.put(ZodiacDirection.ASCENDING.toString(),
                new Integer[]{R.drawable.moon_ascending, R.string.ascending});
        mappings.put(ZodiacDirection.DESCENDING.toString(),
                new Integer[]{R.drawable.moon_descending, R.string.descending});

        // Zodiac signs
        mappings.put(ZodiacSign.AQUARIUS.toString(),
                new Integer[]{R.drawable.aquarius, R.string.aquarius});
        mappings.put(ZodiacSign.ARIES.toString(),
                new Integer[]{R.drawable.aries, R.string.aries});
        mappings.put(ZodiacSign.CANCER.toString(),
                new Integer[]{R.drawable.cancer, R.string.cancer});
        mappings.put(ZodiacSign.CAPRICORN.toString(),
                new Integer[]{R.drawable.capricorn, R.string.capricorn});
        mappings.put(ZodiacSign.GEMINI.toString(),
                new Integer[]{R.drawable.gemini, R.string.gemini});
        mappings.put(ZodiacSign.LEO.toString(),
                new Integer[]{R.drawable.leo, R.string.leo});
        mappings.put(ZodiacSign.LIBRA.toString(),
                new Integer[]{R.drawable.libra, R.string.libra});
        mappings.put(ZodiacSign.PISCES.toString(),
                new Integer[]{R.drawable.pisces, R.string.pisces});
        mappings.put(ZodiacSign.SAGITTARIUS.toString(),
                new Integer[]{R.drawable.sagittarius, R.string.sagittarius});
        mappings.put(ZodiacSign.SCORPIO.toString(),
                new Integer[]{R.drawable.scorpio, R.string.scorpio});
        mappings.put(ZodiacSign.TAURUS.toString(),
                new Integer[]{R.drawable.taurus, R.string.taurus});
        mappings.put(ZodiacSign.VIRGO.toString(),
                new Integer[]{R.drawable.virgo, R.string.virgo});

        // Zodiac elements
        mappings.put(ZodiacElement.AIR.toString(),
                new Integer[]{R.drawable.air, R.string.air});
        mappings.put(ZodiacElement.EARTH.toString(),
                new Integer[]{R.drawable.earth, R.string.earth});
        mappings.put(ZodiacElement.FIRE.toString(),
                new Integer[]{R.drawable.fire, R.string.fire});
        mappings.put(ZodiacElement.WATER.toString(),
                new Integer[]{R.drawable.water, R.string.water});

        // Interpretation qualities
        mappings.put(Interpreter.Quality.WORST.toString(),
                new Integer[]{R.drawable.quality_worst, R.string.interpretation_worst});
        mappings.put(Interpreter.Quality.BAD.toString(),
                new Integer[]{R.drawable.quality_bad, R.string.interpretation_bad});
        mappings.put(Interpreter.Quality.GOOD.toString(),
                new Integer[]{R.drawable.quality_good, R.string.interpretation_good});
        mappings.put(Interpreter.Quality.BEST.toString(),
                new Integer[]{R.drawable.quality_best, R.string.interpretation_best});
    }

    public static Integer[] getResourceIds(Enum<?> e) {
        return mappings.get(e.toString());
    }

    public static String formatDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE);
    }

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(LOCALE);
        return date.format( formatter );
    }

    public static String formatTime(Context context, LocalDateTime date) {
        /* Ignores 24h-format  - see https://github.com/JakeWharton/ThreeTenABP/issues/16 */
        /*LocalTime time = LocalTime.from(date).truncatedTo(ChronoUnit.MINUTES);
        return time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));*/
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeFormat(context);
        DateTimeFormatter format = DateTimeFormatter.ofPattern(sdf.toPattern());
        return format.format(date);

    }
}
