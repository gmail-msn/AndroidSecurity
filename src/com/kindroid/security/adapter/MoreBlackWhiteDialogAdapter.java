package com.kindroid.security.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.security.R;
import com.kindroid.security.util.HistoryNativeCursor;
import com.kindroid.security.util.NativeCursor;
public class MoreBlackWhiteDialogAdapter extends BaseAdapter {
	private int mDrableId[];
	private int  mStringId[];
	private Context context;
	private int mType;
	private NativeCursor mNc;
	private HistoryNativeCursor hNc;
	
	public MoreBlackWhiteDialogAdapter(Context context,int type,NativeCursor nc,int drableId[],int stringId[],HistoryNativeCursor hnc){
		this.mDrableId=drableId;
		this.mStringId=stringId;
		this.context=context;
		this.mType=type;
		this.mNc=nc;
		this.hNc=hnc;
	}


	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDrableId.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDrableId[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
			convertView = inflater.inflate(R.layout.more_dialog_item, null);
		}
		ImageView mIv=(ImageView) convertView.findViewById(R.id.appImageView);
		TextView appTextView= (TextView)convertView.findViewById(R.id.appTextView);
		mIv.setBackgroundResource(mDrableId[position]);
		if(mType==1){
			if (position==2||position==3) {
				String str=context.getString(mStringId[position], mNc.getmPhoneNum());
				appTextView.setText(str);
			}else{
				appTextView.setText(mStringId[position]);
			}
		}else if(mType==2){
			if (position==0) {
				appTextView.setText(mStringId[position]);
			}else{
				String str=context.getString(mStringId[position], hNc.getmAddress());
				appTextView.setText(str);
			}
			
		}
		return convertView;
	}

}
