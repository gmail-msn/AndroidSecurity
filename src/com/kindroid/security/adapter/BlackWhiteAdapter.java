package com.kindroid.security.adapter;

import java.text.Format;


import com.kindroid.security.R;
import com.kindroid.security.ui.InterceptBlackList;
import com.kindroid.security.ui.InterceptWhiteList;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.NativeCursor;
import com.kindroid.security.util.UtilDailog;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BlackWhiteAdapter extends CursorAdapter {
	private LayoutInflater mLayoutFlater;

	private Context mContext;
	private int mType;
	private int mPosition = -1;

	public BlackWhiteAdapter(Context context, Cursor c, boolean autoRequery,
			int type) {
		super(context, c, autoRequery);
		this.mType = type;
		mContext = context;
		mLayoutFlater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView mPhoneTv = (TextView) view.findViewById(R.id.phone_tv);
		TextView mRemarkTv = (TextView) view.findViewById(R.id.remarkets_tv);
		ImageView mSmsInterceptIv = (ImageView) view
				.findViewById(R.id.sms_intercept_iv);
		ImageView mPhoneInterceptIv = (ImageView) view
				.findViewById(R.id.phone_intercept_iv);
//		LinearLayout mHandleLinear = (LinearLayout) view
//				.findViewById(R.id.handle_linear);
		LinearLayout mDelLinear = (LinearLayout) view
				.findViewById(R.id.del_linear);
		LinearLayout mEditLinear = (LinearLayout) view
				.findViewById(R.id.edit_linear);
		LinearLayout mMoreLinear = (LinearLayout) view
				.findViewById(R.id.more_linear);

		NativeCursor nc = convertCursorToNativeCursor(cursor);

		mPhoneTv.setText(nc.getmPhoneNum() == null ? "" : nc.getmPhoneNum());
		mRemarkTv.setText(nc.getmContactName() == null ? "" : nc
				.getmContactName());

		mDelLinear.setOnClickListener(new LinearLister(nc));
		mEditLinear.setOnClickListener(new LinearLister(nc));
		mMoreLinear.setOnClickListener(new LinearLister(nc));
		if (mPosition == cursor.getPosition()) {
			mDelLinear.setVisibility(View.VISIBLE);
			mEditLinear.setVisibility(View.VISIBLE);
			mMoreLinear.setVisibility(View.VISIBLE);
		}else{
			mDelLinear.setVisibility(View.GONE);
			mEditLinear.setVisibility(View.GONE);
			mMoreLinear.setVisibility(View.GONE);
			
		}
		if (mType == 1) {
			mSmsInterceptIv.setVisibility(View.VISIBLE);
			mPhoneInterceptIv.setVisibility(View.VISIBLE);
			mSmsInterceptIv
					.setImageResource(nc.ismSmsStatus() ? R.drawable.sms_intercept_enable
							: R.drawable.sms_intercept_diable);
			mPhoneInterceptIv
					.setImageResource(nc.ismRingStatus() ? R.drawable.phone_intercept_enable
							: R.drawable.phone_intercept_disable);
			mSmsInterceptIv.setOnClickListener(new LinearLister(nc));
			mPhoneInterceptIv.setOnClickListener(new LinearLister(nc));
			if (nc.getmPhoneType() == 2) {
				mMoreLinear.setVisibility(View.GONE);

				String str_area = context.getString(R.string.area_text, nc
						.getmContactName() == null ? "" : nc.getmContactName());
				String str_code = context.getString(R.string.code_text,
						nc.getmPhoneNum() == null ? "" : nc.getmPhoneNum());

				mPhoneTv.setText(str_area);
				mRemarkTv.setText(str_code);
			}
		} else if (mType == 2) {
			if (nc.getmPhoneType() == 2) {
				mMoreLinear.setVisibility(View.GONE);

				String str_area = context.getString(R.string.area_text, nc
						.getmContactName() == null ? "" : nc.getmContactName());
				String str_code = context.getString(R.string.code_text,
						nc.getmPhoneNum() == null ? "" : nc.getmPhoneNum());

				mPhoneTv.setText(str_area);
				mRemarkTv.setText(str_code);
			}

		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		return mLayoutFlater.inflate(R.layout.intercept_black_white_item, null);
		
		
	}

	public int getmPosition() {
		return mPosition;
	}

	public void setmPosition(int mPosition) {
		this.mPosition = mPosition;
	}

	public void requeryCursor() {
		if (mType == 1) {

			((InterceptBlackList) mContext).refreshAdapter();

		} else if (mType == 2) {
			((InterceptWhiteList) mContext).refreshAdapter();
		}

	}

	class LinearLister implements View.OnClickListener {
		private NativeCursor nc;
		private Context parent_con;

		public LinearLister(NativeCursor nc) {
			this.nc = nc;
			if (mType == 1) {
				parent_con = ((InterceptBlackList) mContext).getParent();
			} else {
				parent_con=((InterceptWhiteList) mContext).getParent();
			}
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.sms_intercept_iv:
				nc.setmSmsStatus(!nc.ismSmsStatus());
				InterceptDataBase.get(mContext).UpdateBlackWhiteList(nc);
				requeryCursor();

				break;
			case R.id.phone_intercept_iv:
				nc.setmRingStatus(!nc.ismRingStatus());
				InterceptDataBase.get(mContext).UpdateBlackWhiteList(nc);
				requeryCursor();
				break;
			case R.id.edit_linear:
				int phoneType = nc.getmPhoneType();
				if(phoneType == 1){
					UtilDailog.getInsertBlackWhiteDialog(parent_con, nc.getmRequestType()==2?3:2, nc).show();
				}else{
					UtilDailog.getInceptBlackWhiteDialog(parent_con, nc.getmRequestType()==2?3:2, nc).show();
				}
				break;
			case R.id.del_linear:
				UtilDailog.getDelListDialog(parent_con, 1, nc).show();
				break;
			case R.id.more_linear:

				UtilDailog.getMoreDialog(parent_con, 1, nc,null,null).show();
				break;
			default:
				break;
			}

		}
	}

	private NativeCursor convertCursorToNativeCursor(Cursor c) {
		NativeCursor nc = new NativeCursor();
		try {
			nc.setmId(c.getInt(c.getColumnIndex(InterceptDataBase.ID)));
			nc.setmPhoneNum(c.getString(c
					.getColumnIndex(InterceptDataBase.PHONENUM)));

			nc.setmContactName(c.getString(c
					.getColumnIndex(InterceptDataBase.CONTACTNAME)));
			nc.setmSmsStatus(c.getInt(c
					.getColumnIndex(InterceptDataBase.SMSSTATUS)) > 0);
			nc.setmRingStatus(c.getInt(c
					.getColumnIndex(InterceptDataBase.RINGSTATUS)) > 0);
			nc.setmPhoneType(c.getInt(c
					.getColumnIndex(InterceptDataBase.PHONETYPE)));
			nc.setmRequestType(mType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nc;

	}

}
