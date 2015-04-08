package com.ximoon.weichat.utils;

import java.util.ArrayList;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.speech.SpeechError;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;

public class VoiceRecognizer implements RecognizerDialogListener{
	private RecognizerDialog dialog;
	private StringBuilder sb=new StringBuilder();
	private String voiceResult = null;
	
	
	public VoiceRecognizer(Context context){
		dialog=new RecognizerDialog(context, "appid="+Constants.XUNFEI_APPID+"");
		dialog.setListener(this);
	}
	public void start(){
		//设置识别的类型     sms文字转语音
		String engine="sms";
		//设置是否带标点符号。0不带，1带
		String params="asr_ptt=0";
		//是否进行语法识别
		dialog.setEngine(engine, params, null);
		//设置语音识别的采样率8-16k
		dialog.setSampleRate(RATE.rate16k);
		//将识别对话框显示出来
		dialog.show();
		voiceResult = null;
	}
	//处理识别结果
	@Override
	public void onEnd(SpeechError error) {
		// TODO Auto-generated method stub
		if (error==null) {
			voiceResult = sb.toString();
			sb.setLength(0);
		}else {
			int errorCode = error.getErrorCode();
			String errorDesc = error.getErrorDesc();
		}
	}
	//每次识别结果得到的时候调用该方法，被多次调用
	@Override
	public void onResults(ArrayList<RecognizerResult> results, boolean arg1) {
		// TODO Auto-generated method stub
		for (RecognizerResult result : results) {
			String text=result.text;
			sb.append(text);
		}
	}
	public String getResult(){
		while (voiceResult == null ) {
			SystemClock.sleep(500);
			continue;
		}
		return voiceResult;
	}
}
