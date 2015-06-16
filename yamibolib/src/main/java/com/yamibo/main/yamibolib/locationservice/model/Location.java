package com.yamibo.main.yamibolib.locationservice.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.yamibo.main.yamibolib.model.GPSCoordinate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * created by 牛奶
 * Clover:
 * TODO modify: <br>
 *     增加 private field region，用于让程序可以根据地理位置选择合适的API服务（百度或Android）<br>
 * 增加private field mTime：可以构造能计算timeOffset的GPSCoordinate
 */
public class Location implements Parcelable {
    public static final DecimalFormat FMT = new DecimalFormat("#.00000", new DecimalFormatSymbols(
            Locale.ENGLISH));

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
    private static final SingleClassLoader CITY_CL = new SingleClassLoader(City.class);


    private double latitude;
    private double longitude;
    private double offsetLatitude;
    private double offsetLongitude;
    private String address;
    private City city;
    private int accuracy;

    private int region;
    private long mTime=0;
    public static final int NOT_IN_CN=0;
    public static final int IN_CN=1;

    private static final int DEFAULT_REGION =IN_CN;
    private static long DEFAULT_TIME=0;
    private static int DEFAULT_ACCRURACY=100;

    public Location(double latitude, double longitude, double offsetLatitude,
                    double offsetLongitude, String address, City city) {
        this(latitude,longitude,offsetLatitude,offsetLongitude,address,city,DEFAULT_ACCRURACY,DEFAULT_REGION,DEFAULT_TIME);
    }

    //todo modify: add missing accurary

    public Location(double latitude, double longitude, double offsetLatitude,
                    double offsetLongitude, String address, City city, int accuracy,
                    int region,long mTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.offsetLatitude = offsetLatitude;
        this.offsetLongitude = offsetLongitude;
        this.address = address;
        this.city = city;
        this.accuracy=accuracy;
        this.region = region;
        this.mTime=mTime;
    }


    private Location(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        offsetLatitude = in.readDouble();
        offsetLongitude = in.readDouble();
        address = in.readString();
        city = in.readParcelable(CITY_CL);
        accuracy = in.readInt();
        region =in.readInt();
        mTime=in.readLong();
    }

    public double latitude() {
        return latitude;
    }


    public double longitude() {
        return longitude;
    }

    public double offsetLatitude() {
        return offsetLatitude;
    }

    public double offsetLongitude() {
        return offsetLongitude;
    }

    //
    // Decoding
    //

    public String address() {
        return address;
    }

    public City city() {
        return city;
    }

    public int accuracy() {
        return accuracy;
    }

    public int getRegion(){
        return region;
    }
    public long getTime(){
        return mTime;
    }

    //
    // Parcelable
    //


    @Override
    public String toString() {
        if (address != null) {
            return address;
        }
        if (offsetLatitude != 0 && offsetLongitude != 0) {
            return "(" + FMT.format(offsetLatitude) + ", " + FMT.format(offsetLongitude) + ")";
        }
        return "(" + FMT.format(latitude) + ", " + FMT.format(longitude) + ")";
    }


    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeDouble(offsetLatitude);
        out.writeDouble(offsetLongitude);
        out.writeString(address);
        out.writeParcelable(city, flags);
        out.writeInt(accuracy);
        out.writeInt(region);
        out.writeLong(mTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
