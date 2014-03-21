/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

public final class Constant {
	
	public static final String[] DOWNLOAD_STATUS={"Downloading","INSTALLED","UNINSTALLED","DOWNLOADED"};
	
	private static final String BASEURL254=Config.BASEURL254;
	private static final String BASEURL128=Config.BASEURL128;

	
	public static final String SUBCLASS_CATEGORY_URL=BASEURL128+"AppStore_war_exploded/categoryWS/getCat";
	
	public static final String SEARCH_URL=BASEURL128+"AppStore_war_exploded/searchWS/search";
	public static final String SEARCH_URL_NEW = BASEURL254 + "DefenderService_1.8/search/keyword/";
	public static final String FETCH_ICON_URL = BASEURL254 + "AppStore_war_exploded/iconWS/get/MHx8fHxndWVzdHx8MDAwMC0wMC0wMCAwMDowMDowMHx8T3NrQjIxRklSblBYclAxTGZkTWdHYzYzTTN3MEgyM1FPZisyMEdUNmt2VkdaQm1tZ0VZbk1UQXVReldaZzFmQUxxbk13YzdNbitpNg0KOGp4dnlzN2Z0aHVEY2Y1dDZwcFM5NUQ0ek1iSmQ1cTc3ZXlDb3BUamJEM3Q4Y1A2TDB2eW9HZGU5K1JDWTluT1cvNVFWcEQrSFlnUw0KQ2NyOUZDZk12blNpOExrWnE3MD0=/";
	public static final String GET_TOPICS_URL = BASEURL254 + "DefenderService_1.8/topic/get/";
	public static final String GET_TOPICAPP_URL = BASEURL254 + "DefenderService_1.8/topic/app/";
	public static final String AD_DRAWABLE_URL =  BASEURL254 + "DefenderService_1.8/ad/banner_new/";
	public static final String AD_CLICK_URL = BASEURL254 + "DefenderService_1.8/ad/click/";
	
	public static final String TOPIC_CLICK_URL = BASEURL254 + "DefenderService_1.8/topic/click/";
	public static final String CATEGORY_CLICK_URL = BASEURL254 + "DefenderService_1.8/category/click/";
	
	public static final String APP_IMAGE_URL = BASEURL254+"AppStore_war_exploded/profileWS/image";
	
	public static final String DOWNLOAD_APK_URL="http://203.156.192.128/api/DownloadApp";
	
	public static final String UPGRADE_CLIENT_URL = BASEURL128+"AppStore_war_exploded/client/version/";
	
	public static final String UPGRADE_URL = BASEURL254+"AppStore_war_exploded/client/lastVersion/defender";
	public static final String UPGRADE_EN_URL = Config.UPGRADE_EN_URL;
	public static final String UPGRADE_ZH_URL = Config.UPGRADE_ZH_URL;
	public static final String REPORT_URL = BASEURL254+"DefenderService_1.8/report/client/";
	public static final String HOT_KEYWORD_URL = BASEURL254+"AppStore_war_exploded/hotKeyWordsWS/get";
	
	public static final String RECOMMEND_URL = BASEURL254+"DefenderService_1.8/promotion/get/";
	public static final String REMOTE_BACKUP_URL = BASEURL254+"DefenderService_1.8/backup/uploadNew";
	
	public static final String REMOTE_RESTORE_URL = BASEURL254+"DefenderService_1.8/backup/download/";
	public static final String BACKUP_GETINFO_URL = BASEURL254+"DefenderService_1.8/backup/getInfo/";
	public static final String ACCOUNT_MANAGE_URL = BASEURL254+"DefenderService_1.8/account/op/";
	public static final String FEEDBACK_URL = BASEURL254+"DefenderService_1.8/feedback/bug/";
	public static final String ACCOUNT_CHANGE_PWD = BASEURL254+"DefenderService_1.8/account/passwd/";

	public static final String LOGIN_URL = BASEURL254+"DefenderService_1.8/account/login/";
	public static final String REGISTER_URL = BASEURL254+"DefenderService_1.8/account/register_new";
	public static final String FORGOTPWD_URL = BASEURL254+"DefenderService_1.8/forget/passwd/";
	public static final String virusUpdateCheckUrl = BASEURL254+"AppStore_war_exploded/client/lastVersion/virus";
	
	public static final String SENDINTRODUCTIONBYEMAIL_URL = BASEURL254+"DefenderService_1.8/email/post";
	
	public static final String CURRENT_CLIENT_VERSION = "2.0";
	 
	public static final String SHAREDPREFERENCES_REMOTESECURITY="remote_security";
	
	public static final String SHAREDPREFERENCES_SAFEMOBILENUMBER="safe_mobile_number";
	
	public static final String SHAREDPREFERENCES_AFTERUPDATESIMSENDMES="after_update_sim_send_mes";
	
	public static final String SHAREDPREFERENCES_AFTERUPDATESIMTOLOCKMOBILE="after_update_sim_to_lock_mobile";
	
	public static final String SHAREDPREFERENCES_REMOTESECURITYPASSWD="remote_security_passwd";
	
	public static final String SHAREDPREFERENCES_SIMUNIQUETAG="sim_unique_tag";
	
	public static final String SHAREDPREFERENCES_TOKEN="token";
	
	public static final String SHAREDPREFERENCES_USERNAME="user_name";
	public static final String SHAREDPREFERENCES_NETWORKRX="network_rx";
	public static final String SHAREDPREFERENCES_NETWORKTX="network_tx";
	
	public static final String SHAREDPREFERENCES_NETWORKWIFIRX="network_wifirx";
	public static final String SHAREDPREFERENCES_NETWORKWIFITX="network_wifitx";
	
	public static final String SHAREDPREFERENCES_FIRSTINSTALLREMOTESECURITY="first_install_remote_security";
	
	public static final String SHAREDPREFERENCES_ENABLETRAFFICMOITER="enable_traffic_moiter";
	public static final String SHAREDPREFERENCES_ENABLEAPPTRAFFICMOITER="enable_app_traffic_moiter";
	public static final String SHAREDPREFERENCES_APPFILEEXIT="app_file_exit";
	public static final String SHAREDPREFERENCES_WINX="win_x";
	public static final String SHAREDPREFERENCES_WINy="win_y";
	public static final String SHAREDPREFERENCES_APPFIRST_INSTALL="app_item_first_install";
	
	/** block rule 
	 * 0 night model ,intercept data accoding the selected rules in the specific time period
	 * 1 default model, intercept blacklist and keywords.
	 * 2 contact modle, accept only contact list and white list.
	 * 3 whitelist mode
	 * 4 all accept model,accept all phone and sms.
	 * 5 all intercept model, intercept all phone and sms. 
	 * 6 all intercept phone model,intercept all phone and blacklist that has been set sms and keywod. 
	 * 
	 * 
	 * */
	public static final String SHAREDPREFERENCES_BLOCKINGRULES="blocking_rule";	
	public static final String SHAREDPREFERENCES_NIGHTBLOCKINGRULES="night_blocking_rule";
	/**
	 * 拦截处理模式,从1到5,分别表示不同的处理模式
	 */
	public static final String INTERCEPT_TREAT_MODE = "intercept_treat_mode";
	/**
	 * 0表示发生拦截时不通知拦截信息;1表示发生时通知拦截信息
	 */
	public static final String INTERCEPT_NOTIFY_INFO = "intercept_notify_info";
	public static final String NODISTURB_START_TIME = "nodisturb_start_time";
	public static final String NODISTURB_END_TIME = "nodisturb_end_time";
	public static final String NODISTURB_DAY_TIME = "nodisturb_day_time";	
	
	
	
	public static final String TAGDEL="ehoo_del";
	public static final String TAGGPS="ehoo_gps";
	public static final String APPTRAFFICDIR="/proc/uid_stat";
	
	public static final String BROACTUPDATEINTERCEPT="com.update.intercept.list";
	
	public static final String BROACTUPDATEINTERCEPTHISTORY="com.update.intercept.history";
	
	public static final String BROACTUPDATEINFINISHBLOCK="com.update.intercept.finish";
	
	
	
	
}
