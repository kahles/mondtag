package de.kah2.mondtag.calendar;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Comparator;
import java.util.LinkedList;

import de.kah2.zodiac.libZodiac4A.Day;
import de.kah2.zodiac.libZodiac4A.interpretation.Interpreter;

/**
 * <p>This is a wrapper for Interpreter providing string-ids, icons and {@link Comparator}s needed
 * by the views.</p>
 * <p>Because we have multiple subclasses of {@link Interpreter}, we can't inherit from all of them.
 * So we use an instance and delegate the work.</p>
 */
public class MappedInterpreter {

    private static final String TAG = MappedInterpreter.class.getSimpleName();

    /** The android-string-resource-id */
    private final int interpreterNameStringId;

    /**
     * The translated name needed for sorting. To display the name use the resource id of
     * {@link #getId()} to avoid problems if the user switches language while the app is active.
     */
    private final String interpreterName;

    private final Class<? extends Interpreter> interpreterClass;

    private Interpreter interpreterInstance;

    private String qualityText;
    private String annotations;
    private int qualityIcon;
    private boolean isQualityNeutral;

    /**
     * @param nameId the android string resource id
     * @param nameString the translated name to be able to sort the interpreters
     * @param interpreterClass and the class used to do the interpretation
     */
    MappedInterpreter(int nameId,
                      String nameString,
                      Class<? extends Interpreter> interpreterClass ) {

        this.interpreterNameStringId = nameId;
        this.interpreterName = nameString;
        this.interpreterClass = interpreterClass;
    }

    /**
     * To simply clone an {@link MappedInterpreter}. Does not clone interpretation /
     * {@link Interpreter}-instance.
     */
    @SuppressWarnings("CopyConstructorMissesField")
    MappedInterpreter(MappedInterpreter original) {
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

            Log.e(TAG, "MappedInterpreter couldn't instantiate interpreter "
                    + this.interpreterName, e);
        }
    }

    private void processInterpreterResults(Context context) {

        final Interpreter.Quality quality = this.interpreterInstance.getQuality();

        final Integer[] qualityStringIds = ResourceMapper.getResourceIds( quality );

        // to allow showing name only if quality isn't neutral
        this.isQualityNeutral = (quality == Interpreter.Quality.NEUTRAL);

        if ( this.isQualityNeutral ) {

            this.qualityIcon = 0;
            this.qualityText = "";

        } else {
            this.qualityIcon = qualityStringIds[ResourceMapper.INDEX_IMAGE];
            this.qualityText = context.getString(qualityStringIds[ResourceMapper.INDEX_STRING]);
        }

        final String[] annotationKeys = this.interpreterInstance.getAnnotationsAsStringArray();

        if ( annotationKeys.length == 0 ) {

            // If the active mapping did some Interpretation before, we have to reset them
            this.annotations = "";

        } else {

            final LinkedList<String> annotationStrings = new LinkedList<>();

            for (String key : annotationKeys) {
                annotationStrings.add( context.getString(
                        ResourceMapper.getResourceIds(key)[ResourceMapper.INDEX_STRING] ));
            }

            this.annotations = TextUtils.join(" | ", annotationStrings);
        }
    }

    /**
     * @return the string resource id of the interpreter name as unique id
     */
    public int getId() {
        return this.interpreterNameStringId;
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
        return obj instanceof MappedInterpreter
            && this.interpreterNameStringId == ((MappedInterpreter) obj).interpreterNameStringId;
    }

    /**
     * Comparator to sort by translated name {@link #interpreterName}.
     */
    public static class NameComparator implements Comparator<MappedInterpreter> {

        @Override
        public int compare(MappedInterpreter interpreter1, MappedInterpreter interpreter2) {

            return interpreter1.interpreterName.compareTo( interpreter2.interpreterName );
        }
    }

    /**
     * Comparator to sort {@link MappedInterpreter}s by the calculated
     * {@link de.kah2.libZodiac.interpretation.Interpreter.Quality} of their
     * {@link Interpreter}s: Best qualities first, worst last.
     * <strong>Be sure to call {@link MappedInterpreter#interpret(Day, Context)} first, to avoid
     * {@link NullPointerException}s</strong>
     */
    public static class QualityComparator implements Comparator<MappedInterpreter> {
        @Override
        public int compare(MappedInterpreter interpreter1, MappedInterpreter interpreter2) {

            return interpreter2.interpreterInstance.getQuality().compareTo(
                    interpreter1.interpreterInstance.getQuality() );
        }
    }
}