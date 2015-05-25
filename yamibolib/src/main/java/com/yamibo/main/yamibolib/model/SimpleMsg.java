package com.yamibo.main.yamibolib.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SimpleMsg作为MApi在4xx返回的类型，包含具体的用户可读的错误信息
 * 
 * @author Yimin
 * 
 */
public class SimpleMsg implements Parcelable {
	protected String title;
	protected String content;
	protected int icon;
	protected int flag;
	protected int statusCode;
	protected String data;

	public SimpleMsg(String title, String content, int icon, int flag) {
		this.title = title;
		this.content = content;
		this.icon = icon;
		this.flag = flag;
	}

	public SimpleMsg(int statusCode, String title, String content, int icon,
			int flag, String data) {
		this.statusCode = statusCode;
		this.title = title;
		this.content = content;
		this.icon = icon;
		this.flag = flag;
		this.data = data;
	}

	public int statusCode() {
		return statusCode;
	}

	public String title() {
		return title;
	}

	public String content() {
		return content;
	}

	@Deprecated
	public int icon() {
		return icon;
	}

	public int flag() {
		return flag;
	}

	public String data() {
		return data;
	}

	@Override
	public String toString() {
		return title + " : " + content;
	}

	//
	// Decoding
	//

	protected SimpleMsg() {
	}

	//
	// Parcelable
	//

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(statusCode);
		out.writeString(title);
		out.writeString(content);
		out.writeInt(icon);
		out.writeInt(flag);
		out.writeString(data);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<SimpleMsg> CREATOR = new Parcelable.Creator<SimpleMsg>() {
		public SimpleMsg createFromParcel(Parcel in) {
			return new SimpleMsg(in);
		}

		public SimpleMsg[] newArray(int size) {
			return new SimpleMsg[size];
		}
	};

	protected SimpleMsg(Parcel in) {
		statusCode = in.readInt();
		title = in.readString();
		content = in.readString();
		icon = in.readInt();
		flag = in.readInt();
		data = in.readString();
	}
}
