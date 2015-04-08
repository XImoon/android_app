package com.ximoon.weichat.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class ClientInfo implements Parcelable{
	
	/** _id  */
	public int _id;
	/** 账号*/
	public String username;
	/** 密码*/
	public String password;
	/** ip地址*/
	public String ip;
	/** 端口号*/
	public int port;
	/** 是否有新消息*/
	public boolean isNew;
	/** 昵称*/
	public String nickname;
	/** 签名*/
	public String motto;
	/** 头像*/
	public String headicon;
	/** 电话*/
	public String phone;
	/** 性别*/
	public String sex;
	/** 出生日期*/
	public String birth;
	/** 昵称*/
	public String exname;
	/** 状态*/
	public boolean isonline;
//	/** 朋友*/
//	public List<ClientInfo> friends;
	
	public ClientInfo() {
		super();
	}

	public ClientInfo(String ip, int port, boolean isNew) {
		super();
		this.ip = ip;
		this.port = port;
		this.isNew = isNew;
	}

	public ClientInfo(String ip,int port,int _id,String username, String nickname, String motto, String headicon, boolean isNew) {
		super();
		this._id = _id;
		this.username = username;
		this.nickname = nickname;
		this.ip = ip;
		this.port = port;
		this.motto = motto;
		this.headicon = headicon;
		this.isNew = isNew;
	}
	
	public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(_id);
        out.writeString(username);
        out.writeString(nickname);
        out.writeString(ip);
        out.writeInt(port);
        out.writeString(motto);
        out.writeString(headicon);
        out.writeString(phone);
        out.writeString(sex);
        out.writeString(birth);
        out.writeString(exname);
        out.writeInt(isNew ? 1 :0);
        out.writeInt(isonline ? 1 :0);
//        out.writeList(friends);
    }

    public static final Parcelable.Creator<ClientInfo> CREATOR
            = new Parcelable.Creator<ClientInfo>() {
        public ClientInfo createFromParcel(Parcel in) {
           ClientInfo info = new ClientInfo();
           info._id = in.readInt();
           info.username = in.readString();
           info.nickname = in.readString();
           info.ip = in.readString();
           info.port = in.readInt();
           info.motto = in.readString();
           info.headicon = in.readString();
           info.phone = in.readString();
           info.sex = in.readString();
           info.birth = in.readString();
           info.exname = in.readString();
           info.isNew = in.readInt() == 1 ? true : false;
           info.isonline = in.readInt() == 1 ? true : false;
//           info.friends = in.readArrayList(null);
        	return info;
        }

        public ClientInfo[] newArray(int size) {
            return new ClientInfo[size];
        }
    };

	@Override
	public String toString() {
		return "ClientInfo [_id=" + _id + ", username=" + username
				+ ", password=" + password + ", ip=" + ip + ", port=" + port
				+ ", isNew=" + isNew + ", nickname=" + nickname + ", motto="
				+ motto + ", headicon=" + headicon + ", phone=" + phone
				+ ", sex=" + sex + ", birth=" + birth + ", exname=" + exname
				+ "]";
	}
	
}