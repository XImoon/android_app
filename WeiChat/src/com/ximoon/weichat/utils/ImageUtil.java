package com.ximoon.weichat.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class ImageUtil {
	public static Bitmap getBitmapFromPath(String path)
			throws ClientProtocolException, IOException {
		Bitmap bitmap = null;
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(path);//
		request.getParams().getIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == 200) {
			InputStream is = response.getEntity().getContent();
			bitmap = BitmapFactory.decodeStream(is);
			int oldWidth = bitmap.getWidth();
			int oldHeight = bitmap.getHeight();
			int newWidth = 60;
			int newHeight = 60;
			float sx = newWidth / (float) oldWidth;
			float sy = newHeight / (float) oldHeight;
			Matrix matrix = new Matrix();
			matrix.setScale(sx, sy);
			bitmap = bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight,
					matrix, true);
			is.close();
		}

		return bitmap;
	}

	public static Bitmap getjustableBitmap(Bitmap bitmap, float width,
			float height) {
		float sx = width / (float) bitmap.getWidth();
		float sy = height / (float) bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.setScale(sx, sy);
		bitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return bitmap;
	}

	public static void saveBitmap(Bitmap bitmap, String name) throws Exception {
		File f = new File("http://192.168.191.1:8080/WeiChat/images", name + ".png");
		if (!f.exists()) {
			f.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(f);
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		out.flush();
		out.close();
	}

	public static Bitmap loadBitmapFromLocal(String path) {
		Bitmap bitmap = null;
		File file = new File(path);
		if (file.exists()) {
			bitmap = BitmapFactory.decodeFile(path);
		}
		return bitmap;
	}

//	public static void sendData(Bitmap bitmap,String path,String name) throws Exception {
//		HttpClient httpClient = new DefaultHttpClient();  
//        HttpContext localContext = new BasicHttpContext();  
//        HttpPost httpPost = new HttpPost(path);  
//        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
//        Bitmap bmpCompressed = Bitmap.createScaledBitmap(bitmap, 180, 80, true);  
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
//        bmpCompressed.compress(CompressFormat.JPEG, 100, bos);  
//        byte[] data = bos.toByteArray();  
//        entity.addPart("customer_icon", new ByteArrayBody(data, name));  
//        httpPost.setEntity(entity);  
//        HttpResponse response = httpClient.execute(httpPost, localContext);  
//        BufferedReader reader = new BufferedReader(new InputStreamReader( response.getEntity().getContent(), "UTF-8"));  
//        String sResponse = reader.readLine(); 
//	}

}
