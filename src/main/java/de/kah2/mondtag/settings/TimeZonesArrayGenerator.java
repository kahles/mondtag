package de.kah2.mondtag.settings;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class is used to get sorted Arrays of {@link ZoneId}s and the
 * corresponding strings to display.
 */
class TimeZonesArrayGenerator {

    private final static int INDEX_IDS = 0;
    private final static int INDEX_DESCRIPTIONS = 1;

    private static String[][] zonesAndOffsets;

    private static List<TimeZone> getAvailableTimezonesSorted() {
        final LinkedList<TimeZone> tzList = new LinkedList<>();

        final Set<String> zones = ZoneId.getAvailableZoneIds();

        final LocalDateTime myDateTime = LocalDateTime.now();

        for (String zone : zones) {
            final ZoneId zoneId = ZoneId.of(zone);
            final ZonedDateTime zonedDateTime = myDateTime.atZone(zoneId);
            final ZoneOffset offset = zonedDateTime.getOffset();

            final TimeZone entry = new TimeZone(zoneId, offset);

            tzList.add(entry);
        }

        Collections.sort(tzList);

        return tzList;
    }

    private static void initZonesAndOffsetsArrayIfNecessary() {

        if (zonesAndOffsets != null) {
            return;
        }

        List<TimeZone> timeZones = getAvailableTimezonesSorted();

        zonesAndOffsets = new String[2][timeZones.size()];
        int numberOfZone = 0;

        for (TimeZone zone : timeZones) {
            final String idString = zone.id.toString();

            zonesAndOffsets[INDEX_IDS][numberOfZone] = idString;
            zonesAndOffsets[INDEX_DESCRIPTIONS][numberOfZone] = zone.getCleanOffsetString() + " " + idString ;

            numberOfZone++;
        }
    }

    /**
     * @return An array of {@link ZoneId}s whose indices correspond to the array returned by
     * {@link #getZoneDescriptions()}
     */
    public static String[] getZoneIds() {
        initZonesAndOffsetsArrayIfNecessary();
        return zonesAndOffsets[INDEX_IDS];
    }

    /**
     * @return An array of time zone description texts whose indices correspond to the array
     * returned by {@link #getZoneIds()}
     */
    public static String[] getZoneDescriptions() {
        initZonesAndOffsetsArrayIfNecessary();
        return zonesAndOffsets[INDEX_DESCRIPTIONS];
    }

    /**
     * This class is used to manage {@link ZoneId}s and their corresponding {@link ZoneOffset}s
     * and make them {@link Comparable}.
     */
    private static class TimeZone implements Comparable<TimeZone> {

        private final static String UTC_OFFSET_STRING = "+00:00";

        private final ZoneId id;
        private final ZoneOffset offset;

        TimeZone(ZoneId id, ZoneOffset offset) {
            this.id = id;
            this.offset = offset;
        }

        @Override
        public int compareTo(@NonNull TimeZone o) {
            final int offsetComparison = this.offset.compareTo(o.offset);

            if (offsetComparison != 0)
                return offsetComparison;
            else
                return this.id.toString().compareTo(o.id.toString());
        }

        /** Returns the offset as a String or {@value #UTC_OFFSET_STRING} if there's no offset. */
        String getCleanOffsetString() {
            if (this.offset.equals(ZoneOffset.UTC)) {
                return UTC_OFFSET_STRING;
            } else {
                return this.offset.toString();
            }
        }
    }
}
