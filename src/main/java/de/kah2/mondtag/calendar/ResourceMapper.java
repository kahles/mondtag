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

import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.DataManager;
import de.kah2.zodiac.libZodiac4A.ProgressListener;
import de.kah2.zodiac.libZodiac4A.interpretation.Gardening;
import de.kah2.zodiac.libZodiac4A.interpretation.Interpreter;
import de.kah2.zodiac.libZodiac4A.planetary.LunarPhase;
import de.kah2.zodiac.libZodiac4A.zodiac.ZodiacDirection;
import de.kah2.zodiac.libZodiac4A.zodiac.ZodiacElement;
import de.kah2.zodiac.libZodiac4A.zodiac.ZodiacSign;

/**
 * This class is used to map keys of libZodiac to string- or image-resources, and formats dates and
 * times.
 *
 * Created by kahles on 11.11.16.
 */

public class ResourceMapper {

    public final static int INDEX_IMAGE = 0;
    public final static int INDEX_STRING = 1;

    private final static Hashtable<String, Integer[]> mappings = new Hashtable<>();

    static {
        // data fetching activities
        putString(ProgressListener.State.IMPORTING, R.string.status_importing);
        putString(ProgressListener.State.GENERATING, R.string.status_generating);
        putString(ProgressListener.State.EXTENDING_PAST, R.string.status_extending);
        putString(ProgressListener.State.EXTENDING_FUTURE, R.string.status_extending);
        putString(ProgressListener.State.COUNTING, R.string.status_counting);

        // Lunar phases
        putAll(LunarPhase.FULL_MOON, R.drawable.moon_full, R.string.full_moon);
        putAll(LunarPhase.INCREASING, R.drawable.moon_waxing, R.string.increasing);
        putAll(LunarPhase.DECREASING, R.drawable.moon_waning, R.string.decreasing);
        putAll(LunarPhase.NEW_MOON, R.drawable.moon_new, R.string.new_moon);

        // Zodiac directions
        putAll(ZodiacDirection.ASCENDING, R.drawable.moon_ascending, R.string.ascending);
        putAll(ZodiacDirection.DESCENDING, R.drawable.moon_descending, R.string.descending);

        // Zodiac signs
        putAll(ZodiacSign.AQUARIUS, R.drawable.aquarius, R.string.aquarius);
        putAll(ZodiacSign.ARIES, R.drawable.aries, R.string.aries);
        putAll(ZodiacSign.CANCER, R.drawable.cancer, R.string.cancer);
        putAll(ZodiacSign.CAPRICORN, R.drawable.capricorn, R.string.capricorn);
        putAll(ZodiacSign.GEMINI, R.drawable.gemini, R.string.gemini);
        putAll(ZodiacSign.LEO, R.drawable.leo, R.string.leo);
        putAll(ZodiacSign.LIBRA, R.drawable.libra, R.string.libra);
        putAll(ZodiacSign.PISCES, R.drawable.pisces, R.string.pisces);
        putAll(ZodiacSign.SAGITTARIUS, R.drawable.sagittarius, R.string.sagittarius);
        putAll(ZodiacSign.SCORPIO, R.drawable.scorpio, R.string.scorpio);
        putAll(ZodiacSign.TAURUS, R.drawable.taurus, R.string.taurus);
        putAll(ZodiacSign.VIRGO, R.drawable.virgo, R.string.virgo);

        // Zodiac elements
        putAll(ZodiacElement.AIR, R.drawable.air, R.string.air);
        putAll(ZodiacElement.EARTH, R.drawable.earth, R.string.earth);
        putAll(ZodiacElement.FIRE, R.drawable.fire, R.string.fire);
        putAll(ZodiacElement.WATER, R.drawable.water, R.string.water);

        // Interpretation qualities
        putAll(Interpreter.Quality.WORST, R.drawable.quality_worst, R.string.interpretation_worst);
        putAll(Interpreter.Quality.BAD, R.drawable.quality_bad, R.string.interpretation_bad);
        putAll(Interpreter.Quality.GOOD, R.drawable.quality_good, R.string.interpretation_good);
        putAll(Interpreter.Quality.BEST, R.drawable.quality_best, R.string.interpretation_best);


        // Interpreter annotations

        // Gardening

		putString( Gardening.Plants.FLOWERS, R.string.interpret_gardening_plants_flowers );
		putString( Gardening.Plants.FRUIT_PLANTS, R.string.interpret_gardening_plants_fruit );
		putString( Gardening.Plants.LAWN, R.string.interpret_gardening_plants_lawn );
		putString( Gardening.Plants.LEAFY_VEGETABLES, R.string.interpret_gardening_plants_leafy );
		putString( Gardening.Plants.ROOT_VEGETABLES, R.string.interpret_gardening_plants_root );
		putString( Gardening.Plants.POTATOES, R.string.interpret_gardening_plants_potatoes );
		putString( Gardening.Plants.SALAD, R.string.interpret_gardening_plants_salad );

		putString( Gardening.HarvestInterpreter.Usage.TO_CONSERVE, R.string.interpret_gardening_harvest_conserve );
		putString( Gardening.HarvestInterpreter.Usage.TO_DRY, R.string.interpret_gardening_harvest_dry );
		putString( Gardening.HarvestInterpreter.Usage.CONSUME_IMMEDIATELY, R.string.interpret_gardening_harvest_consume );

		putString( Gardening.WeedControlInterpreter.Actions.DIG, R.string.interpret_gardening_dig );
		putString( Gardening.WeedControlInterpreter.Actions.WEED, R.string.interpret_gardening_weed);
		putString( Gardening.WeedControlInterpreter.Actions.WEED_BEFORE_NOON, R.string.interpret_gardening_weed_before_noon);

		putString( Gardening.TrimInterpreter.PlantCategory.FRUIT_TREES, R.string.interpret_gardening_trim_fruit );
		putString( Gardening.TrimInterpreter.PlantCategory.SICK_PLANTS, R.string.interpret_gardening_trim_sick );

		putString( Gardening.CombatPestsInterpreter.PestType.OVERTERRESTRIAL, R.string.interpret_gardening_combatpests_over );
		putString( Gardening.CombatPestsInterpreter.PestType.SUBTERRESTRIAL, R.string.interpret_gardening_combatpests_sub );
		putString( Gardening.CombatPestsInterpreter.PestType.SLUGS, R.string.interpret_gardening_combatpests_slugs );
    }

    /**
     * Returns the resource-ids belonging to an enum value.
     * @param key the key
     * @return an array where INDEX_STRING contains the string-id and INDEX_IMAGE the id of the icon
     */
    public static Integer[] getResourceIds(Enum<?> key) {
        return getResourceIds( key.toString() );
    }

    /**
     * Returns the resource-ids belonging to an enum value.
     * @param key the key
     * @return an array where INDEX_STRING contains the string-id and INDEX_IMAGE the id of the icon
     */
    public static Integer[] getResourceIds(String key) {
        return mappings.get(key);
    }

    /** Returns the translated day of the week of a date. */
    public static String formatDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, DataManager.getLocale());
    }

    /** Returns the formatted date string for a {@link LocalDate} */
    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(DataManager.getLocale());
        return date.format( formatter );
    }

    /** returns day of week and date */
    public static String formatLongDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                .withLocale(DataManager.getLocale());
        return date.format(formatter);
    }

    /** Returns the formatted time string of a {@link LocalDateTime}-object. */
    public static String formatTime(Context context, LocalDateTime date) {
        /* Ignores 24h-format  - see https://github.com/JakeWharton/ThreeTenABP/issues/16 */
        /*LocalTime time = LocalTime.from(date).truncatedTo(ChronoUnit.MINUTES);
        return time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));*/
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeFormat(context);
        DateTimeFormatter format = DateTimeFormatter.ofPattern(sdf.toPattern());
        return format.format(date);
    }

    private static void putString(Enum<?> key, int stringId) {
        putAll(key, -1, stringId);
    }

    private static void putAll(Enum<?> key, int drawableId, int stringId) {
        mappings.put( key.toString(),
                    new Integer[]{ drawableId, stringId } );
    }
}
