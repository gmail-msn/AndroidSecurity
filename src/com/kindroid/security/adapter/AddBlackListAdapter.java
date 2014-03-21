package com.kindroid.security.adapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.model.BlackListItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class AddBlackListAdapter extends BaseAdapter {
	private List<BlackListItem> items;
	private Activity mActivity;
	
	public AddBlackListAdapter(Activity activity){
		this.mActivity = activity;
		this.items = new ArrayList<BlackListItem>();
	}
	public void sortItemsByContactName(final boolean desc){
		Collections.sort(items, new Comparator<BlackListItem>() {
			@Override
			public int compare(BlackListItem object1,
					BlackListItem object2) {
				// TODO Auto-generated method stub
				Locale locale = mActivity.getResources().getConfiguration().locale;
				Collator myCollator = Collator.getInstance(locale);
				String name1 = object1.getContactName();
				String name2 = object2.getContactName();
				if (desc) {
					if (myCollator.compare(name1, name2) < 0) {
						return -1;
					} else if (myCollator.compare(name1, name2) > 0) {
						return 1;
					} else {
						return 0;
					}
				} else {
					if (myCollator.compare(name2, name1) < 0) {
						return -1;
					} else if (myCollator.compare(name2, name1) > 0) {
						return 1;
					} else {
						return 0;
					}
				}
			}

		});
	}
	public void selectAllItem(boolean status){
		for(BlackListItem item : items){
			item.setSelected(status);
		}
	}
	public void addItem(BlackListItem item){
		items.add(item);
	}
	public void delItem(int position){
		items.remove(position);
	}
	public void clear(){
		items.clear();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
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
			LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
			convertView = inflater.inflate(R.layout.add_list_from_item, null);
		}
		final BlackListItem item = items.get(position);
		TextView list_item_title = (TextView)convertView.findViewById(R.id.list_item_title);
		list_item_title.setText(item.getContactName());
		TextView list_item_desp = (TextView)convertView.findViewById(R.id.list_item_desp);
		list_item_desp.setText(item.getContentDesp());
		CheckBox cb = (CheckBox)convertView.findViewById(R.id.select_cb);
		cb.setChecked(item.isSelected());
		/*
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				item.setSelected(isChecked);
			}
			
		});
		*/
		return convertView;
	}

}
