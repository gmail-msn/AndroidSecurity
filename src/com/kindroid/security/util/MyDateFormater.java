/*
 * Copyright (C)  Kindroid.com, 2011-2012
 * File:
 * Author:
 * Date:
 * Description:
 */

package com.kindroid.security.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class MyDateFormater
{
  private DateFormat DF_DATE;
  private DateFormat DF_DATETIME;
  private DateFormat DF_DATE_LOCALE;
  private DateFormat DF_LOCALE;

  MyDateFormater()
  {
    SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    this.DF_DATETIME = localSimpleDateFormat1;
    SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    this.DF_DATE = localSimpleDateFormat2;
    DateFormat localDateFormat1 = DateFormat.getDateTimeInstance();
    this.DF_LOCALE = localDateFormat1;
    DateFormat localDateFormat2 = DateFormat.getDateInstance();
    this.DF_DATE_LOCALE = localDateFormat2;
  }

  public Date DF_DATETIMEparse(String paramString)
    throws ParseException
  {
    return this.DF_DATETIME.parse(paramString);
  }

  public Date DF_DATEparse(String paramString)
    throws ParseException
  {
    return this.DF_DATE.parse(paramString);
  }

  public String getDate(Calendar paramCalendar)
  {
    DateFormat localDateFormat = this.DF_DATE;
    Date localDate = paramCalendar.getTime();
    return localDateFormat.format(localDate);
  }

  public String getDateTime(Calendar paramCalendar)
  {
    DateFormat localDateFormat = this.DF_DATETIME;
    Date localDate = paramCalendar.getTime();
    return localDateFormat.format(localDate);
  }

  public String getLocaleDate(Calendar paramCalendar)
  {
    DateFormat localDateFormat = this.DF_DATE_LOCALE;
    Date localDate = paramCalendar.getTime();
    return localDateFormat.format(localDate);
  }

  public String getLocaleDateTime(Calendar paramCalendar)
  {
    DateFormat localDateFormat = this.DF_LOCALE;
    Date localDate = paramCalendar.getTime();
    return localDateFormat.format(localDate);
  }
}
