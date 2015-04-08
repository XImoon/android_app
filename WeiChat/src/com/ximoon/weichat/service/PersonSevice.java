package com.ximoon.weichat.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ximoon.weichat.entity.PostInfo;

public class PersonSevice {
	public static Bitmap getBitMapFromPath(String path) throws Exception,
			IOException {
		// 连网
		Bitmap bitmap = null;
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(path);
		request.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		HttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			InputStream is = response.getEntity().getContent();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		}

		return bitmap;
	}

	// 获得服务器上的数据
	public List<PostInfo> getPostsFromGson(String path) throws Exception {
		List<PostInfo> lists = null;
		// 第 一：连接获得is
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(new URI(path));
		request.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		HttpResponse response = client.execute(request);
		// 超时5000毫秒
		if (response.getStatusLine().getStatusCode() == 200) {
			InputStream is = response.getEntity().getContent();
			// 解析成字符串
			String gson = parserISToJson(is);
			lists = parserStrToGson(gson);
		}
		return lists;
	}

	private String parserISToJson(InputStream is) throws Exception {
		String line = null;

		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String json = sb.toString();
		br.close();
		isr.close();
		return json;
	}

	//
	private List<PostInfo> parserStrToGson(String msg) {
		// 创建一个Gson对象
		Gson gson = new Gson();
		// 记号：按List<PostInfo>来进行字符串解析
		TypeToken<List<PostInfo>> token = new TypeToken<List<PostInfo>>() {
		};
		// 将GSON格式的字符串转为指定的格式

		return gson.fromJson(msg, token.getType());
	}

	public boolean insertReplyLayer(String path, Map<String, String> map_reply) {
		// 拼实体
		List<NameValuePair> lists = new ArrayList<NameValuePair>();
		for (Entry<String, String> temp : map_reply.entrySet()) {
			// ctrl+t看它的实现类
			NameValuePair pair = new BasicNameValuePair(temp.getKey(),
					temp.getValue());
			lists.add(pair);
		}
		// 送
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(path);
		request.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);

		// 设置实体输出编码
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(lists, "UTF-8");
			// 提交
			request.setEntity(entity);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream is = response.getEntity().getContent();
				String msg = parserInputStreamToString(is);
				if ("ok".equals(msg)) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	private String parserInputStreamToString(InputStream is) throws Exception {
		int len = 0;
		byte[] buffer = new byte[1024 * 2];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		String msg = baos.toString().trim();// 去两边空格
		baos.close();
		is.close();
		return msg;
	}

	/**
	 * public List<PostInfo> getPersons(String path, String encoding) throws
	 * Exception { List<PostInfo> lists = null; // 第 一：连接获得is HttpClient client
	 * = new DefaultHttpClient(); HttpPost request = new HttpPost(path);
	 * request.
	 * getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
	 * 5000); HttpResponse response = client.execute(request); if
	 * (response.getStatusLine().getStatusCode() == 200) { InputStream is =
	 * response.getEntity().getContent(); lists = parseISToListPerson(is,
	 * encoding); }
	 * 
	 * return lists; }
	 * 
	 * private List<PostInfo> parseISToListPerson(InputStream is, String
	 * encoding) throws Exception { // 流变 List<PostInfo> lists = null; PostInfo
	 * info = null; XmlPullParser parser = Xml.newPullParser(); // 设置输出编码
	 * parser.setInput(is, encoding); // 获得事件类型 int eventType =
	 * parser.getEventType(); while (XmlPullParser.END_DOCUMENT != eventType) {
	 * switch (eventType) { case XmlPullParser.START_TAG: // 判断 if
	 * ("persons".equals(parser.getName())) { lists = new ArrayList<PostInfo>();
	 * } else if ("person".equals(parser.getName())) { info = new PostInfo(); }
	 * else if ("_id".equals(parser.getName())) { info._id =
	 * Integer.parseInt(parser.nextText()); } else if
	 * ("username".equals(parser.getName())) { info.name = parser.nextText(); }
	 * else if ("password".equals(parser.getName())) { info.password =
	 * parser.nextText(); } else if ("pic".equals(parser.getName())) { info.pic
	 * = parser.nextText(); }
	 * 
	 * break; case XmlPullParser.END_TAG: // 判断 if
	 * ("person".equals(parser.getName())) { lists.add(info); info = null; }
	 * break;
	 * 
	 * default: break; }
	 * 
	 * eventType = parser.next(); }
	 * 
	 * return lists; }
	 */
}
