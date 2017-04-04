package com.example.android.androidnews;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by giris on 8/11/2016.
 */
public class News implements Parcelable {
    private String mTitle;
    private String mAuthor;
    private String mDate;
    private String mUrl;
    public News(String Title, String Author, String Date, String Url)
    {
        mTitle = Title;
        mAuthor = Author;
        mDate= Date;
        mUrl= Url;
    }
    public String getmTitle() {
        return mTitle;
    }
    public String getmAuthor()
    {
        return mAuthor;
    }
    public String getmDate(){
        return mDate;
    }
    public String getUrl() {
        return mUrl;
    }
    protected News(Parcel in) {
        mTitle = in.readString();
        mAuthor = in.readString();
        mDate = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mAuthor);
        dest.writeString(mDate);
    }
    @SuppressWarnings("unused")
    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }
        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
}