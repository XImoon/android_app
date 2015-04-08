package com.ximoon.weichat.entity;

import java.io.Serializable;

public class PostInfo implements Serializable{
	/**编号*/
	public int _id;
	/**分类主题*/
	public String theme;
	/**标题*/
	public String title;
	/**第几楼*/
	public int layer;
	/**内容*/
	public String content;
	/**时间*/
	public String time;
	/**用户ID*/
	public int user_id;
	/**用户姓名*/
	public String name;
	/**图片路径/images/caocao.jpg*/
	public String pic;
	
	public PostInfo() {
		// TODO Auto-generated constructor stub
	}

	public PostInfo(int id, String theme, String title, int layer,
			String content, String time, int userId, String name, String pic) {
		super();
		_id = id;
		this.theme = theme;
		this.title = title;
		this.layer = layer;
		this.content = content;
		this.time = time;
		user_id = userId;
		this.name = name;
		this.pic = pic;
	}

	@Override
	public String toString() {
		return "PostInfo [_id=" + _id + ", content=" + content + ", layer="
				+ layer + ", name=" + name + ", pic=" + pic + ", theme="
				+ theme + ", time=" + time + ", title=" + title + ", user_id="
				+ user_id + "]";
	}
	
	
}
