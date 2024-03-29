package de.kah2.mondtag.calendar;

import android.content.Context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.kah2.mondtag.R;
import de.kah2.zodiac.libZodiac4A.Day;
import de.kah2.zodiac.libZodiac4A.interpretation.Gardening;
import de.kah2.zodiac.libZodiac4A.interpretation.Interpreter;

/**
 * This class manages the {@link MappedInterpreter}s to map String-IDs to {@link Interpreter}s.
 * Static method {@link #init(Context)} must be called at app start to initialize these interpreters
 * using the android resource-IDs.
 */
public class InterpreterManager {

    private static LinkedList<MappedInterpreter> interpreters;

    public static void init(Context context) {

        interpreters = new LinkedList<>();

        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_CombatPests, context.getString(R.string.interpret_Gardening_CombatPests), Gardening.CombatPestsInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_CuttingTransplant, context.getString(R.string.interpret_Gardening_CuttingTransplant), Gardening.CuttingTransplantInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_Fertilize, context.getString(R.string.interpret_Gardening_Fertilize), Gardening.FertilizeInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_Graft, context.getString(R.string.interpret_Gardening_Graft), Gardening.GraftInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_Harvest, context.getString(R.string.interpret_Gardening_Harvest), Gardening.HarvestInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_MowLawn, context.getString(R.string.interpret_Gardening_MowLawn), Gardening.MowLawnInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_SowPlant, context.getString(R.string.interpret_Gardening_SowPlant), Gardening.SowPlantInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_Trim, context.getString(R.string.interpret_Gardening_Trim), Gardening.TrimInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_Water, context.getString(R.string.interpret_Gardening_Water), Gardening.WaterInterpreter.class) );
        interpreters.add( new MappedInterpreter(R.string.interpret_Gardening_WeedControl, context.getString(R.string.interpret_Gardening_WeedControl), Gardening.WeedControlInterpreter.class) );

        Collections.sort(interpreters, new MappedInterpreter.NameComparator());
    }

    static List<Integer> getIds() {

        checkInterpreters();

        final LinkedList<Integer> keys = new LinkedList<>();

        for (MappedInterpreter mapping : interpreters) {
            keys.add( mapping.getId() );
        }

        return keys;
    }

    /**
     * @return a clone of the mapping to prevent external modification or null when there's no
     * interpreter with the given id
     */
    static MappedInterpreter getInterpreter(int id) {

        checkInterpreters();

        for (MappedInterpreter mapping : interpreters) {

            if (mapping.getId() == id) {

                // return a copy to prevent modification of the original
                return new MappedInterpreter( mapping );
            }
        }

        return null;
    }

    /**
     * Uses copies of all InterpreterMappings and returns a list of interpretations that aren't
     * neutral sorted by quality.
     * Used to show all interpretations id {@link DayDetailFragment}.
     * @see MappedInterpreter.QualityComparator
     */
    static LinkedList<MappedInterpreter> getAllInterpretations(Day day, Context context) {

        checkInterpreters();

        final LinkedList<MappedInterpreter> results = new LinkedList<>();

        for (MappedInterpreter interpreter : interpreters) {

            final MappedInterpreter clone = new MappedInterpreter( interpreter );
            clone.interpret(day, context);

            if (!clone.isQualityNeutral()) {
                results.addLast(clone);
            }
        }

        Collections.sort(results, new MappedInterpreter.QualityComparator());

        return results;
    }

    private static void checkInterpreters() {
        if (interpreters == null) {
            throw new IllegalStateException( "No data - forgot to call init()?" );
        }
    }
}
