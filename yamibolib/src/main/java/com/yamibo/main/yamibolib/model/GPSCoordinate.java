package com.yamibo.main.yamibolib.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;

public class GPSCoordinate implements Parcelable {
    public static final GPSCoordinate NULL = new GPSCoordinate(Double.NaN,
            Double.NaN, 0, 0, "null");
    public static final Parcelable.Creator<GPSCoordinate> CREATOR = new Parcelable.Creator<GPSCoordinate>() {
        public GPSCoordinate createFromParcel(Parcel in) {
            return new GPSCoordinate(in);
        }

        public GPSCoordinate[] newArray(int size) {
            return new GPSCoordinate[size];
        }
    };
    private static final double RADIUS = 6371000;
    private static final DecimalFormat FMT = new DecimalFormat("0.#####");
    private final double latitude;
    private final double longitude;
    private final int accuracy;
    private final long timeOffset;
    private final String source;

    public GPSCoordinate(double lat, double lon) {
        this(lat, lon, 0, 0, "");
    }

    public GPSCoordinate(double lat, double lon, int accuracy, long timeOffset,
                         String source) {
        this.latitude = lat;
        this.longitude = lon;
        this.accuracy = accuracy;
        this.timeOffset = timeOffset;
        this.source = source;
    }

    public GPSCoordinate(Location l) {
        this.latitude = l.getLatitude();
        this.longitude = l.getLongitude();
        this.accuracy = (int) l.getAccuracy();
        this.timeOffset = l.getTime() - System.currentTimeMillis();
        this.source = l.getProvider();
    }

    private GPSCoordinate(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        accuracy = in.readInt();
        timeOffset = in.readLong();
        source = in.readString();
    }

    public double latitude() {
        return latitude;
    }

    /**
     * 返回格式化的latitude字符串<br>
     * 保留5位小数
     */
    public String latitudeString() {
        return FMT.format(latitude);
    }

    public double longitude() {
        return longitude;
    }

    /**
     * 返回格式化的longitude字符串<br>
     * 保留5位小数
     */
    public String longitudeString() {
        return FMT.format(longitude);
    }

    public int accuracy() {
        return accuracy;
    }

    public long timeOffset() {
        return timeOffset;
    }

    public String source() {
        return source;
    }

    public boolean isValid() {
        if (this == NULL)
            return false;
        if (latitude == 0 && longitude == 0)
            return false;
        if (!(latitude >= -90 && latitude <= 90))
            return false;
        if (!(longitude >= -180 && longitude <= 180))
            return false;
        return true;
    }

    public boolean isFresh(long expire) {
        return timeOffset <= 0 && timeOffset >= -expire;
    }

    @Override
    protected Object clone() {
        return new GPSCoordinate(latitude, longitude, accuracy, timeOffset,
                source);
    }

    //
    // Parcelable
    //

    public double distanceTo(GPSCoordinate point) {
        if (point == this)
            return 0;
        double lat1 = latitude / 180 * Math.PI;
        double lon1 = longitude / 180 * Math.PI;
        double lat2 = point.latitude / 180 * Math.PI;
        double lon2 = point.longitude / 180 * Math.PI;
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(lat1)
                * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return RADIUS * c;
    }

    @Override
    public String toString() {
        if (this == NULL)
            return "(?,?) [null]";
        return "(" + FMT.format(latitude) + "," + FMT.format(longitude) + ") ["
                + accuracy + "," + source + "]";
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeInt(accuracy);
        out.writeLong(timeOffset);
        out.writeString(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
