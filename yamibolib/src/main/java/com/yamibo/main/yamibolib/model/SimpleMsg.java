package com.yamibo.main.yamibolib.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dianping.archive.ArchiveException;
import com.dianping.archive.DPObject;
import com.dianping.archive.Decoding;
import com.dianping.archive.DecodingFactory;
import com.dianping.archive.Unarchiver;

/**
 * SimpleMsg作为MApi在4xx返回的类型，包含具体的用户可读的错误信息
 * 
 * @author Yimin
 * 
 */
public class SimpleMsg implements Parcelable, Decoding {
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

	public SimpleMsg(DPObject obj) {
		this.statusCode = obj.getInt(0x8d);
		this.title = obj.getString(0x36e9);
		this.content = obj.getString(0x57b6);
		this.icon = obj.getInt(0xb0bb);
		this.flag = obj.getInt(0x73ad);
		this.data = obj.getString(0x63ea);
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

	public static final DecodingFactory<SimpleMsg> DECODER = new DecodingFactory<SimpleMsg>() {
		@Override
		public SimpleMsg[] createArray(int length) {
			return new SimpleMsg[length];
		}

		@Override
		public SimpleMsg createInstance(int hash16) {
			if (hash16 == 0x909d) { // SimpleMsg
				return new SimpleMsg();
			} else {
				return null;
			}
		}
	};

	@Override
	public void decode(Unarchiver u) throws ArchiveException {
		int hash16;
		while ((hash16 = u.readMemberHash16()) > 0) {
			switch (hash16) {
			case 0x8d: // StatusCode
				this.statusCode = u.readInt();
				break;
			case 0x36e9: // Title
				this.title = u.readString();
				break;
			case 0x57b6: // Content
				this.content = u.readString();
				break;
			case 0xb0bb: // Icon
				this.icon = u.readInt();
				break;
			case 0x73ad: // Flag
				this.flag = u.readInt();
				break;
			case 0x63ea: // Data
				this.data = u.readString();
				break;
			default:
				u.skipAnyObject();
				break;
			}
		}
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
