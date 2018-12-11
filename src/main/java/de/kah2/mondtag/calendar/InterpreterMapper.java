package de.kah2.mondtag.calendar;

import android.content.Context;

import java.util.Collections;
import java.util.LinkedList;

import de.kah2.libZodiac.Day;
import de.kah2.libZodiac.interpretation.Gardening;
import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.mondtag.R;

/**
 * This class manages the {@link InterpreterMapping}s to map String-IDs to {@link Interpreter}s.
 * Static method {@link #init(Context)} must be called at app start to initialize these mappings
 * using the android resource-IDs.
 */
public class InterpreterMapper {

    private static LinkedList<InterpreterMapping> mappings;

    public static void init(Context context) {

        mappings = new LinkedList<>();

        add(context, R.string.interpret_Gardening_CombatPests, Gardening.CombatPestsInterpreter.class);
        add(context, R.string.interpret_Gardening_CuttingTransplant, Gardening.CuttingTransplantInterpreter.class);
        add(context, R.string.interpret_Gardening_Fertilize, Gardening.FertilizeInterpreter.class);
        add(context, R.string.interpret_Gardening_Graft, Gardening.GraftInterpreter.class);
        add(context, R.string.interpret_Gardening_Harvest, Gardening.HarvestInterpreter.class);
        add(context, R.string.interpret_Gardening_MowLawn, Gardening.MowLawnInterpreter.class);
        add(context, R.string.interpret_Gardening_SowPlant, Gardening.SowPlantInterpreter.class);
        add(context, R.string.interpret_Gardening_Trim, Gardening.TrimInterpreter.class);
        add(context, R.string.interpret_Gardening_Water, Gardening.WaterInterpreter.class);
        add(context, R.string.interpret_Gardening_WeedControl, Gardening.WeedControlInterpreter.class);

        Collections.sort(mappings, new InterpreterMapping.NameComparator());
    }

    private static void add(Context context, int id, Class<? extends Interpreter> interpreterClass) {
        mappings.add( new InterpreterMapping(id, context.getString(id), interpreterClass) );
    }

    static LinkedList<Integer> getKeys() {

        checkMappings();

        final LinkedList<Integer> keys = new LinkedList<>();

        for (InterpreterMapping mapping : mappings) {
            keys.add( mapping.getId() );
        }

        return keys;
    }

    /** @return a clone of the mapping to prevent external modification */
    static InterpreterMapping getMapping(int id) {

        checkMappings();

        for (InterpreterMapping mapping : mappings) {

            if (mapping.getId() == id) {

                // return a copy to prevent modification of the original
                return new InterpreterMapping( mapping );
            }
        }

        return null;
    }

    /**
     * Uses all InterpreterMappings and returns a list of interpretations that aren't neutral sorted
     * by quality.
     * Used to show all interpretations id {@link DayDetailFragment}.
     * @see de.kah2.mondtag.calendar.InterpreterMapping.QualityComparator
     */
    static LinkedList<InterpreterMapping> getInterpretedMappings(Day day, Context context) {

        checkMappings();

        final LinkedList<InterpreterMapping> results = new LinkedList<>();

        for (InterpreterMapping mapping : mappings) {

            final InterpreterMapping clone = new InterpreterMapping( mapping );
            clone.interpret(day, context);

            if (!clone.isQualityNeutral()) {
                results.addLast(clone);
            }
        }

        Collections.sort(results, new InterpreterMapping.QualityComparator());

        return results;
    }

    private static void checkMappings() {
        if (mappings == null) {
            throw new IllegalStateException( "No data - forgot to call init()?" );
        }
    }
}
