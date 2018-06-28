package de.kah2.mondtag.calendar;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;

import de.kah2.libZodiac.interpretation.Gardening;
import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.mondtag.R;

public class InterpreterMapper {

    private static LinkedList<InterpreterMapping> mappings;

    private static Context context;

    public static void init(Context context) {

        InterpreterMapper.context = context;

        mappings = new LinkedList<>();

        add(R.string.interpret_Gardening_CombatSlugs, Gardening.CombatSlugsInterpreter.class);
        add(R.string.interpret_Gardening_CutFruitTree, Gardening.CutFruitTreeInterpreter.class);
        add(R.string.interpret_Gardening_Cutting, Gardening.CuttingInterpreter.class);
        add(R.string.interpret_Gardening_Graft, Gardening.GraftInterpreter.class);
        add(R.string.interpret_Gardening_MowLawn, Gardening.MowLawnInterpreter.class);
        add(R.string.interpret_Gardening_OverterrestrialPests, Gardening.OverterrestrialPestsInterpreter.class);
        add(R.string.interpret_Gardening_SubterrestrialPests, Gardening.SubterrestrialPestsInterpreter.class);
        add(R.string.interpret_Gardening_TrimSick, Gardening.TrimSickInterpreter.class);
        add(R.string.interpret_Gardening_Water, Gardening.WaterInterpreter.class);
        add(R.string.interpret_Gardening_WeedDig, Gardening.WeedDigInterpreter.class);
        add(R.string.interpret_Gardening_SowPlant, Gardening.SowPlantInterpreter.class);

        // We can forget the context
        InterpreterMapper.context = null;

        Collections.sort(mappings);
    }

    private static void add(int id, Class<? extends Interpreter> interpreterClass) {
        mappings.add( new InterpreterMapping(id, context.getString(id), interpreterClass) );
    }

    static LinkedList<Integer> getKeys() {

        checkMappings();

        final LinkedList<Integer> keys = new LinkedList<>();

        for (InterpreterMapping mapping : mappings) {
            keys.add( mapping.id );
        }

        return keys;
    }

    static InterpreterMapping getMapping(int id) {

        checkMappings();

        for (InterpreterMapping mapping : mappings) {

            if (mapping.id == id) {
                return mapping;
            }
        }

        return null;
    }

    private static void checkMappings() {
        if (mappings == null) {
            throw new IllegalStateException( "No data - forgot to call init()?" );
        }
    }

    /**
     * This class is used to map an android string resource id to a Subclass of interpreter and
     * provides {@link #compareTo(InterpreterMapping)} to be able to order these mappings by
     * translated title.
     */
    public static class InterpreterMapping implements Comparable<InterpreterMapping>{

        private final int id;
        private final String i18n;
        private final Class<? extends Interpreter> interpreterClass;

        private InterpreterMapping(int id, String i18n, Class<? extends Interpreter> interpreterClass) {

            this.id = id;
            this.i18n = i18n;
            this.interpreterClass = interpreterClass;
        }

        /**
         * @return The string resource id
         */
        public int getId() {
            return id;
        }

        /**
         * @return The translated title
         */
        public String getI18n() {
            return i18n;
        }

        /**
         * @return the corresponding subclass of {@link de.kah2.libZodiac.interpretation.Interpreter}
         */
        public Class<? extends Interpreter> getInterpreterClass() {
            return interpreterClass;
        }

        @Override
        public int compareTo(@NonNull InterpreterMapping interpreterMapping) {
            return this.i18n.compareTo( interpreterMapping.i18n );
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof InterpreterMapping
                    && i18n.equals( ((InterpreterMapping) obj).i18n );
        }
    }
}
