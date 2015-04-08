package com.ximoon.weichat.entity;

import java.io.Serializable;

public class PhotoItem implements Serializable {
	private static final long serialVersionUID = 8682674788506891598L;
	private int  photoID;
	private boolean select;
	private String path;
	public PhotoItem(int id,String path) {
		photoID = id;
		select = false;
		this.path=path;
	}
	
	public PhotoItem(int id,boolean flag) {
		photoID = id;
		select = flag;
	}
	
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPhotoID() {
		return photoID;
	}
	public void setPhotoID(int photoID) {
		this.photoID = photoID;
	}
	public boolean isSelect() {
		return select;
	}
	public void setSelect(boolean select) {
		this.select = select;
	}
	@Override
	public String toString() {
		return "PhotoItem [photoID=" + photoID + ", select=" + select + "]";
	}
}
