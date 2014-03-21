package com.kindroid.security.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.kindroid.security.R;
import com.kindroid.security.util.ProcInfo;
import com.kindroid.security.util.TaskUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class CleanListAdapter extends BaseAdapter {
	private Context mContext;
	private List<ProcInfo> mItems;
	private LayoutInflater mLayoutFlater;

	public CleanListAdapter(Context context) {
		mContext = context;
		mItems = new ArrayList<ProcInfo>();
		mLayoutFlater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addItem(ProcInfo item) {
		mItems.add(item);
	}

	public void delItem(ProcInfo item) {
		mItems.remove(item);
	}

	public void delItem(int index) {
		mItems.remove(index);
	}

	public void addItemAll(Collection<ProcInfo> allItems) {
		mItems.addAll(allItems);
	}

	public void clearItems() {
		mItems.clear();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = mLayoutFlater.inflate(R.layout.clean_list_item, null);
		}
		final ImageView procIconIv = (ImageView) convertView
				.findViewById(R.id.clean_item_image_view);
		final ProcInfo item = mItems.get(position);
		procIconIv.setImageDrawable(item.getIcon(mContext));
		
		final int p = position;
		
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.scale_anim); 
				anim.setAnimationListener(new AnimationListener(){

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						TaskUtil.killProcess(item.getPackageName(), mContext);					
						delItem(p);				
						notifyDataSetChanged();
						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
				});
				procIconIv.startAnimation(anim);				
			}
		});
		
		return convertView;
	}

}
