/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.model;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kindroid.security.R;
import com.kindroid.security.service.SysClassNet;
import com.kindroid.security.util.KindroidSecurityApplication;

public abstract class Device {

	private static Device instance = null;

	private String[] mInterfaces = null;

	public synchronized static Device getDevice() {
		if (instance == null) {
			instance = new DiscoverableDevice();
		}
		return instance;
	}

	public abstract String[] getNames();

	public abstract String getCell();

	public abstract List<String> getCells();

	public abstract String getWiFi();

	public abstract String getBluetooth();

	public synchronized String[] getInterfaces() {
		if (mInterfaces == null) {
			List<String> tmp = new ArrayList<String>();
			if (getCell() != null) {
				tmp.add(getCell());
			}
			if (getWiFi() != null) {
				tmp.add(getWiFi());
			}
			if (getBluetooth() != null) {
				tmp.add(getBluetooth());
			}
			mInterfaces = tmp.toArray(new String[tmp.size()]);
		}
		return mInterfaces;
	}

	public String getPrettyName(String inter) {
		Resources r = KindroidSecurityApplication.resources();
		if (getCell() != null && getCell().equals(inter)) {
			return r.getString(R.string.interfaceTypeCell);
		} else if (getWiFi() != null && getWiFi().equals(inter)) {
			return r.getString(R.string.interfaceTypeWifi);
		} else if (getBluetooth() != null && getBluetooth().equals(inter)) {
			return r.getString(R.string.interfaceTypeBluetooth);
		} else {
			return inter;
		}
	}

	public int getIcon(String inter) {
		if (getCell() != null && getCell().equals(inter)) {
			return R.drawable.cell;
		} else if (getBluetooth() != null && getBluetooth().equals(inter)) {
			return R.drawable.bluetooth;
		} else {
			return R.drawable.wifi;
		}
	}

}

/**
 * Automatically discover the network interfaces. No real magic here, just try
 * different possible solutions.
 */
class DiscoverableDevice extends Device {

	private static final String[] CELL_INTERFACES = { //
	"rmnet0", "pdp0", "ppp0", "vsnet0" //
	};

	private static final String[] WIFI_INTERFACES = { //
	"eth0", "tiwlan0", "wlan0", "athwlan0", "eth1" //
	};

	private String mCell = null;

	private String mWiFi = null;

	private List<String> mCells = null;

	@Override
	public String[] getNames() {
		return null;
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		if (mCell == null) {
			for (String inter : CELL_INTERFACES) {
				if (SysClassNet.isUp(inter)) {
					// Log.i(getClass().getName(), "Cell interface: " + inter);
					mCell = inter;
					break;
				}
			}
		}
		return mCell;
	}

	@Override
	public String getWiFi() {
		if (mWiFi == null) {
			for (String inter : WIFI_INTERFACES) {
				if (SysClassNet.isUp(inter)) {
					// Log.i(getClass().getName(), "WiFi interface: " + inter);
					mWiFi = inter;
					break;
				}
			}
		}
		return mWiFi;
	}

	/*
	 * @Override public synchronized String[] getInterfaces() { // Do not cache
	 * the array. List<String> tmp = new ArrayList<String>(); if (getCell() !=
	 * null) { tmp.add(getCell()); } if (getWiFi() != null) {
	 * tmp.add(getWiFi()); } if (getBluetooth() != null) {
	 * tmp.add(getBluetooth()); } return tmp.toArray(new String[tmp.size()]); }
	 * 
	 * @Override public String getPrettyName(String inter) { Resources r =
	 * KindroidSecurityApplication.resources(); if (getCell() != null &&
	 * getCell().equals(inter)) { return
	 * r.getString(R.string.interfaceTypeCell); } else if (getWiFi() != null &&
	 * getWiFi().equals(inter)) { return
	 * r.getString(R.string.interfaceTypeWifi); } else if (getBluetooth() !=
	 * null && getBluetooth().equals(inter)) { return
	 * r.getString(R.string.interfaceTypeBluetooth); } // If nothing found, try
	 * to return a best guess... if
	 * (Arrays.asList(CELL_INTERFACES).contains(inter)) { return
	 * r.getString(R.string.interfaceTypeCell); } else if
	 * (Arrays.asList(WIFI_INTERFACES).contains(inter)) { return
	 * r.getString(R.string.interfaceTypeWifi); } // Really but really nothing
	 * found. return inter; }
	 * 
	 * @Override public int getIcon(String inter) { if (getCell() != null &&
	 * getCell().equals(inter)) { return R.drawable.cell; } else if
	 * (getBluetooth() != null && getBluetooth().equals(inter)) { return
	 * R.drawable.bluetooth; } // If nothing found, try to return a best
	 * guess... if (Arrays.asList(CELL_INTERFACES).contains(inter)) { return
	 * R.drawable.cell; } else if
	 * (Arrays.asList(WIFI_INTERFACES).contains(inter)) { return
	 * R.drawable.wifi; } // Really but really nothing found. return
	 * R.drawable.wifi; }
	 */
	@Override
	public synchronized String[] getInterfaces() {
		// Do not cache the array.
		List<String> tmp = new ArrayList<String>();
		if (getCells() != null) {
			tmp.addAll(mCells);
		}
		if (getWiFi() != null) {
			tmp.add(getWiFi());
		}
		if (getBluetooth() != null) {
			tmp.add(getBluetooth());
		}
		return tmp.toArray(new String[tmp.size()]);
	}

	@Override
	public String getPrettyName(String inter) {
		Resources r = KindroidSecurityApplication.resources();
		if (getCells() != null && getCells().contains(inter)) {
			return r.getString(R.string.interfaceTypeCell);
		} else if (getWiFi() != null && getWiFi().equals(inter)) {
			return r.getString(R.string.interfaceTypeWifi);
		} else if (getBluetooth() != null && getBluetooth().equals(inter)) {
			return r.getString(R.string.interfaceTypeBluetooth);
		}
		// If nothing found, try to return a best guess...
		if (Arrays.asList(CELL_INTERFACES).contains(inter)) {
			return r.getString(R.string.interfaceTypeCell);
		} else if (Arrays.asList(WIFI_INTERFACES).contains(inter)) {
			return r.getString(R.string.interfaceTypeWifi);
		}
		// Really but really nothing found.
		return inter;
	}

	@Override
	public int getIcon(String inter) {
		if (getCells() != null && getCells().contains(inter)) {
			return R.drawable.cell;
		} else if (getBluetooth() != null && getBluetooth().equals(inter)) {
			return R.drawable.bluetooth;
		}
		// If nothing found, try to return a best guess...
		if (Arrays.asList(CELL_INTERFACES).contains(inter)) {
			return R.drawable.cell;
		} else if (Arrays.asList(WIFI_INTERFACES).contains(inter)) {
			return R.drawable.wifi;
		}
		// Really but really nothing found.
		return R.drawable.wifi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		if(mCells == null){
			mCells = new ArrayList<String>();
			mCells.add("ppp0");
		}
		for (String inter : CELL_INTERFACES) {
			if (SysClassNet.isUp(inter) && !mCells.contains(inter)) {
				// Log.i(getClass().getName(), "Cell interface: " + inter);
				mCells.add(inter);
			}
//			else if(!SysClassNet.isUp(inter) && mCells.contains(inter)){
//				mCells.remove(inter);
//			}
		}
		
		return mCells;
	}

}

/**
 * Generic device implementation corresponding to the emulator.
 */
class GenericDevice extends Device {

	@Override
	public String[] getNames() {
		return new String[] { "generic" };
	}

	@Override
	public String getBluetooth() {
		return null;
	}

	@Override
	public String getCell() {
		return null;
	}

	@Override
	public String getWiFi() {
		return "eth0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Default device implementation.
 */
class DefaultDevice extends Device {

	private static final String INTERFACE_PATTERN = "^wifi\\.interface=(\\S+)$";

	private static final String BUILD_PROP = "/system/build.prop";

	private String mWifi = null;

	@Override
	public String[] getNames() {
		return new String[0];
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		return "rmnet0";
	}

	@Override
	public String getWiFi() {
		//
		if (mWifi == null) {
			Pattern pattern = Pattern.compile(INTERFACE_PATTERN);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(BUILD_PROP));
				String line;
				while ((line = br.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {
						mWifi = matcher.group(1);
						break;
					}
				}
			} catch (IOException e) {
				Log.e(getClass().getName(),
						"Unable to discover WiFi interface", e);
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// Silently ignore.
					}
				}
			}
			// Nothing found. Returns a "possible" default.
			if (mWifi == null) {
				mWifi = "eth0";
			}
		}
		return mWifi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Default device implementation corresponding to the HTC Dream and HTC Magic.
 */
class DreamDevice extends Device {

	@Override
	public String[] getNames() {
		// TODO Get the device name of the HTC Magic.
		return new String[] { "dream" };
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		return "rmnet0";
	}

	@Override
	public String getWiFi() {
		return "tiwlan0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Device implementation for the Samsung I7500. Also works with the I5700
 * (Spica).
 */
class SamsungI7500Device extends Device {

	@Override
	public String[] getNames() {
		return new String[] { "GT-I7500", "spica", "GT-I5700" };
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		return "pdp0";
	}

	@Override
	public String getWiFi() {
		return "eth0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Device implementation for the T-Mobile Pulse (Huawei U8220). Also works for
 * the Google Nexus One and HTC Desire.
 */
class PulseDevice extends Device {

	@Override
	public String[] getNames() {
		return new String[] { "U8220", "passion", "bravo" };
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		return "rmnet0";
	}

	@Override
	public String getWiFi() {
		return "eth0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Device implementation for the Motorola Droid.
 */
class DroidDevice extends Device {

	@Override
	public String[] getNames() {
		return new String[] { "sholes" };
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		return "ppp0";
	}

	@Override
	public String getWiFi() {
		return "tiwlan0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Device implementation for the LG Eve Android GW620R.
 */
class EveDevice extends Device {

	@Override
	public String[] getNames() {
		return new String[] { "EVE" };
	}

	@Override
	public String getBluetooth() {
		return "bnep0";
	}

	@Override
	public String getCell() {
		return "rmnet0";
	}

	@Override
	public String getWiFi() {
		return "wlan0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kindroid.security.model.Device#getCells()
	 */
	@Override
	public List<String> getCells() {
		// TODO Auto-generated method stub
		return null;
	}

}