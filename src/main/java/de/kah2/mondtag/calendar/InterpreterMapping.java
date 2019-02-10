package de.kah2.mondtag.calendar;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Comparator;
import java.util.LinkedList;

import de.kah2.libZodiac.Day;
import de.kah2.libZodiac.interpretation.Interpreter;

/**
 * This is a helper class to do an interpretation for a {@link Day}-object and provide translated
 * Strings and Icons.
 */
public class InterpreterMapping {

    public static final String TAG = InterpreterMapping.class.getSimpleName();

    /** The android-string-resource-id */
    private final int interpreterNameStringId;

    /**
     * The translated name to display and sort
     * TODO use only for sorting?
     */
    private final String interpreterName;

    private final Class<? extends Interpreter> interpreterClass;
    private Interpreter interpreterInstance;

    // Use empty Strings as default, if no values are set
    private String qualityText = "";
    private String annotations = "";
    private int qualityIcon = 0;

    private boolean isQualityNeutral = true;

    /**
     * @param nameId the android string resource id
     * @param nameString the translated name to be able to sort the interpreters
     * @param interpreterClass and the class used to do the interpretation
     */
    InterpreterMapping(int nameId,
                               String nameString,
                               Class<? extends Interpreter> interpreterClass ) {

        this.interpreterNameStringId = nameId;
        this.interpreterName = nameString;
        this.interpreterClass = interpreterClass;
    }

    /**
     * To simply clone an {@link InterpreterMapping}. Does not clone interpretation /
     * {@link Interpreter}-instance!
     */
    InterpreterMapping(InterpreterMapping original) {
        this(original.interpreterNameStringId, original.interpreterName, original.interpreterClass);
    }

    /**
     * Creates an interpretation object.
     * @param day the {@link Day} object containing the data to interpret.
     * @param context The application context to get resource ids
     */
    void interpret( Day day, Context context ) {

        try {

            this.interpreterInstance =
                    this.interpreterClass.newInstance();
            this.interpreterInstance.setDayAndInterpret(day);

            this.processInterpreterResults(context);

        } catch (Exception e) {

            Log.e(TAG, "InterpreterMapping couldn't instantiate interpreter "
                    + this.interpreterName, e);
        }
    }

    private void processInterpreterResults(Context context) {

        final Interpreter.Quality quality = this.interpreterInstance.getQuality();

        final Integer[] qualityStringIds = ResourceMapper.getResourceIds( quality );

        // to allow showing name only if quality isn't neutral
        this.isQualityNeutral = (quality == Interpreter.Quality.NEUTRAL);

        if ( !this.isQualityNeutral ) {
            this.qualityIcon = qualityStringIds[ResourceMapper.INDEX_IMAGE];
            this.qualityText = context.getString(qualityStringIds[ResourceMapper.INDEX_STRING]);
        }

        final String[] annotationKeys = this.interpreterInstance.getAnnotationsAsStringArray();

        if ( annotationKeys.length > 0 ) {

            final LinkedList<String> annotationStrings = new LinkedList<>();

            for (String key : annotationKeys) {
                annotationStrings.add( context.getString(
                        ResourceMapper.getResourceIds(key)[ResourceMapper.INDEX_STRING] ));
            }

            this.annotations = TextUtils.join(" | ", annotationStrings);
        }
    }

    /**
     * @return uses the string resource id of the interpreter name as unique id
     */
    public int getId() {
        return this.interpreterNameStringId;
    }

    /**
     * @return The translated name
     */
    public String getInterpreterName() {
        return this.interpreterName;
    }

    /**
     * @return the image resource id of the quality icon or 0, if
     */
    int getQualityIcon() {
        return this.qualityIcon;
    }

    String getQualityText() {
        return this.qualityText;
    }

    String getAnnotations() {
        return this.annotations;
    }

    boolean isQualityNeutral() {
        return isQualityNeutral;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InterpreterMapping
            && this.interpreterNameStringId == ((InterpreterMapping) obj).interpreterNameStringId;
    }

    /**
     * Comparator to sort by translated name {@link #interpreterName}.
     */
    public static class NameComparator implements Comparator<InterpreterMapping> {

        @Override
        public int compare(InterpreterMapping interpreter1, InterpreterMapping interpreter2) {

            return interpreter1.interpreterName.compareTo( interpreter2.interpreterName );
        }
    }

    /**
     * Comparator to sort {@link de.kah2.mondtag.calendar.InterpreterMapping}s by the calculated
     * {@link de.kah2.libZodiac.interpretation.Interpreter.Quality} of their
     * {@link Interpreter}s: Best qualities first, worst last.
     * <strong>Be sure to call {@link InterpreterMapping#interpret(Day, Context)} first, to avoid
     * {@link NullPointerException}s</strong>
     */
    public static class QualityComparator implements Comparator<InterpreterMapping> {
        @Override
        public int compare(InterpreterMapping interpreter1, InterpreterMapping interpreter2) {

            return interpreter2.interpreterInstance.getQuality().compareTo(
                    interpreter1.interpreterInstance.getQuality() );
        }
    }
}