/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import android.content.Context;

import java.text.DecimalFormat;
import java.util.Hashtable;

import com.kindroid.security.R;

public class UnitsConversion extends DecimalFormat {
	private static final long perminite = 60000L;
	private static final long perhour = 3600000L;
	private static final long perday = 86400000L;
	private static final long permonth = 2592000000L;
    private static final long serialVersionUID = 3168068393840262910L;
    private static final String htmlTag1 = "<font color=\"#64BD45\"><big>";
    private static final String htmlTag2 = "</big></font>";    
    private static Hashtable<String, String> validUnits = new Hashtable<String, String>();   
    private static int GB_MAX_SIZE = 1;   
    private static int MB_MAX_SIZE = GB_MAX_SIZE * 1024;    
    private static int KB_MAX_SIZE = MB_MAX_SIZE * 1024;    
    private static int BYTES_MAX_SIZE = KB_MAX_SIZE * 1024;   
    private Double numPart;    
    private String originalUnit;    
    private String unit;    
    private String result;

    static {
        validUnits.put("字节", "Bytes");
        validUnits.put("bytes", "Bytes");
        validUnits.put("byte", "Bytes");
        validUnits.put("kb", "KB");
        validUnits.put("k", "KB");
        validUnits.put("兆", "MB");
        validUnits.put("mb", "MB");
        validUnits.put("m", "MB");
        validUnits.put("gb", "GB");
        validUnits.put("g", "GB");
    }
    public static String formatLastScanTime(Context context, long last_scan_time) {
		String retStr = null;
		if (last_scan_time == 0) {
			retStr = "0 " + context.getString(R.string.virus_scan_days_ago_text);
		} else {
			long period = System.currentTimeMillis() - last_scan_time;
			if (period > permonth) {
				retStr = period / permonth
						+ context.getString(R.string.virus_scan_months_ago_text);
			} else if (period > perday) {
				retStr = period / perday
						+ context.getString(R.string.virus_scan_days_ago_text);
			} else if (period > perhour) {
				retStr = period / perhour
						+ context.getString(R.string.virus_scan_hours_ago_text);
			} else if (period > perminite) {
				retStr = period / perminite
						+ context.getString(R.string.virus_scan_minites_ago_text);
			} else {
				retStr = period / 1000
						+ context.getString(R.string.virus_scan_seconds_ago_text);
			}
		}
		return retStr;
	} 

    public UnitsConversion() {
        super("########.##");
        numPart = null;
        result = null;
        unit = null;
        originalUnit = null;
    }
    
    public static String formatCacheSize(int type, long length) {
		String result = "";
		if (type == 1) {
			DecimalFormat df = new DecimalFormat("###.");
			df.setMinimumFractionDigits(2);
			if (length >= 1073741824) {
				result = df.format((double) length / 1073741824);
				result = htmlTag1 + result + htmlTag2 +"G";
			} else if (length >= 1048576) {
				result = df.format((double) length / 1048576);
				result = htmlTag1 + result + htmlTag2 + "M";
			} else if (length >= 1024) {
				result = df.format((double) length / 1024);
				result = htmlTag1 + result + htmlTag2 + "K";
			} else {
				result = htmlTag1 + length + htmlTag2 + "B";
			}

		}
		return result;
	}
    
    public String defaultConversionForHtml(long input){
    	analyzeString(String.valueOf(input));
        if (result != null) {
            return result;
        }
        
        if (unit.equals("Bytes")) {
            int numPart2Int = numPart.intValue();
            
            if ((BYTES_MAX_SIZE - numPart2Int) < (1024 * 1024) / 2) {
                return "1 GB";
            }
            // (0,1KB)
            if (numPart2Int < 1024) {
                return htmlTag1 + numPart2Int + htmlTag2 + " Bytes";
            }
            // [1KB,1023KB]
            if (numPart2Int >= 1024 && numPart2Int <= (1024 - 1) * 1024) {
                return htmlTag1 + format(numPart / (1024 * 1024)) + htmlTag2 + " MB";
            }
            // (1023KB,1GB)
            if (numPart2Int > (1024 - 1) * 1024 && numPart2Int < BYTES_MAX_SIZE) {
                return htmlTag1 + format(numPart / (1024 * 1024)) + htmlTag2 + " MB";
            } else
                result = "";
            return result;
        }

        if (unit.equals("KB")) {
            return "还没实现....";
        }

        if (unit.equals("MB")) {
            return "还没实现....";
        }

        if (unit.equals("GB")) {
            return "还没实现....";
        }
        result = "";
        return result;
    }
    
    public String defaultConversion(int input){
    	return defaultConversion(String.valueOf(input));
    }
    
    public String defaultConversion(long input){
    	return defaultConversion(String.valueOf(input));
    }
    public String[] conversion(String input) {
        analyzeString(input);
        String[] ret = new String[2];
        if (unit.equals("Bytes")) {
            int numPart2Int = numPart.intValue();
            if ((BYTES_MAX_SIZE - numPart2Int) < (1024 * 1024) / 2) {
            	ret[0] = "1";
            	ret[1] = " GB";
                return ret;
            }            
            // (0,1KB)
            if (numPart2Int < 1024) {
            	ret[0] = String.valueOf(numPart2Int);
            	ret[1] = " Bytes";
                return ret;
            }
            // [1KB,1023KB]
            if (numPart2Int >= 1024 && numPart2Int <= (1024 - 1) * 1024) {
            	ret[0] = format(numPart / 1024);
            	ret[1] = " KB";
                return ret;
            }
            // (1023KB,1GB)
            if (numPart2Int > (1024 - 1) * 1024 && numPart2Int < BYTES_MAX_SIZE) {
            	ret[0] = format(numPart / (1024 * 1024));
            	ret[1] = " MB";
                return ret;
            } else{
            	
            }
            
        }

        
        return ret;
    }

    public String defaultConversion(String input) {
        analyzeString(input);
        if (result != null) {
            return result;
        }
        if (unit.equals("Bytes")) {
            int numPart2Int = numPart.intValue();
            if ((BYTES_MAX_SIZE - numPart2Int) < (1024 * 1024) / 2) {
                return "1 GB";
            }
            // (0,1KB)
            if (numPart2Int < 1024) {
                return numPart2Int + " Bytes";
            }
            // [1KB,1023KB]
            if (numPart2Int >= 1024 && numPart2Int <= (1024 - 1) * 1024) {
                return format(numPart / (1024 * 1024)) + " MB";
            }
            // (1023KB,1GB)
            if (numPart2Int > (1024 - 1) * 1024 && numPart2Int < BYTES_MAX_SIZE) {
                return format(numPart / (1024 * 1024)) + " MB";
            } else
                result = "";
            return result;
        }

        if (unit.equals("KB")) {
            return "还没实现....";
        }

        if (unit.equals("MB")) {
            return "还没实现....";
        }

        if (unit.equals("GB")) {
            return "还没实现....";
        }
        result = "";
        return result;
    }

    public void analyzeString(String input) {
        if (input == null || input.trim().length() < 2) {
           
            result = "";
            return;
        }
        input = input.replaceAll(" ", "");
        int firstIndexOfUnit;
        String strOfNum;
        for (int i = input.length() - 1; i >= 0; i--) {
            if (Character.isDigit(input.charAt(i))) {
                firstIndexOfUnit = i + 1;
                originalUnit = input.substring(firstIndexOfUnit,
                        input.length()).toLowerCase();
                if (!isValidUnit(originalUnit)) {
                    result = "";
                    return;
                }
                
                if(originalUnit.length() == 0)
                	originalUnit = "bytes";
                unit = validUnits.get(originalUnit);
                
                strOfNum = input.substring(0, firstIndexOfUnit);
                numPart = Double.parseDouble(strOfNum);
                if (!isValidNum(numPart, unit)) {
                    result = "";
                    return;
                }
                if (numPart == 0) {
                    result = "0 Bytes";
                    return;
                }
                break;
            }
        }
        if (unit == null || numPart == null) {
            result = "";
            return;
        }

    }

    public boolean isValidNum(Double num, String unit) {
        if (num == null || num < 0 || num > BYTES_MAX_SIZE) {
            return false;
        }
        if (unit.equals("KB") && num > KB_MAX_SIZE) {
            return false;
        }
        if (unit.equals("MB") && num > MB_MAX_SIZE) {
            return false;
        }
        if (unit.equals("GB") && num > GB_MAX_SIZE) {
            return false;
        }
        return true;
    }

    public boolean isValidUnit(String originalUnit) {
        if (originalUnit == null || originalUnit.trim().length() < 1) {
            originalUnit = "bytes";
            return true;
        }
        for (String validUnit : validUnits.keySet()) {
            if (validUnit.equalsIgnoreCase(originalUnit)) {
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) {
       
        for (int i = 1020 * 1024; i <= 1024 * 1111; i += 9) {
            String input = i + " ";
            System.out.println(input + " ---> "
                    + new UnitsConversion().defaultConversion(input));
        }
    }

}

