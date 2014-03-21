package com.kindroid.security.adapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.kindroid.security.R;
import com.kindroid.security.model.AppInfoForManage;
import com.kindroid.security.util.UnitsConversion;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CacheClearListAdapter extends BaseAdapter {
	public static final int SORT_BY_SIZE = 0;
	public static final int SORT_BY_NAME = 1;
	private Activity mActivity;
	private List<AppInfoForManage> items;
	public boolean mOnlyStateChange = false;

	public CacheClearListAdapter(Activity activity) {
		this.mActivity = activity;
		this.items = new ArrayList<AppInfoForManage>();
	}

	public void setSelectedAll(boolean checked) {
		for (AppInfoForManage aifm : items) {
			aifm.setSelected(checked);
		}
	}

	public void setSelectedAllForInstall(boolean checked) {
		for (AppInfoForManage aifm : items) {
			// if(!aifm.isInstalled())
			aifm.setSelected(checked);
		}

	}
	public List<AppInfoForManage> getItems(){
		return this.items;
	}
	public void setItems(List<AppInfoForManage> items){
		this.items = items;
	}

	public void sortItems(int flag, boolean desc) {
		final boolean tDesc = desc;
		switch (flag) {
		case SORT_BY_SIZE:
			Collections.sort(items, new Comparator<AppInfoForManage>() {
				@Override
				public int compare(AppInfoForManage object1, AppInfoForManage object2) {
					if (tDesc) {
						return object1.getCacheSize() > object2.getCacheSize() ? -1 : 1;
					} else {
						return object2.getCacheSize() > object1.getCacheSize() ? -1 : 1;
					}
				}

			});
			break;
		case SORT_BY_NAME:
			Collections.sort(items, new Comparator<AppInfoForManage>() {
				@Override
				public int compare(AppInfoForManage object1,AppInfoForManage object2) {
					Locale locale = mActivity.getResources().getConfiguration().locale;
					Collator myCollator = Collator.getInstance(locale);
					String label1 = object1.getLabel().toString().trim();
					String label2 = object2.getLabel().toString().trim();

					if (tDesc) {
						if (myCollator.compare(label2, label1) < 0) {
							return -1;
						} else if (myCollator.compare(label2, label1) > 0) {
							return 1;
						} else {
							return 0;
						}
					} else {
						if (myCollator.compare(label1, label2) < 0) {
							return -1;
						} else if (myCollator.compare(label1, label2) > 0) {
							return 1;
						} else {
							return 0;
						}
					}
				}

			});
			break;
		}
	}

	public void addItem(AppInfoForManage aifm) {
		this.items.add(aifm);
	}

	public void deleteItem(int index) {
		this.items.remove(index);
	}

	public void delteItem(AppInfoForManage aifm) {
		this.items.remove(aifm);
	}

	public void clearItems() {
		this.items.clear();
	}

	@Override
	public int getCount() {
		return this.items.size();
	}

	@Override
	public Object getItem(int position) {
		return this.items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = this.mActivity.getLayoutInflater().inflate(R.layout.cache_clear_item, null);
		}
		final AppInfoForManage aifm = items.get(position);
		ImageView iconView = (ImageView) convertView.findViewById(R.id.softmanage_app_icon);
		iconView.setImageDrawable(aifm.getIcon());
		TextView nameView = (TextView) convertView.findViewById(R.id.softmanage_app_label);
		nameView.setText(aifm.getLabel());
		if (aifm.getFlag() == 0) {
			TextView versionView = (TextView) convertView.findViewById(R.id.softmanage_app_version);
			versionView.setVisibility(View.GONE);
		} else {
			TextView versionView = (TextView) convertView.findViewById(R.id.softmanage_app_version);
			if (aifm.getVersion() != null) {
				versionView.setText(aifm.getVersion());
			} else {
				versionView.setText(R.string.softmanage_version_unknown);
			}
			versionView.setVisibility(View.VISIBLE);
		}
		TextView sizeView = (TextView) convertView.findViewById(R.id.softmanage_app_size);
		sizeView.setText(Html.fromHtml(mActivity.getString(R.string.cache_management_cache_text)
				+ UnitsConversion.formatCacheSize(1, aifm.getCacheSize())));

		if (aifm.getFlag() != 0) {
			if (aifm.isInstalled()) {
				TextView isInstalledView = (TextView) convertView.findViewById(R.id.softmanage_already_installed_tip);
				isInstalledView.setVisibility(View.VISIBLE);
			}
		} 

		return convertView;
	}

}
