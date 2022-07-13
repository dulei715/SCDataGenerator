package org.geocrowd.datasets.synthesis.didi;

public class DiDiCheckin implements Comparable<DiDiCheckin> {
    public long orderId;
    public int timestamp;
    public double longitude;
    public double latitude;

    public DiDiCheckin(long id, int timestamp, double lnt, double lat) {
        this.orderId = id;
        this.timestamp = timestamp;
        this.longitude = lnt;
        this.latitude = lat;
    }

    public int compareTo(DiDiCheckin o) {
        if (this.timestamp > o.timestamp)
            return 1;
        else if (this.timestamp < o.timestamp)
            return -1;
        else
            return 0;
    }
}
