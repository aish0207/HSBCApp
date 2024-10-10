package com.example.hsbcapp;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDetails implements Parcelable {
    private String name;
    private String address;
    private String mobile;

    public UserDetails(String name, String address, String mobile) {
        this.name = name;
        this.address = address;
        this.mobile = mobile;
    }

    protected UserDetails(Parcel in) {
        name = in.readString();
        address = in.readString();
        mobile = in.readString();
    }

    public static final Creator<UserDetails> CREATOR = new Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel in) {
            return new UserDetails(in);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(mobile);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getMobile() {
        return mobile;
    }
}
