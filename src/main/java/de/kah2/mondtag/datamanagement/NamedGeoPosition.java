package de.kah2.mondtag.datamanagement;

import android.location.Address;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;

import de.kah2.libZodiac.planetary.Position;

/**
 * <p>This class extends {@link Position} with a name field and transformation methods.</p>
 * <p>Besides formatting methods it provides import from {@link Address} and building GeoURIs.</p>
 *
 * Created by kahles on 18.11.16.
 */

public class NamedGeoPosition extends Position {

    private static final String TAG = NamedGeoPosition.class.getSimpleName();

    // Since it seems to be standard we use '.' as decimal separator
    private final static Locale LAT_LONG_LOCALE = Locale.ROOT;

    public final static String VALUE_SEPARATOR = ",";

    private final static String GEO_URI_PREFIX = "geo:";
    private final static String GEO_URI_SEPARATOR = ",";
    private final static String GEO_URI_PARAM_PREFIX = "?";
    private final static String GEO_URI_ZOOM_PREFIX = "z=";
    private final static int GEO_URI_ZOOM = 18;
    private final static String GEO_URI_PARAM_SEPARATOR = "&";
    private final static String GEO_URI_SEARCH_PREFIX = "q=";

    private final static String DOUBLE_FORMAT = "%.6f";

    private final String name;

    public NamedGeoPosition(double lat, double lng) {
        super(lat, lng);
        this.name = "";
    }

    public NamedGeoPosition(String name, double lat, double lng) {

        super(lat, lng);

        // remove VALUE_SEPARATOR occurrences to ensure serializability
        this.name = name.replace(VALUE_SEPARATOR, " ");
    }

    public NamedGeoPosition(Address address) {

        this( address.getAddressLine(0), address.getLatitude(), address.getLongitude());
    }

    /**
     * Creates a {@link NamedGeoPosition} object from comma-separated latitude,longitude
     * @throws IllegalArgumentException if given {@link String} couldn't be parsed
     */
    public static NamedGeoPosition from(String commaSeparatedLatLonValues)
            throws IllegalArgumentException {

        if (commaSeparatedLatLonValues == null) {
            throw new IllegalArgumentException("String to parse is null");
        }

        final String[] values = TextUtils.split(commaSeparatedLatLonValues, VALUE_SEPARATOR);

        if (values.length != 3)
            throw new IllegalArgumentException("Wrong number of values - " +
                    "should be NAME" + VALUE_SEPARATOR
                    + "LATITUDE" + VALUE_SEPARATOR
                    + "LONGITUDE");

        final String name = values[0];
        final double lat = Double.parseDouble(values[1]);
        final double lon = Double.parseDouble(values[2]);

        NamedGeoPosition position = new NamedGeoPosition(name, lat, lon);

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

        return this.getName() + VALUE_SEPARATOR
                + this.getLatitude() + VALUE_SEPARATOR
                + this.getLongitude();
    }
    
    /**
     * @return a string to display - not really compatible with {@link #from(String)}
     */
    public String toFormattedString() {
        return this.getName() + VALUE_SEPARATOR + " "
                + this.getFormattedLatitude() + VALUE_SEPARATOR + " "
                + this.getFormattedLongitude();
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

    /** Returns a String representing the latitude in neutral format "x.xxxxxx" */
    public String getFormattedLatitude() {
        return String.format( LAT_LONG_LOCALE, DOUBLE_FORMAT, this.getLatitude() );
    }

    /** Returns a String representing the longitude in neutral format "x.xxxxxx" */
    public String getFormattedLongitude() {
        return String.format( LAT_LONG_LOCALE, DOUBLE_FORMAT, this.getLongitude() );
    }

    public String getName() {
        return name;
    }
}
