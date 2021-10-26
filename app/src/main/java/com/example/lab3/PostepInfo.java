package com.example.lab3;

import android.os.Parcel;
import android.os.Parcelable;

public class PostepInfo implements Parcelable {
    public int mPobranychBajtow = 0;
    public int mRozmiar = 0;
    public Status status = Status.Pobieranie_nierozpoczete;
    @Override
    public int describeContents() {
        return 0;
    }

    public PostepInfo(Parcel paczka) {
        mPobranychBajtow = paczka.readInt();
        mRozmiar = paczka.readInt();

    }

    public PostepInfo(int mRozmiar) {
        this.mRozmiar = mRozmiar;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mPobranychBajtow);
        dest.writeInt(mRozmiar);
        dest.writeString(status.name());
    }

    public static final Parcelable.Creator<PostepInfo> CREATOR =
            new Parcelable.Creator<PostepInfo>(){
                @Override
                public PostepInfo createFromParcel(Parcel source){
                    return new PostepInfo(source);
             }
                @Override
                public PostepInfo[] newArray(int size){
                    return new PostepInfo[size];
                }
        };

    public int getmPobranychBajtow() {
        return mPobranychBajtow;
    }

    public void setmPobranychBajtow(int mPobranychBajtow) {
        this.mPobranychBajtow = mPobranychBajtow;
    }

    public int getmRozmiar() {
        return mRozmiar;
    }

    public void setmRozmiar(int mRozmiar) {
        this.mRozmiar = mRozmiar;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        Pobieranie_nierozpoczete,
        Pobieranie_rozpoczete,
        Pobieranie_zakonczone,
        Pobieranie_blad,
    }
}
