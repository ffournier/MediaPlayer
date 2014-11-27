package com.android2ee.mediaplayer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;

public class POJOAudio implements Parcelable, Comparable<POJOAudio> {

	
	private String path;
	private String name;
	private Date date;
	private int time;
	
	public POJOAudio(String path, String name, Date date) {
		super();
		this.path = path;
		this.name = name;
		this.date = date;
		this.time = -1;
		// TODO read file for time
		
		calculDuration();
	}
	
	private void calculDuration() {
		MediaPlayer mp = new MediaPlayer();
		try {
			mp.setDataSource(path);
			mp.prepare();
			this.time = mp.getDuration();
			mp.release();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public int compareTo(POJOAudio another) {
		return this.date.compareTo(another.date);
	}

	public POJOAudio(Parcel in) {
		super();
		this.path = in.readString();
		this.name = in.readString();
		this.date = (Date) in.readValue(Date.class.getClassLoader());
		this.time = in.readInt();
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public Date getDate() {
		return date;
	}

	public long getTime() {
		return time;
	}
	
	@Override
	public String toString() {
		if (time < 0) {
			return name;
		} else {
			SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss", Locale.getDefault());
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			return name + "time " + format.format((new Date(time)));
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
		dest.writeString(name);
		dest.writeValue(date);
		dest.writeInt(time);
	}
	
	public static final Parcelable.Creator<POJOAudio> CREATOR = new Parcelable.Creator<POJOAudio>()
	{
	    @Override
	    public POJOAudio createFromParcel(Parcel source)
	    {
	        return new POJOAudio(source);
	    }

	    @Override
	    public POJOAudio[] newArray(int size)
	    {
		return new POJOAudio[size];
	    }
	};

	

}
