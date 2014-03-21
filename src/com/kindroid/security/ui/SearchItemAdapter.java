/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:	heli.zhao
 * Date: 2011-07
 * Description:
 */

package com.kindroid.security.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.kindroid.security.R;

public class SearchItemAdapter extends BaseAdapter {

	private List<String[]> words;
	private Context ctx;

	public SearchItemAdapter(Context context, List<String[]> words) {
		this.ctx = context;
		this.words = words;
	}

	public SearchItemAdapter(Context context) {
		this.ctx = context;
	}

	public void setWords(List<String[]> words) {
		this.words = words;
	}

	public List<String[]> getWords() {
		return words;
	}

	public int getCount() {
		return words.size();
	}

	public Object getItem(int location) {
		return words.get(location);
	}

	public long getItemId(int index) {
		return index;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.hot_app_item, null);

		if (words != null && !words.isEmpty() && words.size() > 0) {
			TextView title = (TextView) v.findViewById(R.id.hotAppTextView);

			String[] word = words.get(position);

			if (null != title) {
				title.setText(word[0]);
				if (word[0].equals("淘宝")) {
					title.setTextColor(Color.parseColor("#FF7E00"));
				} else if (word[0].equals("极游网")) {
					title.setTextColor(android.graphics.Color.BLUE);
				}
			}

			int idx = Integer.parseInt(word[1]);
//			title.setTextSize(10 + idx * 2);
			title.setTextSize(12 + idx);

			// params: left, top, right, bottom
			if (position % 4 == 0) {
				title.setPadding(5, 1, 1, 1);
			} else if (position % 4 == 1) {
//				title.setPadding(1, 20, 1, 1);
				title.setPadding(1, 13, 1, 1);
			} else if (position % 4 == 2) {
				title.setPadding(1, 1, 5, 1);
			} else if (position % 4 == 3) {
//				title.setPadding(1, 1, 1, 20);
				title.setPadding(1, 1, 1, 13);
			}

		}
		return v;
	}

}
