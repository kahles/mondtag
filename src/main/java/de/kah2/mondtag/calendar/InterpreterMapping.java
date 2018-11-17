package de.kah2.mondtag.calendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedList;

import de.kah2.libZodiac.Day;
import de.kah2.libZodiac.interpretation.Interpreter;

/**
 * This is a helper class to do an interpretation for a {@link Day}-object and provide translated
 * Strings and Icons.
 */
public class InterpreterMapping implements Comparable<InterpreterMapping> {

    public static final String TAG = InterpreterMapping.class.getSimpleName();

    private final int interpreterNameStringid;
    private final String interpreterName;
    private final Class<? extends Interpreter> interpreterClass;

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

        this.interpreterNameStringid = nameId;
        this.interpreterName = nameString;
        this.interpreterClass = interpreterClass;
    }

    /**
     * Creates an interpretation object.
     * @param day the {@link Day} object containing the data to interpret.
     * @param context The application context to get resource ids
     */
    void interpret( Day day, Context context ) {

        try {

            final Interpreter interpreter =
                    this.interpreterClass.newInstance();
            interpreter.setDayAndInterpret(day);

            this.processInterpreterResults(interpreter, context);

        } catch (Exception e) {

            Log.e(TAG, "InterpreterMapping couldn't instantiate interpreter "
                    + this.interpreterName, e);
        }
    }

    private void processInterpreterResults( Interpreter interpreter,
                                            Context context) {

        final Interpreter.Quality quality = interpreter.getQuality();

        final Integer[] qualityStringIds = ResourceMapper.getResourceIds( quality );

        // to allow showing name only if quality isn't neutral
        this.isQualityNeutral = (quality == Interpreter.Quality.NEUTRAL);

        if ( !this.isQualityNeutral ) {
            this.qualityIcon = qualityStringIds[ResourceMapper.INDEX_IMAGE];
            this.qualityText = context.getString(qualityStringIds[ResourceMapper.INDEX_STRING]);
        }

        final String[] annotations = interpreter.getAnnotationsAsStringArray();

        if ( annotations.length > 0 ) {

            final LinkedList<String> annotationStrings = new LinkedList<>();

            for (String annotationKey : annotations) {

                annotationStrings.add( context.getString(
                        ResourceMapper.getResourceIds(annotationKey)[ResourceMapper.INDEX_STRING] ));
            }

            this.annotations = TextUtils.join(" | ", annotationStrings);
        }
    }

    /**
     * @return uses the string resource id of the interpreter name as unique id
     */
    public int getId() {
        return this.interpreterNameStringid;
    }

    /**
     * @return The translated name
     */
    public String getInterpreterName() {
        return this.interpreterName;
    }

    /**
     * To allow showing the name only if interpreted quality isn't neutral.
     * @return the translated name or an empty string if quality is neutral
     */
    String getInterpreterNameIfNotNeutral() {

        if (isQualityNeutral) {
            return "";
        } else {
            return this.getInterpreterName();
        }
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

    @Override
    public int compareTo(@NonNull InterpreterMapping interpreterMapping) {
        return this.interpreterName.compareTo( interpreterMapping.interpreterName );
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InterpreterMapping
                && this.interpreterName.equals(
                        ((InterpreterMapping) obj).interpreterName );
    }
}