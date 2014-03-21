package com.kindroid.security.ui;

import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.security.R;
import com.kindroid.security.util.FirewallApi;
import com.kindroid.security.util.FirewallApi.FirewallApp;
import com.kindroid.security.util.MemoryUtil;

public class AppItemAdapter extends BaseAdapter {
	private static final int NET_TYPE_WIFI = 0;
	private static final int NET_TYPE_3G = 1;
	private FirewallApp[] list;
	private Context ctx;

	public AppItemAdapter(Context context, FirewallApp[] toList){
		this.ctx=context;
		this.list=toList;
	}

	public int getCount() {
		return list.length;
	}

	public Object getItem(int location) {
		return list[location];
	}

	public long getItemId(int index) {
		return index;
	}
	
	public FirewallApp[] getItems() {
		return list;
	}
	
	public static void sortApp(FirewallApp[] apps) {
		Arrays.sort(apps, new Comparator<FirewallApp>() {
			@Override
			public int compare(FirewallApp o1, FirewallApp o2) {
			/*	if ((o1.selected_wifi | o1.selected_3g) == (o2.selected_wifi | o2.selected_3g)) {
					return o1.names[0].compareTo(o2.names[0]);
				}*/
				if ((o1.selected_wifi || o1.selected_3g) && !o2.selected_3g && !o2.selected_wifi) {
					return 1;
				}else if(!o1.selected_wifi && !o1.selected_3g && !o2.selected_3g && !o2.selected_wifi){
					return o1.names[0].compareTo(o2.names[0]);
				}else if(!o1.selected_wifi && !o1.selected_3g && (o2.selected_3g || o2.selected_wifi)){
					return -1;
				}else if(o1.selected_wifi && o1.selected_3g && o2.selected_3g && o2.selected_wifi){
					return o1.names[0].compareTo(o2.names[0]);
				}
				/*if (o1.selected_wifi || o1.selected_3g)
					return -1;*/
			
				return -1;
			}
		});
	}
	
   	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			

		if (convertView == null) {
			// Inflate a new view
			convertView = inflater.inflate(R.layout.listitem, null);
		} 
		
//		final CheckBox box_wifi = (CheckBox) convertView.findViewById(R.id.itemcheck_wifi);
//		final CheckBox box_3g = (CheckBox) convertView.findViewById(R.id.itemcheck_3g);
		final ImageView g3ImageView = (ImageView) convertView.findViewById(R.id.g3ImageView);
		final ImageView wifiImageView = (ImageView) convertView.findViewById(R.id.wifiImageView);
		
		final ImageView image = (ImageView) convertView.findViewById(R.id.appImageicon);

		TextView text = (TextView) convertView.findViewById(R.id.itemtext);
		TextView totalDay = (TextView) convertView.findViewById(R.id.netTraficDayTotalTextView);
		TextView totalMonth = (TextView) convertView.findViewById(R.id.netTraficMonthTotalTextView);

//		box_wifi.setOnCheckedChangeListener(this);
//		box_3g.setOnCheckedChangeListener(this);
		
		final FirewallApp app = list[position];
		text.setText(app.toString());
//		box_wifi.setTag(app);
//		box_wifi.setChecked(app.selected_wifi);
//		box_3g.setTag(app);
//		box_3g.setChecked(app.selected_3g);
		
		if (app.selected_wifi) {
			wifiImageView.setImageResource(R.drawable.icon_yunxu04);
		} else {
			wifiImageView.setImageResource(R.drawable.icon_jinzhi03);
		}
		
		if (app.selected_3g) {
			g3ImageView.setImageResource(R.drawable.icon_yunxu04);
		} else {
			g3ImageView.setImageResource(R.drawable.icon_jinzhi03);
		}
		
		Handler mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				Boolean[] result = (Boolean[]) msg.obj;
				if (result[0]) {
//					sortApp(list);
					FirewallApi.saveRules(ctx, AppItemAdapter.this);
				} else {
					switch (msg.arg1) {
					case R.id.wifiImageView:
						if (result[1]) {
							app.selected_wifi = true;
						} else {
							app.selected_wifi = false;
						}
						break;
					case R.id.g3ImageView:
						if (result[1]) {
							app.selected_3g = true;
						} else {
							app.selected_3g = false;
						}
						break;
					default:
						break;
					}
					Toast.makeText(ctx, ctx.getResources().getString(R.string.firewall_not_surpport), Toast.LENGTH_SHORT).show();
				}
				wifiImageView.setClickable(true);
				g3ImageView.setClickable(true);
				notifyDataSetChanged();
			}
			
		};
		
		wifiImageView.setOnClickListener(new ImageClickLisener(app, app.selected_wifi, mHandler));
		g3ImageView.setOnClickListener(new ImageClickLisener(app, app.selected_3g, mHandler));
		
		totalDay.setText(MemoryUtil.formatMemoryBySize(1, app.total_day));
		totalMonth.setText(MemoryUtil.formatMemoryBySize(1, app.total_month));
		
		Drawable icon = app.getIcon();
		
		if (icon == null) {
			image.setImageResource(R.drawable.icondefault);
		} else {
			image.setImageDrawable(icon);
		}

		return convertView;
	}
   	
	/**
	 * Called an application is check/unchecked
	 */
	/*@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final FirewallApp app = (FirewallApp) buttonView.getTag();
		if (app != null) {
			switch (buttonView.getId()) {
				case R.id.itemcheck_wifi: 
					app.selected_wifi = isChecked; 
					break;
				case R.id.itemcheck_3g: 
					app.selected_3g = isChecked; 
					break;
			}
			FirewallApi.applyIptablesRules(ctx, false);
		}
	}*/
	
	private static class ListEntry {
		private CheckBox box_wifi;
		private CheckBox box_3g;
		private TextView text;
	}

	class ImageClickLisener implements View.OnClickListener {
		FirewallApp app;
		boolean allowOrNot;
		Handler mHandler;
		public ImageClickLisener(FirewallApp apps, boolean isAllow, Handler handler) {
			this.allowOrNot = isAllow;
			this.app = apps;
			this.mHandler = handler;
		}
		
		@Override
		public void onClick(View view) {
			ImageView imgVW = (ImageView) view;
			switch (view.getId()) {
			case R.id.wifiImageView:
				if (allowOrNot) {
					app.selected_wifi = false;
					imgVW.setImageResource(R.drawable.firewall_grey_allow_icon);
				} else {
					app.selected_wifi = true;
					imgVW.setImageResource(R.drawable.firewall_grey_forbid_icon);
				}
				break;
			case R.id.g3ImageView:
				if (allowOrNot) {
					app.selected_3g = false;
					imgVW.setImageResource(R.drawable.firewall_grey_allow_icon);
				} else {
					app.selected_3g = true;
					imgVW.setImageResource(R.drawable.firewall_grey_forbid_icon);
				}
				break;
			default:
				break;
			}
			imgVW.setClickable(false);
			
			new DealingData(view.getId(), allowOrNot, mHandler).start();
//			boolean isSuccess = FirewallApi.applingIptablesRules(ctx, false);
			/*if (isSuccess) {
				FirewallApi.saveRules(ctx);
			} else {
				switch (view.getId()) {
				case R.id.wifiImageView:
					if (allowOrNot) {
						app.selected_wifi = true;
					} else {
						app.selected_wifi = false;
					}
					break;
				case R.id.g3ImageView:
					if (allowOrNot) {
						app.selected_3g = true;
					} else {
						app.selected_3g = false;
					}
					break;
				default:
					break;
				}
				Toast.makeText(ctx, ctx.getResources().getString(R.string.firewall_not_surpport), Toast.LENGTH_SHORT).show();
			}
			notifyDataSetChanged();*/
		}
	}
	
	class DealingData extends Thread {
		int netType;
		boolean disableOrNot;
		Handler dealingHandler;
		public DealingData(int net, boolean disOrNot, Handler handler) {
			this.netType = net;
			this.disableOrNot = disOrNot;
			this.dealingHandler = handler;
		}
		@Override
		public void run() {
			boolean success = FirewallApi.applingIptablesRules(ctx, AppItemAdapter.this, false);
			Message msg = dealingHandler.obtainMessage();
			Boolean[] blean_obj = new Boolean[] {success, disableOrNot};
			msg.obj = blean_obj;
			msg.arg1 = netType;
			msg.what = 0;
			dealingHandler.sendMessage(msg);
		}		
	}

}
