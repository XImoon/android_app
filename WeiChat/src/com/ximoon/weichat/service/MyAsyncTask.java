package com.ximoon.weichat.service;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ximoon.weichat.R;
import com.ximoon.weichat.utils.Constants;
import com.ximoon.weichat.view.UpDateView;

public class MyAsyncTask {
	private Context context;
	private UpDateView upDateView;

	public MyAsyncTask(Context context, UpDateView upDateView) {
		super();
		this.context = context;
		this.upDateView = upDateView;
	}

	public void queryAnswer(String text) {
		new QueryAnswerAsyncTask().execute(text);

	}

	class QueryAnswerAsyncTask extends AsyncTask<String, Void, Object> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(String... params) {
			String str = params[0];
			String result = getAnswer(str);

			return result;
		}

		@Override
		protected void onPostExecute(Object result) {

			super.onPostExecute(result);
			String text = (String) result;
			upDateView.UpDateSheView(text);

		}

		private String getAnswer(String str) {
			if (str.contains("笑话")) {
				String[] jokeArray = context.getResources().getStringArray(
						R.array.joke);
				Random random = new Random();
				int index = random.nextInt(jokeArray.length);
				String jokeString = jokeArray[index];
			 	return jokeString;
			} else if (str.contains("报警")) {
				sendCallBroadCast(context, Constants.ACTIVITY_CALL_ACTION);
			}else if(str.contains("短信")){
				sendMessageBroadCast(context, Constants.ACTIVITY_SENDMESSAGE_ACTION);
			}else if (str.contains("pz")) {
				sendCameraBroadCast(context,Constants.ACTIVITY_CAMERA_ACTION);
			}
			return null;

		}
		private void sendCameraBroadCast(Context context,
				String action) {
			Intent intent = new Intent(Constants.ACTIVITY_CAMERA_ACTION);
	        intent.addCategory(Intent.CATEGORY_DEFAULT);
	        context.sendBroadcast(intent);
			
		}

		public void sendCallBroadCast(Context context,String action) {
			//
			Intent intent = new Intent(Constants.ACTIVITY_CALL_ACTION);
	        intent.addCategory(Intent.CATEGORY_DEFAULT);
	        context.sendBroadcast(intent);
	        
		}

		private void sendMessageBroadCast(Context context,String action) {
			//
	        Intent intent=new Intent(Constants.ACTIVITY_SENDMESSAGE_ACTION);
	        intent.addCategory(Intent.CATEGORY_DEFAULT);
	        context.sendBroadcast(intent);
		}
		
//		private void sendTruthOrBraveBroadCast(Context context,String action) {
//			//
//			Intent intent3=new Intent(Constants.ACTIVITY_TRUTHORBRAVE_ACTION);
//			intent3.addCategory(Intent.CATEGORY_DEFAULT);
//			context.sendBroadcast(intent3);
//		}
	}

}
