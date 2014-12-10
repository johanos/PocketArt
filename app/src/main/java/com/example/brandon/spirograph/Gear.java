package com.example.brandon.spirograph;

/**
 * Created by Brandon on 12/6/2014.
 */

import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Gear implements Parcelable
{
    //List of points on gear to draw along
    public List<pPoint> drawPoints = new ArrayList<pPoint>();

    public Gear()
    {
        setRadius(0);
        setCenter(new pPoint(0,0));
    }

    public Gear(double radius)
    {
        setRadius(radius);
        setCenter(new pPoint(0,0));
    }

    public Gear(double radius, pPoint center)
    {
        setRadius(radius);
        setCenter(center);
    }

    public Gear(double radius, double centerX, double centerY)
    {
        setRadius(radius);
        setCenter(new pPoint(centerX, centerY));
    }

    private double _radius;
    public double getRadius() { return _radius; }
    public void setRadius(double value) { _radius = value; }

    private pPoint _center;
    public pPoint getCenter() { return _center; }
    public void setCenter(pPoint value) { _center = value; }

    private int mData;

    /* everything below here is for implementing Parcelable */

    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Gear> CREATOR = new Parcelable.Creator<Gear>() {
        public Gear createFromParcel(Parcel in) {
            return new Gear(in);
        }

        public Gear[] newArray(int size) {
            return new Gear[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Gear(Parcel in) {
        mData = in.readInt();
    }
}

