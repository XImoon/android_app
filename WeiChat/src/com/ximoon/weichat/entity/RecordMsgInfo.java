package com.ximoon.weichat.entity;

import java.io.Serializable;

public class RecordMsgInfo implements Serializable{

	public int _id;
	public String flag;
	public String msg;
	public String img;
	public String voice;
	public int emoji;
	public String time;
	
	public RecordMsgInfo() {
		super();
	}
	
}
