package de.kah2.mondtag.datamanagement;

import android.net.Uri;
import android.text.TextUtils;

import de.kah2.libZodiac.planetary.Position;

/**
 * This class extends {@link Position} with transformation methods to enable handling
 * comma-separated values and building GeoUris.
 *
 * Created by kahles on 18.11.16.
 */

public class StringConvertiblePosition extends Position {

    public final static String VALUE_SEPARATOR = ",";

    private final static String GEO_URI_PREFIX = "geo:";
    private final static String GEO_URI_SEPARATOR = ",";
    private final static String GEO_URI_PARAM_PREFIX = "?";
    private final static String GEO_URI_ZOOM_PREFIX = "z=";
    private final static int GEO_URI_ZOOM = 18;
    private final static String GEO_URI_PARAM_SEPARATOR = "&";
    private final static String GEO_URI_SEARCH_PREFIX = "q=";

    private StringConvertiblePosition(double lat, double lng) {
        super(lat, lng);
    }

    /**
     * Creates a {@link StringConvertiblePosition} object from comma-separated latitude,longitude
     * @throws IllegalArgumentException if given {@link String} couldn't be parsed
     */
    public static StringConvertiblePosition from(String commaSeparatedLatLonValues)
            throws IllegalArgumentException {
        String[] values = TextUtils.split(commaSeparatedLatLonValues, VALUE_SEPARATOR);

        if (values.length != 2)
            throw new IllegalArgumentException("Wrong number of values - " +
                    "should be LAT" + VALUE_SEPARATOR + "LONG");

        double lat = Double.parseDouble(values[0]);
        double lon = Double.parseDouble(values[1]);

        StringConvertiblePosition position = new StringConvertiblePosition(lat, lon);

        if (!position.isValid()) {
            throw new IllegalArgumentException(
                    "Values of latitude or longitude are not in valid range");
        }

        return position;
    }

    /**
     * Exports comma-separated values
     */
    @Override
    public String toString() {
        return this.getLatitude() + VALUE_SEPARATOR + this.getLongitude();
    }

    /**
     * Creates a GeoUri that can be passed to {@link android.content.Intent}s of type
     * {@link android.content.Intent#ACTION_VIEW} to open the position with a maps app.
     */
    public Uri toGeoUri() {
        return Uri.parse(GEO_URI_PREFIX
                + this.getLatitude() + GEO_URI_SEPARATOR + this.getLongitude()
                + GEO_URI_PARAM_PREFIX
                + GEO_URI_ZOOM_PREFIX + GEO_URI_ZOOM
                + GEO_URI_PARAM_SEPARATOR
                + GEO_URI_SEARCH_PREFIX + this.getLatitude() + GEO_URI_SEPARATOR + getLongitude() );
    }
}
