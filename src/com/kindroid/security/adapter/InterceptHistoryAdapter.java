package com.kindroid.security.adapter;

import java.util.List;



import com.kindroid.security.R;
import com.kindroid.security.ui.InterceptHistoryPhone;
import com.kindroid.security.ui.InterceptHistorySms;

import com.kindroid.security.util.HistoryNativeCursor;
import com.kindroid.security.util.InterceptDataBase;
import com.kindroid.security.util.NativeCursor;
import com.kindroid.security.util.UtilDailog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.widget.LinearLayout;
import android.widget.TextView;

public class InterceptHistoryAdapter extends BaseAdapter {
	private LayoutInflater mLayoutFlater;

	private Context mContext;
	private int mType;
	private int mPosition = -1;
	private List<HistoryNativeCursor> mCursors;
	private int mViewId;

	public InterceptHistoryAdapter(Context context,
			List<HistoryNativeCursor> cursors, int type) {
		super();
		this.mType = type;
		mContext = context;
		this.mCursors = cursors;
		mLayoutFlater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (type == 3) {
			mViewId = R.layout.intercept_history_sms_item;
		} else if (type == 4) {
			mViewId = R.layout.intercept_history_phone_item;
		}

	}

	public int getmPosition() {
		return mPosition;
	}

	public void setmPosition(int mPosition) {
		this.mPosition = mPosition;
	}

	public void removePosition() {
		if (mCursors.size() > mPosition && mPosition != -1) {
			mCursors.remove(mPosition);
			mPosition = -1;
			notifyDataSetChanged();
			if (mType == 3) {
				((InterceptHistorySms) mContext).setNumTv(mCursors.size());
				((InterceptHistorySms) mContext).refreshCheckBox(true);

			} else {
				((InterceptHistoryPhone) mContext).setNumTv(mCursors.size());
				((InterceptHistoryPhone) mContext).refreshCheckBox(true);
			}

		}
	}

	class LinearLister implements View.OnClickListener {
		private HistoryNativeCursor nc;
		private Context parent_con;

		public LinearLister(HistoryNativeCursor nc) {
			this.nc = nc;
			if (mType == 3) {
				parent_con = ((InterceptHistorySms) mContext).getParent();
			} else {
				parent_con = ((InterceptHistoryPhone) mContext).getParent();
			}
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {

			case R.id.edit_linear:
				if(mType==3){
					UtilDailog.getBackHistoryDialog(parent_con, nc,
							InterceptHistoryAdapter.this).show();
				}else{
					NativeCursor nc_copy=new NativeCursor();
					nc_copy.setmRequestType(1);
					nc_copy.setmPhoneNum(nc.getmAddress());
					nc_copy.setmRingStatus(true);
					nc_copy.setmSmsStatus(true);
					//UtilDailog.getInsertBlackWhiteDialog(parent_con, 1, nc_copy).show();					
					nc_copy = InterceptDataBase.get(mContext).selectIsExists(nc_copy);
					if (!nc_copy.ismIsExists()) {
						InterceptDataBase.get(mContext).insertBlackWhitList(
								1, "", nc_copy.getmPhoneNum(), true, true, 1);
						Toast.makeText(mContext, R.string.add_list_complete, Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(mContext, R.string.number_already_exist, Toast.LENGTH_LONG).show();
					}
				}

				
				break;
			case R.id.del_linear:

				UtilDailog.getDelHistoryDialog(parent_con, nc,
						InterceptHistoryAdapter.this).show();
				break;
			case R.id.more_linear:

				if(mType==3){
					UtilDailog.getMoreDialog(parent_con, 2, null, nc,
							InterceptHistoryAdapter.this).show();
				}else{
					UtilDailog.getMoreDialog(parent_con, 3, null, nc,
							InterceptHistoryAdapter.this).show();
				}
				
				break;
			default:
				break;
			}

		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mCursors.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mCursors.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView == null){
			convertView = mLayoutFlater.inflate(mViewId, null);
		}

		if(position>=mCursors.size()){
			return convertView;
		}

		final HistoryNativeCursor hnc = mCursors.get(position);

		CheckBox cb = (CheckBox) convertView.findViewById(R.id.select_cb);
		cb.setOnCheckedChangeListener(null);
		cb.setChecked(hnc.isSelect());

		LinearLayout mHandleLinear = (LinearLayout) convertView
				.findViewById(R.id.handle_linear);
		LinearLayout mDelLinear = (LinearLayout) convertView
				.findViewById(R.id.del_linear);
		LinearLayout mEditLinear = (LinearLayout) convertView
				.findViewById(R.id.edit_linear);
		LinearLayout mMoreLinear = (LinearLayout) convertView
				.findViewById(R.id.more_linear);
		mDelLinear.setOnClickListener(new LinearLister(hnc));
		mEditLinear.setOnClickListener(new LinearLister(hnc));
		mMoreLinear.setOnClickListener(new LinearLister(hnc));
		TextView mRemarkTv = (TextView) convertView
		.findViewById(R.id.remarkets_tv);
		if (mPosition == position) {
			mHandleLinear.setVisibility(View.VISIBLE);
			mRemarkTv.setSingleLine(false);
			
		}else{
			mRemarkTv.setSingleLine(true);
			mHandleLinear.setVisibility(View.GONE);
		}
		TextView mPhoneTv = (TextView) convertView.findViewById(R.id.phone_tv);
		TextView mDateTv = (TextView) convertView.findViewById(R.id.date_tv);
		

		if (mType == 3) {
			mPhoneTv.setText(hnc.getmAddress() == null ? "" : hnc.getmAddress());
			mDateTv.setText(hnc.getmDate() == null ? "" : hnc.getmDate());
			mRemarkTv.setText(hnc.getmBody() == null ? "" : hnc.getmBody());

		} else if (mType == 4) {
			mPhoneTv.setText(hnc.getmAddress() == null ? "" : hnc.getmAddress());
			mDateTv.setText(hnc.getmRemark() == null ? "" : hnc.getmRemark());
			mRemarkTv.setText(hnc.getmDate() == null ? "" : hnc.getmDate());

		}

		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				hnc.setSelect(isChecked);
				if(mType==3){
					((InterceptHistorySms)mContext).refreshCheckBox(isChecked);
				}else{
					((InterceptHistoryPhone)mContext).refreshCheckBox(isChecked);
				}
				
			
				
			}
		});

		return convertView;
	}

}
