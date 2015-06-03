package com.yamibo.main.yamibolib.locationservice.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.yamibo.main.yamibolib.model.GPSCoordinate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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

    public Location(double latitude, double longitude, double offsetLatitude,
                    double offsetLongitude, String address, City city) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.offsetLatitude = offsetLatitude;
        this.offsetLongitude = offsetLongitude;
        this.address = address;
        this.city = city;
    }

    private Location() {
    }

    private Location(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        offsetLatitude = in.readDouble();
        offsetLongitude = in.readDouble();
        address = in.readString();
        city = in.readParcelable(CITY_CL);
        accuracy = in.readInt();
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

    //
    // Parcelable
    //

    public GPSCoordinate coord() {
        if (offsetLatitude != 0 && offsetLongitude != 0) {
            return new GPSCoordinate(offsetLatitude, offsetLongitude);
        } else {
            return new GPSCoordinate(latitude, longitude);
        }
    }

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
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
