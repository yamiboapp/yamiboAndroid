package com.yamibo.main.yamibolib.locationservice.model;

import android.os.Parcel;
import android.os.Parcelable;

public class City implements Parcelable {

    /**
     * TODO question: what are id and flag? Use 0,0 for the present constructoin. See the constructor blow.
     */
    private int id;
    private String name;
    private int flag;

    public static final City DEFAULT = new City(0, "上海", 0);

    public City(String name){
        this(0,name,0);
    }

    public City(int id, String name, int flag) {
        this.id = id;
        this.name = name;
        this.flag = flag;
    }


    private City(Parcel in) {
        id = in.readInt();
        name = in.readString();
        flag = in.readInt();
    }
//
// Parcelable
//

    @Override
    public String toString() {
        return id + " : " + name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };


    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeInt(flag);
    }
}
