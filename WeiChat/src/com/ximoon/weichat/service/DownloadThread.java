package com.ximoon.weichat.service;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread {
	/** 线程id */
	private int threadId;
	/** 块的大小 */
	private int block;
	/** 下载的路径 */
	private String path;
	/** 下到哪里 */
	private File dir;
	/** 请求起始 */
	private int startPosition;
	private int endPosition;

	public DownloadThread(int threadId, int block, String path, File dir) {
		super();
		this.threadId = threadId;
		this.block = block;
		this.path = path;
		this.dir = dir;

		startPosition = threadId * block;
		endPosition = (threadId + 1) * block - 1;
		System.out.println(threadId+"-"+startPosition+"----"+endPosition);
	}

	@Override
	public void run() {
		// 写啥
		try {
			//请求服务器
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5000);
			//不要再判断200
			//分段请求
			conn.setRequestProperty("Range", "bytes="+startPosition+"-"+endPosition);
			InputStream is = conn.getInputStream();
			//被随机写入到本地
			RandomAccessFile raf = new RandomAccessFile(dir, "rwd");
			raf.seek(startPosition);//改变初始位置  从哪点开始下
			int len = 0;
			byte[] buffer = new byte[1024*8];
			while((len=is.read(buffer))!=-1){
				raf.write(buffer, 0, len);
			}
			raf.close();
			is.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.run();
	}

}