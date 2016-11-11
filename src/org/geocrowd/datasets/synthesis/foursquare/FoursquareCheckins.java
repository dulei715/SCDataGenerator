package org.geocrowd.datasets.synthesis.foursquare;

import org.geocrowd.dtype.PointTime;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;

/**
 * Created by jianxun on 16/5/15.
 */
public class FoursquareCheckins implements Comparable<FoursquareCheckins> {
    public String userId;
    public String venueId;
    public long utcTime;
    public double longitude;
    public double latitude;

    private static SimpleDateFormat sourceDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");

    public static FoursquareCheckins parse(String line) {
        if (line.endsWith("\n")) {
            line = line.substring(0, line.length() - 1);
        }
        String[] parts = line.split("\t");
        FoursquareCheckins result = new FoursquareCheckins();
        result.userId = parts[0];
        result.venueId = parts[1];
        try {
            result.utcTime = sourceDateFormat.parse(parts[2]).getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.latitude = Double.parseDouble(parts[3]);
        result.longitude = Double.parseDouble(parts[4]);
        return result;
    }

    public int compareTo(FoursquareCheckins o) {
        if (this.utcTime > o.utcTime)
            return 1;
        else if (this.utcTime < o.utcTime)
            return -1;
        else
            return 0;
    }
}
