/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-08
 * Description:
 */

package com.kindroid.security.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.kindroid.security.R;
import com.kindroid.security.util.Base64Handler;
import com.kindroid.security.util.CommonProtoc;
import com.kindroid.security.util.Constant;
import com.kindroid.security.util.FeedbackProtoc;
import com.kindroid.security.util.HttpRequestUtil;
import com.kindroid.security.util.RequestProtoc;
import com.kindroid.security.util.ResponseProtoc;
import com.kindroid.security.util.Utilis;
import com.kindroid.security.util.ResponseProtoc.ResponseContext;

public class FeedbackActivity extends Activity {
	private EditText email_input;
	private EditText desp_input;
	private Spinner spinner_feedback;
	private String[] spinnerArray;
	private String feedBackType;
	private ArrayAdapter adapter;
	private ScrollView scrollView;
	private GestureDetector gestureDetector;
	private static List<CommonProtoc.FeedbackType> fdType;
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private boolean mFeedingBack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_back);
		fdType = new ArrayList<CommonProtoc.FeedbackType>();
		fdType.add(CommonProtoc.FeedbackType.GENERAL);
		fdType.add(CommonProtoc.FeedbackType.VIRUS);
		fdType.add(CommonProtoc.FeedbackType.NET);
		fdType.add(CommonProtoc.FeedbackType.MESSAGE_INTERCEPT);
		fdType.add(CommonProtoc.FeedbackType.TASK);
		fdType.add(CommonProtoc.FeedbackType.APP);
		fdType.add(CommonProtoc.FeedbackType.WATCHDOG);
		fdType.add(CommonProtoc.FeedbackType.BACKUP);
		fdType.add(CommonProtoc.FeedbackType.ACCOUNT);
		fdType.add(CommonProtoc.FeedbackType.CLIENT);
		fdType.add(CommonProtoc.FeedbackType.UPGRADE);
		fdType.add(CommonProtoc.FeedbackType.CACHE_CLEAN);
		fdType.add(CommonProtoc.FeedbackType.BOOT_SPEEDUP);
		fdType.add(CommonProtoc.FeedbackType.EXAMINE);
		fdType.add(CommonProtoc.FeedbackType.FIREWALL);
		fdType.add(CommonProtoc.FeedbackType.OTHER_FEEDBACK);

		email_input = (EditText) findViewById(R.id.email_input);
		desp_input = (EditText) findViewById(R.id.desp_input);
		scrollView = (ScrollView) findViewById(R.id.scroview);
		scrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		gestureDetector = new GestureDetector(new MyGestureDetector());

		desp_input.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				// TODO Auto-generated method stub
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;

			}
		});

		spinner_feedback = (Spinner) findViewById(R.id.spinner_feedback);
		spinnerArray = getResources().getStringArray(
				R.array.feeback_list_string);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spinnerArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_feedback.setAdapter(adapter);
		spinner_feedback
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						feedBackType = (String) adapter.getItem(position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						feedBackType = null;
					}

				});

		Button button_ok = (Button) findViewById(R.id.button_ok);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (feedBackType == null) {
					Toast.makeText(FeedbackActivity.this,
							R.string.feedback_type_input_prompt,
							Toast.LENGTH_LONG).show();
					return;
				}
				if (desp_input.getText().toString().trim().equals("")) {
					Toast.makeText(FeedbackActivity.this,
							R.string.feedback_desp_input_prompt,
							Toast.LENGTH_LONG).show();
					return;
				}
				String email = email_input.getText().toString().trim();
				if (!valid(email)) {
					Toast.makeText(FeedbackActivity.this,
							R.string.account_man_change_email_input_error_text,
							Toast.LENGTH_LONG).show();
					return;
				}
				submitFeedBack();
			}
		});
	}

	private boolean valid(String email) {
		if (TextUtils.isEmpty(email)) {
			return true;
		}
		Pattern pattern = Pattern
				.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]+?@([a-zA-Z0-9]+(?:\\.[a-zA-Z0-9-_]+){1,})$");
		return (pattern.matcher(email)).matches();
	}

	private void submitFeedBack() {
		if (mFeedingBack) {
			return;
		}

		//String mToken = Utilis.getToken();

		FeedbackProtoc.Feedback.Builder feedback = FeedbackProtoc.Feedback
				.newBuilder();
		String email = email_input.getText().toString().trim();
		if (!email.equals("")) {
			feedback.setEmail(email);
		}
		String desp = desp_input.getText().toString().trim();
		feedback.setContent(desp);

		feedback.setFeedbackType(fdType.get(spinner_feedback
				.getSelectedItemPosition()));

		FeedbackProtoc.FeedbackRequest.Builder feedbackRequ = FeedbackProtoc.FeedbackRequest
				.newBuilder();
		feedbackRequ.setFeedback(feedback);

		RequestProtoc.RequestContext.Builder context = RequestProtoc.RequestContext
				.newBuilder();
		//context.setAuthToken(mToken);

		RequestProtoc.Request.Builder request = RequestProtoc.Request
				.newBuilder();
		request.setFeedbackRequest(feedbackRequ);
		request.setContext(context);

		Base64Handler base64 = new Base64Handler();
		BufferedReader mReader = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet();
		int ret = -1;
		try {
			/*
			URI url = new URI(Constant.FEEDBACK_URL
					+ base64.encode(request.build().toByteArray()));
			httpGet.setURI(url);
			HttpResponse response = client.execute(httpGet);

			StringBuffer mBuffer = new StringBuffer();
			mReader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			
			String line = mReader.readLine();
			while (line != null) {
				mBuffer.append(line);
				line = mReader.readLine();
			}

			ResponseProtoc.Response resp = ResponseProtoc.Response
					.parseFrom(base64.decodeBuffer(mBuffer.toString()));
			*/
			HashMap<String, String> param = new HashMap<String, String>();
			param.put("request", base64.encode(request.build().toByteArray()));
			Map<String, Object> res = HttpRequestUtil.postData(
					Constant.FEEDBACK_URL, param, 10000);
			ResponseProtoc.Response resp = ResponseProtoc.Response.parseFrom(base64.decodeBuffer(new String((byte[])res.get("Content"))));
			
			if (resp.hasContext()) {
				ResponseContext rc = resp.getContext();
				ret = rc.getResult();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = -1;
		} finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (ret == 0) {
			feedSucc();
		} else if(ret == -1){
			Toast.makeText(FeedbackActivity.this, R.string.feedback_fail_text,
					Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(FeedbackActivity.this, R.string.feedback_fail_prompt,
					Toast.LENGTH_LONG).show();
		}
		mFeedingBack = false;

	}

	private void feedSucc() {
		Toast.makeText(FeedbackActivity.this, R.string.feedback_succ_text,
				Toast.LENGTH_LONG).show();
		desp_input.setText("");
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			
			desp_input.scrollTo(0, (int) distanceY);
			
			return true;

		}

		@Override
		public void onLongPress(MotionEvent e) { // Do nothing }
			
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		try {
			int location[] = new int[2];
			desp_input.getLocationOnScreen(location);
			Rect r = new Rect();
			r.left = location[0];
			r.top = location[1];
			r.right = r.left + desp_input.getWidth();
			r.bottom = r.top + desp_input.getHeight();
			if (r.contains((int) ev.getX(), (int) ev.getY())) {
				desp_input.requestFocus();
				return desp_input.onTouchEvent(ev);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.dispatchTouchEvent(ev);
	}

}
