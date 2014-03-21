package com.kindroid.security.util;

import com.kindroid.security.R;
import com.kindroid.security.adapter.InterceptHistoryAdapter;
import com.kindroid.security.adapter.InterceptTreatModeListAdapter;
import com.kindroid.security.adapter.MoreBlackWhiteDialogAdapter;

import java.util.Calendar;

import android.app.Dialog;

import android.content.ContentValues;

import android.app.TimePickerDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.sax.StartElementListener;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class UtilDailog {

	public static Dialog getInsertBlackWhiteDialog(final Context context,
			final int type, final NativeCursor nativeCursor) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.insert_edit_black_white_dialog,
				null);

		LinearLayout mMobileLinear = (LinearLayout) view
				.findViewById(R.id.mobile_linear);
		LinearLayout mRemarkLinear = (LinearLayout) view
				.findViewById(R.id.remark_linear);
		LinearLayout mInterceptRuleLinar = (LinearLayout) view
				.findViewById(R.id.intercept_linear);
		TextView mTypeStatusTv = (TextView) view
				.findViewById(R.id.type_status_tv);
		TextView mMobileTv = (TextView) view.findViewById(R.id.mobile_tv);
		final EditText mMobileEt = (EditText) view.findViewById(R.id.mobile_et);

		TextView mRemarkTv = (TextView) view.findViewById(R.id.remark_tv);
		final EditText mRemarkEt = (EditText) view.findViewById(R.id.remark_et);
		final CheckBox mInterceptSmsCb = (CheckBox) view
				.findViewById(R.id.intercept_sms_cb);
		final CheckBox mInterceptPhoneCb = (CheckBox) view
				.findViewById(R.id.intercept_phone_cb);

		Button mBtOk = (Button) view.findViewById(R.id.button_ok);
		Button mBtCancel = (Button) view.findViewById(R.id.button_cancel);
		mMobileTv.setText(R.string.number);
		mRemarkTv.setText(R.string.remark);

		mMobileEt.setText(nativeCursor.getmPhoneNum() == null ? ""
				: nativeCursor.getmPhoneNum());
		mRemarkEt.setText(nativeCursor.getmContactName() == null ? ""
				: nativeCursor.getmContactName());
		mInterceptSmsCb.setChecked(nativeCursor.ismSmsStatus());
		mInterceptPhoneCb.setChecked(nativeCursor.ismRingStatus());
		if (nativeCursor.getmRequestType() == 2) {
			mInterceptPhoneCb.setVisibility(View.GONE);
			mInterceptSmsCb.setVisibility(View.GONE);

		}

		if (type == 1) {
			if (nativeCursor.getmRequestType() == 1) {
				mTypeStatusTv.setText(R.string.add_black_list);
			} else {
				mTypeStatusTv.setText(R.string.add_white_list);
			}

		} else if (type == 2) {
			mTypeStatusTv.setText(R.string.edit_black_list);
		} else if (type == 3) {
			mTypeStatusTv.setText(R.string.edit_white_list);
		}
		mBtOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str = mMobileEt.getText().toString().trim();
				
				boolean isTrue = checkMessage(context, str);
				if (isTrue) {
					if (type == 1) {
						// NativeCursor nc = new NativeCursor();

						nativeCursor.setmPhoneNum(str);
						NativeCursor nc = InterceptDataBase.get(context)
								.selectIsExists(nativeCursor);
						if (!nc.ismIsExists()) {
							String contactName = mRemarkEt.getText().toString()
									.trim();
							boolean mSmsStatus = mInterceptSmsCb.isChecked();
							boolean mRingStatus = mInterceptPhoneCb.isChecked();

							InterceptDataBase.get(context).insertBlackWhitList(
									nc.getmRequestType(), contactName, str,
									mSmsStatus, mRingStatus, 1);
							Toast.makeText(context, R.string.add_list_complete, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(context, R.string.number_already_exist, Toast.LENGTH_LONG).show();
							return;
						}
					} else if (type == 2 || type == 3) {
						// NativeCursor nc = new NativeCursor();
						nativeCursor.setmPhoneNum(str);
						String contactName = mRemarkEt.getText().toString()
								.trim();
						nativeCursor.setmContactName(contactName);
						nativeCursor.setmSmsStatus(mInterceptSmsCb.isChecked());
						nativeCursor.setmRingStatus(mInterceptPhoneCb
								.isChecked());
						InterceptDataBase.get(context).UpdateBlackWhiteList(
								nativeCursor);
						context.sendBroadcast(new Intent(
								Constant.BROACTUPDATEINTERCEPT));

					}
					dialog.cancel();					
				}
								
			}
		});
		mBtCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});

		dialog.setContentView(view);

		return dialog;

	}

	public static Dialog getInceptBlackWhiteDialog(final Context context,
			final int type, final NativeCursor nativeCursor) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.add_list_from_region_dialog,
				null);

		TextView mPromptTv = (TextView) view.findViewById(R.id.edit_prompt_tv);
		final EditText mInputEt = (EditText) view.findViewById(R.id.input_et);
		final CheckBox mInterceptSmsCb = (CheckBox) view
				.findViewById(R.id.intercept_sms_cb);
		final CheckBox mInterceptPhoneCb = (CheckBox) view
				.findViewById(R.id.intercept_phone_cb);
		if (nativeCursor.getmRequestType() == 2) {
			mInterceptPhoneCb.setVisibility(View.GONE);
			mInterceptSmsCb.setVisibility(View.GONE);

		}
		if(nativeCursor != null){
			if(nativeCursor.getmPhoneNum() != null && !nativeCursor.getmPhoneNum().equals("")){
				mInputEt.setText(nativeCursor.getmPhoneNum());
			}
		}
		Button mBtOk = (Button) view.findViewById(R.id.button_ok);
		Button mBtCancel = (Button) view.findViewById(R.id.button_cancel);
		mInterceptSmsCb.setChecked(nativeCursor.ismSmsStatus());
		mInterceptPhoneCb.setChecked(nativeCursor.ismRingStatus());
		if (nativeCursor.getmRequestType() == 1) {
			if(type != 1){
				mPromptTv.setText(R.string.add_blacklist_from_region_prompt);
			}else{
				mPromptTv.setText(R.string.add_black_area);
			}
		} else if (nativeCursor.getmRequestType() == 2) {
			if(type != 1){
				mPromptTv.setText(R.string.add_whitelist_from_region_prompt);
			}else{
				mPromptTv.setText(R.string.add_white_area);
			}
		}
		mBtOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str = mInputEt.getText().toString().trim();
				if (TextUtils.isEmpty(str)) {
					Toast.makeText(context,
							R.string.add_list_from_region_empty_prompt,
							Toast.LENGTH_LONG).show();
					return;
				}
				boolean isRegionCode = isRegionCode(str);
				boolean validRegionCode = true;
				if (isRegionCode) {
					validRegionCode = validRegionCode(str);
				}
				if (isRegionCode && !validRegionCode) {
					Toast.makeText(context,
							R.string.add_list_from_region_code_error,
							Toast.LENGTH_LONG).show();
					return;
				}
				if (type == 1) {
					NativeCursor nc = new NativeCursor();
					nc.setmRequestType(nativeCursor.getmRequestType());
					if (isRegionCode) {
						String cityName = ConvertUtils.getCityNameByCode(
								context, str);
						nc.setmPhoneNum(str);
						if (cityName != null) {
							nc.setmContactName(cityName);
						}else{
							Toast.makeText(context,
									R.string.add_region_code_unexist,
									Toast.LENGTH_LONG).show();
							return;
						}
					} else {
						String regionCode = ConvertUtils.getCodeByCityName(
								context, str);
						if (regionCode == null) {
							Toast.makeText(context,
									R.string.add_list_from_region_code_unexist,
									Toast.LENGTH_LONG).show();
							return;
						}
						nc.setmPhoneNum(regionCode);
						nc.setmContactName(str);
					}
					nc.setmRequestType(nativeCursor.getmRequestType());
					nc = InterceptDataBase.get(context).selectIsExists(nc);
					if (!nc.ismIsExists()) {
						String contactName = nc.getmContactName();
						boolean mSmsStatus = mInterceptSmsCb.isChecked();
						boolean mRingStatus = mInterceptPhoneCb.isChecked();

						InterceptDataBase.get(context).insertBlackWhitList(
								nc.getmRequestType(), contactName, nc.getmPhoneNum(),
								mSmsStatus, mRingStatus, 2);
						Toast.makeText(context, R.string.add_list_complete, Toast.LENGTH_LONG).show();
					}else{
						if(isRegionCode){
							Toast.makeText(context, R.string.area_code_existed, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(context, R.string.area_existed, Toast.LENGTH_LONG).show();
						}
						return;
					}
				} else if (type == 2 || type == 3) {
					// NativeCursor nc = new NativeCursor();
					if (isRegionCode) {
						String cityName = ConvertUtils.getCityNameByCode(
								context, str);
						nativeCursor.setmPhoneNum(str);
						if (cityName != null) {
							nativeCursor.setmContactName(cityName);
						}else{
							Toast.makeText(context,
									R.string.add_region_code_unexist,
									Toast.LENGTH_LONG).show();
							return;
						}
					} else {
						String regionCode = ConvertUtils.getCodeByCityName(
								context, str);
						if (regionCode == null) {
							Toast.makeText(context,
									R.string.add_list_from_region_code_unexist,
									Toast.LENGTH_LONG).show();
							return;
						}
						nativeCursor.setmPhoneNum(regionCode);						
						nativeCursor.setmContactName(str);
					}

					nativeCursor.setmSmsStatus(mInterceptSmsCb.isChecked());
					nativeCursor.setmRingStatus(mInterceptPhoneCb.isChecked());
					InterceptDataBase.get(context).UpdateBlackWhiteList(
							nativeCursor);
					context.sendBroadcast(new Intent(
							Constant.BROACTUPDATEINTERCEPT));

				}

				dialog.cancel();
			}
		});
		mBtCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});

		dialog.setContentView(view);

		return dialog;
	}

	public static boolean validRegionCode(String str) {
		return TextUtils.isDigitsOnly(str);
	}

	public static boolean isRegionCode(String str) {
		//return str.startsWith("0");
		return TextUtils.isDigitsOnly(str);
	}

	public static Dialog getDelListDialog(final Context context,
			final int type, final NativeCursor nc) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.sure_del_dialog, null);
		dialog.setContentView(view);

		TextView promptText = (TextView) view.findViewById(R.id.prompt_text);

		promptText.setText(R.string.sure_del_the_list);
		Button button_ok = (Button) view.findViewById(R.id.button_ok);
		Button button_cancel = (Button) view.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InterceptDataBase.get(context).DelBlackWhiteList(nc);
				context.sendBroadcast(new Intent(Constant.BROACTUPDATEINTERCEPT));
				dialog.dismiss();

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;

	}

	public static Dialog getDelHistoryDialog(final Context context,
			final HistoryNativeCursor hnc, final InterceptHistoryAdapter adapter) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.sure_del_dialog, null);
		dialog.setContentView(view);

		TextView promptText = (TextView) view.findViewById(R.id.prompt_text);

		promptText.setText(R.string.sure_del_log);
		Button button_ok = (Button) view.findViewById(R.id.button_ok);
		Button button_cancel = (Button) view.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InterceptDataBase.get(context).DelHistory(hnc);
				if(hnc.getmRequestType() == 3){
					UpdateStaticsThread ust = new UpdateStaticsThread(hnc.getmBody(), 1, context);
					ust.start();
				}
				adapter.removePosition();
				dialog.dismiss();

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;

	}

	public static Dialog getBackHistoryDialog(final Context context,
			final HistoryNativeCursor hnc, final InterceptHistoryAdapter adapter) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.sure_del_dialog, null);
		dialog.setContentView(view);

		TextView promptText = (TextView) view.findViewById(R.id.prompt_text);

		promptText.setText(R.string.sure_back_this_sms);
		Button button_ok = (Button) view.findViewById(R.id.button_ok);
		Button button_cancel = (Button) view.findViewById(R.id.button_cancel);
		button_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InterceptDataBase.get(context).DelHistory(hnc);
				adapter.removePosition();
				String mBody = hnc.getmBody();
				InsertSmstoBox(context, hnc);
				UpdateStaticsThread ust = new UpdateStaticsThread(mBody, 0, context);
				ust.start();
				dialog.dismiss();

			}
		});
		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;

	}

	public static Dialog getMoreDialog(final Context context, final int type,
			final NativeCursor nc, final HistoryNativeCursor hnc,
			final InterceptHistoryAdapter mAdapter) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.more_balck_whitelist_dialog,
				null);

		dialog.setContentView(view);

		ListView mListView = (ListView) view.findViewById(R.id.listproc);
		MoreBlackWhiteDialogAdapter adapter = null;
		if (type == 1) {
			int stringId[] = new int[] { R.string.softmanage_apk_delete_text,
					R.string.edit, R.string.dial_phone_num,
					R.string.send_sms_phone_num };

			int drableId[] = new int[] { R.drawable.remote_del_data_icon,
					R.drawable.intercept_edit_icon, R.drawable.phone_icon,
					R.drawable.sms_icon };
			adapter = new MoreBlackWhiteDialogAdapter(context, 1, nc, drableId,

			stringId, hnc);

		} else if (type == 2) {
			int stringId[] = new int[] { R.string.softmanage_apk_delete_text,
					R.string.back_to_inbox, R.string.add_phonenum_to_blacklist,
					R.string.add_phonenum_to_whitelist,
					R.string.save_phonenum_as_contact, R.string.call_phonenum,
					R.string.back_sms_to_phone };

			int drableId[] = new int[] { R.drawable.remote_del_data_icon,
					R.drawable.back_sms,

					R.drawable.blacklist_icon, R.drawable.whitelist_icon,
					R.drawable.contac_person, R.drawable.phone_icon,
					R.drawable.sms_icon };
			adapter = new MoreBlackWhiteDialogAdapter(context, 2, nc, drableId,

			stringId, hnc);

		} else if (type == 3) {

			int stringId[] = new int[] { R.string.softmanage_apk_delete_text,
					R.string.add_phonenum_to_blacklist,
					R.string.add_phonenum_to_whitelist,
					R.string.save_phonenum_as_contact, R.string.call_phonenum,
					R.string.back_sms_to_phone };

			int drableId[] = new int[] { R.drawable.remote_del_data_icon,
					R.drawable.blacklist_icon, R.drawable.whitelist_icon,
					R.drawable.contac_person, R.drawable.phone_icon,
					R.drawable.sms_icon };
			adapter = new MoreBlackWhiteDialogAdapter(context, 2, nc, drableId,

			stringId, hnc);

		}
		mListView.setAdapter(adapter);

		Button button_cancel = (Button) view.findViewById(R.id.button_cancel);

		button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (type == 3 && arg2 > 0) {
					arg2++;
				}

				switch (arg2) {
				case 0:
					if (type == 1) {
						UtilDailog.getDelListDialog(context, type, nc).show();
					} else if (type == 2 || type == 3) {
						UtilDailog.getDelHistoryDialog(context, hnc, mAdapter)
								.show();
					}

					break;
				case 1:
					if (type == 1) {
						UtilDailog.getInsertBlackWhiteDialog(context,
								nc.getmRequestType() == 2 ? 3 : 2, nc).show();
					} else if (type == 2) {

						getBackHistoryDialog(context, hnc, mAdapter).show();

					}

					break;
				case 2:
					if (type == 1) {
						String telNo = nc.getmPhoneNum();
						if ((telNo != null) && (!"".equals(telNo.trim()))) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse("tel:" + telNo));
							context.startActivity(intent);
						}
					} else if (type == 2 || type == 3) {
						NativeCursor nc = new NativeCursor();
						nc.setmRequestType(1);
						nc.setmPhoneNum(hnc.getmAddress());
						nc = InterceptDataBase.get(context).selectIsExists(nc);
						if (!nc.ismIsExists()) {
							InterceptDataBase.get(context).insertBlackWhitList(
									1, "", hnc.getmAddress(), true, true, 1);
							Toast.makeText(context, R.string.add_list_complete, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(context, R.string.number_already_exist, Toast.LENGTH_LONG).show();
						}

					}

					break;
				case 3:
					if (type == 1) {
						String telNoSms = nc.getmPhoneNum();
						if ((telNoSms != null) && (!"".equals(telNoSms.trim()))) {
							Intent intent = new Intent(Intent.ACTION_SENDTO,
									Uri.parse("smsto:" + telNoSms));
							context.startActivity(intent);
						}
					} else if (type == 2 || type == 3) {
						NativeCursor nc = new NativeCursor();
						nc.setmRequestType(2);
						nc.setmPhoneNum(hnc.getmAddress());
						nc = InterceptDataBase.get(context).selectIsExists(nc);
						if (!nc.ismIsExists()) {
							InterceptDataBase.get(context).insertBlackWhitList(
									2, "", hnc.getmAddress(), true, true, 1);
							Toast.makeText(context, R.string.add_list_complete, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(context, R.string.number_already_exist, Toast.LENGTH_LONG).show();
						}
					}

					break;
				case 4:
					if (type == 1) {

					} else if (type == 2||type==3) {
						if(!numberExistInContacts(hnc.getmAddress())){
							Intent createIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
	                        createIntent.setType(Contacts.CONTENT_ITEM_TYPE);
	                        createIntent.putExtra(Insert.PHONE, hnc.getmAddress());
	                        context.startActivity(createIntent);
						}else{
							Toast.makeText(context, R.string.number_already_exist, Toast.LENGTH_LONG).show();
						}

					}

					break;
				case 5:
					if (type == 1) {

					} else if (type == 2 || type == 3) {
						String telNo = hnc.getmAddress();
						if ((telNo != null) && (!"".equals(telNo.trim()))) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse("tel:" + telNo));
							context.startActivity(intent);
						}

					}
					break;
				case 6:
					if (type == 1) {

					} else if (type == 2 || type == 3) {
						String telNoSms = hnc.getmAddress();
						if ((telNoSms != null) && (!"".equals(telNoSms.trim()))) {
							Intent intent = new Intent(Intent.ACTION_SENDTO,
									Uri.parse("smsto:" + telNoSms));
							context.startActivity(intent);
						}

					}
					break;
				}

				dialog.cancel();

			}
		});

		return dialog;

	}
	public static boolean numberExistInContacts(String num){
		return false;
	}

	public static void InsertSmstoBox(Context context, HistoryNativeCursor hnc) {

		final String ADDRESS = "address";
		final String DATE = "date";
		final String READ = "read";
		final String STATUS = "status";
		final String TYPE = "type";
		final String BODY = "body";
		ContentValues values = new ContentValues();
		/* 手机号 */
		values.put(ADDRESS, hnc.getmAddress());
		/* 时间 */
		if (hnc.getmDate() != null && hnc.getmDate().length() > 0) {
			try {
				long time = InterceptDataBase.DF_DATE.parse(hnc.getmDate())
						.getTime();
				values.put(DATE, time);
			} catch (java.text.ParseException pe) {
				pe.printStackTrace();
			}

		}

		values.put(READ, 1);
		values.put(STATUS, -1);
		/* 类型1为收件箱，2为发件箱 */
		values.put(TYPE, 1);
		/* 短信体内容 */
		values.put(BODY, hnc.getmBody());
		/* 插入数据库操作 */
		context.getContentResolver().insert(
				Uri.parse("content://sms"), values);

	}

	public static boolean checkMessage(Context context, String str) {
		boolean isTrue = true;
		if (str.equals("")) {
			Toast.makeText(context, R.string.phone_number_not_empty,
					Toast.LENGTH_SHORT).show();
			isTrue = false;
		}else if(!PhoneNumberUtils.isGlobalPhoneNumber(str)){
			Toast.makeText(context, R.string.input_valid_number,
					Toast.LENGTH_SHORT).show();
			isTrue = false;
		}else if (str.startsWith("1")) {

			isTrue = ConvertUtils.isCellphone(str);
			if (!isTrue) {
				Toast.makeText(context, R.string.input_valide_mobile,
						Toast.LENGTH_SHORT).show();
			}

		} else if (str.startsWith("0")) {
			PhoneType pt = ConvertUtils.getPhonetype(str);
			if(pt.getPhonetype() == 0){
				isTrue = false;
				Toast.makeText(context, R.string.input_mobile_as_rule,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			isTrue = false;
			Toast.makeText(context, R.string.input_mobile_as_rule,
					Toast.LENGTH_SHORT).show();

		}
		return isTrue;
	}

	public static Dialog getInterceptTreadDialog(Context context) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.select_intercept_treat_dialog,
				null);
		dialog.setContentView(view);		
		Button mBtCancel = (Button) view.findViewById(R.id.button_cancel);
		mBtCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		return dialog;
	}
	
	public static Dialog getTimeSettingDialog(final Context context) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.intercept_time_setting_dialog,
				null);
		dialog.setContentView(view);
		final EditText startEt = (EditText) dialog
				.findViewById(R.id.start_time_et);
		final EditText endEt = (EditText) dialog.findViewById(R.id.end_time_et);
		startEt.setOnClickListener(new EditTimeListener(context, startEt));
		endEt.setOnClickListener(new EditTimeListener(context, endEt));
		Button mBtCancel = (Button) dialog.findViewById(R.id.button_cancel);
		mBtCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		return dialog;
	}

	public static Dialog getDaySettingDialog(final Context context) {
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Dialog dialog = new Dialog(context, R.style.Theme_CustomDialog);
		View view = mInflater.inflate(R.layout.intercept_setting_day_dialog,
				null);
		dialog.setContentView(view);
		Button mBtCancel = (Button) dialog.findViewById(R.id.button_cancel);
		mBtCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		return dialog;
	}

}
