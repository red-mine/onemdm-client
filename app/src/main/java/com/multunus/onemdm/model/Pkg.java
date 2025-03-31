package com.multunus.onemdm.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Pkg implements Parcelable{
    private long id;
    private String fingerPrint;
    private String name;
    private String otaUrl;

    public Pkg(){

    }
    public Pkg(Parcel source) {
        id = source.readLong();
        name = source.readString();
        fingerPrint = source.readString();
        otaUrl = source.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOtaUrl() {
        return otaUrl;
    }

    public void setOtaUrl(String otaURL) {
        this.otaUrl = otaURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(fingerPrint);
        dest.writeString(otaUrl);
    }

    public static final Parcelable.Creator<Pkg> CREATOR = new Parcelable.Creator<Pkg>() {

        @Override
        public Pkg createFromParcel(Parcel source) {
            return new Pkg(source);
        }

        @Override
        public Pkg[] newArray(int size) {
            return new Pkg[size];
        }
    };
}
