package com.playup.android.util;

import java.text.DateFormatSymbols;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.text.format.DateFormat;



import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;

@SuppressLint({ "NewApi", "NewApi" })
public class DateUtil {

	Date date;
	Calendar calendar;
	Calendar currentTime;
	Calendar cal;
	SimpleDateFormat currentFormat;
	SimpleDateFormat dateFormatYMDHMS; 
	Date dateObj;
	Resources resource = PlayUpActivity.context.getResources();
	
	
	/**
	 * To parse the string to form the equivalent Date
	 * @param dateString
	 * @param isUTC, if true set the time zone to UTC time zone, else by default it takes defult
	 * @return
	 */
	public Date parseDate(String dateString, boolean isUTC)
	{
		Date parsedDate = null;
		SimpleDateFormat dateFormatter = null; 
		//Log.d("123  ", "dateString------>>>>>"+dateString);
	
		if(dateString!=null){
			try {
				dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				
				if(isUTC)
					dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				
				parsedDate = dateFormatter.parse(dateString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//Logs.show(e);
				dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				
				if(isUTC)
					dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				
				try {
					parsedDate = dateFormatter.parse(dateString);
				} catch (ParseException e1) {
					//Logs.show(e);
				}
			}
			
			
		}
		
		//Log.e("123  ", "parsedDate------>>>>>"+parsedDate);
		return parsedDate;
	}
	
	/**
	 * converting the time into our local time and showing time in 1 day ago
	 * format
	 */
	
	
	public String gmt_to_local_timezone(String local) {

		try {
		
			if ( local == null ) {
				return "";
			}
			date = new Date();
			
			int getTimezoneoffset = date.getTimezoneOffset() * 60 * 1000;
			
			
			long currenttime = date.getTime();
			
			long offset = currenttime + getTimezoneoffset;
			
			dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		//	dateObj = dateFormatYMDHMS.parse(local);
			dateObj = parseDate(local,false);
		
			long msgPostTime = dateObj.getTime();
			
			long actualTime = offset - msgPostTime;
			long diffMinutes = actualTime / (60 * 1000);
			
			
			currentTime = Calendar.getInstance();
			String postedTime = gmtToLocal(local);
			//dateObj = dateFormatYMDHMS.parse(postedTime);
			dateObj = parseDate(postedTime,false);
			
			if (diffMinutes <= 59) {
				
				StringBuilder postTime = new StringBuilder(Integer
						.toString((int) diffMinutes));
			
				if (diffMinutes < 1) {
			
					postTime = new StringBuilder(" "+resource.getString(R.string.justNow));
					return postTime.toString();
				} else if (diffMinutes < 2) {
					postTime.append(" "+resource.getString(R.string.min));
				} else {
					postTime.append(" "+resource.getString(R.string.mins));
				}

				return postTime.toString();

			} else if (diffMinutes >= 60 && diffMinutes <= 1439) {

				int hour = (int) diffMinutes / 60;

				StringBuilder postTime = new StringBuilder(Integer
						.toString(hour));

				if (hour < 2) {
					postTime.append(" "+resource.getString(R.string.hour));
				} else {
					postTime.append(" "+resource.getString(R.string.hours));
				}

				return postTime.toString();

			} else if (diffMinutes >= 1440 && diffMinutes <= 2880) {
				return resource.getString(R.string.yesterday);
			} else if (diffMinutes < (7 * 24 * 60)) {

				int days = (int) diffMinutes / 60 / 24;
				return days + " "+resource.getString(R.string.days);

				// SimpleDateFormat dateFormat = new
				// SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				// return dateFormat.format(dateObj);
			} else if (diffMinutes < (2 * 7 * 24 * 60)) {
				return resource.getString(R.string.lastWeek);
			}
			else
			{
				int months=(currentTime.get(Calendar.YEAR)-(dateObj.getYear()+1900))*12
						  +(currentTime.get(Calendar.MONTH)-dateObj.getMonth());
				if((currentTime.get(Calendar.DATE)<dateObj.getDate())||
				  ((currentTime.get(Calendar.HOUR_OF_DAY)<dateObj.getHours())&&
				   (currentTime.get(Calendar.DATE)==dateObj.getDate())))
					months--;
				
				if(months<1)
				{
					int weeks=( int ) diffMinutes/ 60 / 24 / 7; 
					return weeks + " "+resource.getString(R.string.weeks);
				}
				else if(months<2)
				{
					return resource.getString(R.string.lastMonth);
				}
				else if(months<12)
				{
					return months+" "+resource.getString(R.string.months);
				}
				else if(months<24)
				{
					return resource.getString(R.string.lastYear);
				}
				else
				{
					return months/12+" "+resource.getString(R.string.years);
				}
				
			}
		} catch (Exception e) {
			Logs.show(e);
		} finally {
			date = null;
		}
		return "";

	}
	
	public String gmt_to_local_timezone_tiles(String local) {
		boolean inCatch = false;
		try {
			
			date = new Date();
			
			int getTimezoneoffset = date.getTimezoneOffset() * 60 * 1000;
			
			
			long currenttime = date.getTime();
			
			long offset = currenttime + getTimezoneoffset;
			
			dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			//dateObj = dateFormatYMDHMS.parse(local);
			dateObj = parseDate(local,false);
		
			long msgPostTime = dateObj.getTime();
			
			long actualTime = offset - msgPostTime;
			long diffMinutes = actualTime / (60 * 1000);

			
			currentTime = Calendar.getInstance();
			String postedTime = gmtToLocalTiles(local);
			//dateObj = dateFormatYMDHMS.parse(postedTime);
			dateObj = parseDate(postedTime,false);

			if (diffMinutes <= 59) {
				
				StringBuilder postTime = new StringBuilder(Integer
						.toString((int) diffMinutes));
			
				if (diffMinutes < 1) {
			
					postTime = new StringBuilder(" "+resource.getString(R.string.justNow));
					return postTime.toString();
				} else if (diffMinutes < 2) {
					postTime.append(" "+resource.getString(R.string.min));
				} else {
					postTime.append(" "+resource.getString(R.string.mins));
				}

				return postTime.toString();

			} else if (diffMinutes >= 60 && diffMinutes <= 1439) {

				int hour = (int) diffMinutes / 60;

				StringBuilder postTime = new StringBuilder(Integer
						.toString(hour));

				if (hour < 2) {
					postTime.append(" "+resource.getString(R.string.hour));
				} else {
					postTime.append(" "+resource.getString(R.string.hours));
				}

				return postTime.toString();

			} else if (diffMinutes >= 1440 && diffMinutes <= 2880) {
				return resource.getString(R.string.yesterday);
			} else if (diffMinutes < (7 * 24 * 60)) {

				int days = (int) diffMinutes / 60 / 24;
				return days + " "+resource.getString(R.string.days);

				// SimpleDateFormat dateFormat = new
				// SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				// return dateFormat.format(dateObj);
			} else if (diffMinutes < (2 * 7 * 24 * 60)) {
				return resource.getString(R.string.lastWeek);
			}
			else
			{
				int months=(currentTime.get(Calendar.YEAR)-(dateObj.getYear()+1900))*12
						  +(currentTime.get(Calendar.MONTH)-dateObj.getMonth());
				if((currentTime.get(Calendar.DATE)<dateObj.getDate())||
				  ((currentTime.get(Calendar.HOUR_OF_DAY)<dateObj.getHours())&&
				   (currentTime.get(Calendar.DATE)==dateObj.getDate())))
					months--;
				
				if(months<1)
				{
					int weeks=( int ) diffMinutes/ 60 / 24 / 7; 
					return weeks + " "+resource.getString(R.string.weeks);
				}
				else if(months<2)
				{
					return resource.getString(R.string.lastMonth);
				}
				else if(months<12)
				{
					return months+" "+resource.getString(R.string.months);
				}
				else if(months<24)
				{
					return resource.getString(R.string.lastYear);
				}
				else
				{
					return months/12+" "+resource.getString(R.string.years);
				}
				
			}
		} catch (Exception e) {
			inCatch = true;
			Logs.show(e);
			
		} finally {
			date = null;
		}
	
		if(!inCatch)
		return "";
		else
			return(gmt_to_local_timezone(local));

	}

	// showing the time with 00 insttead of 0.
	public String manipulateTimeForScore(String mins, String secs) {

		int min = -1;
		int sec = -1;
		if (mins != null && mins.trim().length() > 0) {
			try {
				min = Integer.parseInt(mins);
			} catch (Exception e) {
				  
			}
		}
		if (secs != null && secs.trim().length() > 0) {
			try {
				sec = Integer.parseInt(secs);
			} catch (Exception e) {
				  
			}
		}
		String result = "";
		if (min != -1 && sec != -1) {
			if (min < 10) {
				result = "0" + min + ":";
			} else {
				result = min + ":";
			}

			if (sec < 10) {
				result = result + "0" + sec;
			} else {
				result = result + sec;
			}
		}
		return result;
	}

	/**
	 * Getting current month
	 */
	/*public String getCurrentMonth() {

		cal = Calendar.getInstance();

		switch (cal.get(Calendar.MONTH)) {

		case Calendar.JANUARY:
			return resource.getString(R.string.jan1);
		case Calendar.FEBRUARY:
			return resource.getString(R.string.feb1);
		case Calendar.MARCH:
			return resource.getString(R.string.mar1);
		case Calendar.APRIL:
			return resource.getString(R.string.apr1);
		case Calendar.MAY:
			return resource.getString(R.string.may1);
		case Calendar.JUNE:
			return resource.getString(R.string.jun1);
		case Calendar.JULY:
			return resource.getString(R.string.jul1);
		case Calendar.AUGUST:
			return resource.getString(R.string.aug1);
		case Calendar.SEPTEMBER:
			return resource.getString(R.string.sep1);
		case Calendar.OCTOBER:
			return resource.getString(R.string.oct1);
		case Calendar.NOVEMBER:
			return resource.getString(R.string.nov1);
		case Calendar.DECEMBER:
			return resource.getString(R.string.dec1);

		}
		return "";
	}
	
	
	/**
	 * Getting current month
	 */
	public String getCurrentMonth() {

		cal = Calendar.getInstance(Locale.getDefault());
		try{
			return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase();
			
		} catch (Exception e) {

			 
			SimpleDateFormat formatter = new SimpleDateFormat("MMM",Locale.getDefault());
			return formatter.format(cal.getTime()).toUpperCase(Locale.getDefault());

		}catch (Error e) {

			SimpleDateFormat formatter = new SimpleDateFormat("MMM",Locale.getDefault());
			return formatter.format(cal.getTime()).toUpperCase(Locale.getDefault());
		}
		/*switch (cal.get(Calendar.MONTH)) {
		
		case Calendar.JANUARY:
			return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
		case Calendar.FEBRUARY:
			return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
		case Calendar.MARCH:
			return resource.getString(R.string.mar1);
		case Calendar.APRIL:
			return resource.getString(R.string.apr1);
		case Calendar.MAY:
			return resource.getString(R.string.may1);
		case Calendar.JUNE:
			return resource.getString(R.string.jun1);
		case Calendar.JULY:
			return resource.getString(R.string.jul1);
		case Calendar.AUGUST:
			return resource.getString(R.string.aug1);
		case Calendar.SEPTEMBER:
			return resource.getString(R.string.sep1);
		case Calendar.OCTOBER:
			return resource.getString(R.string.oct1);
		case Calendar.NOVEMBER:
			return resource.getString(R.string.nov1);
		case Calendar.DECEMBER:
			return resource.getString(R.string.dec1);

		}*/
	
	}

	/**
	 * Current day according to the month.
	 */
	public int getCurrentDate() {

		 cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 
	 * @param time
	 * @return
	 */
	public String Match_Time(String dStartTime, String dEndTime) {
		try {

			// get the current time
			date = new Date();

			int getTimezoneoffset = date.getTimezoneOffset() * 60 * 1000; // In
																			// milliseconds
			long currentTime = date.getTime() + getTimezoneoffset;

			// get start time from string
			dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//date = dateFormatYMDHMS.parse(dStartTime);
			date = parseDate(dStartTime,false);
			long startTime = date.getTime();

			if (startTime > currentTime) {
				// match is scheduled in future
				//converting match time from gmt to local timezone
				
				//date=dateFormatYMDHMS.parse(gmtToLocal(dStartTime));
				date=parseDate(gmtToLocal(dStartTime),false);
				return match_time_format(date);
			}else if(startTime<currentTime&&dEndTime==null){ // Then Wrong Device time zet
				
				//date=dateFormatYMDHMS.parse(gmtToLocal(dStartTime));
				date=parseDate(gmtToLocal(dStartTime),false);
				StringBuilder str = new StringBuilder();
				boolean is24hr=DateFormat.is24HourFormat(PlayUpActivity.context);
				int dateValue = date.getDate();

				String month = getMonth(date.getMonth());
				String hours = Integer.toString(date.getHours());
				String min = Integer.toString(date.getMinutes());		
				
				if(date.getMinutes()<10)
					min = "0" + min;
				
				if(is24hr && date.getHours()<10) {
					hours = "0" + hours;
				}
				
				str.append(dateValue).append(" ").append(month).append(" ").
				append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
				
				long timeDiff = (currentTime - startTime )/(60*1000);
				if( timeDiff < 30)
					return resource.getString( R.string.starting );
				else	
				   return str.toString();
				
				//return "Wrong device time!";
			}

			if (dEndTime == null) {
				// means the match is still live
				return getTimeDifference(currentTime - startTime);
			}
		} catch (Exception e) {
			  Logs.show(e);
		}
		return null;

	}
	
	
	
	public String Match_TimeRoomFragment ( String dStartTime, String dEndTime, String dScheduleStartTime ) {
		try {

			if(dStartTime!=null&&dEndTime!=null)
				return "Completed";
			
			// get the current time
			date = new Date();

			int getTimezoneoffset = date.getTimezoneOffset() * 60 * 1000; // In
																			// milliseconds
			long currentTime = date.getTime() + getTimezoneoffset;

			// get start time from string
			 dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			 
			 if(dStartTime != null){
				 //date = dateFormatYMDHMS.parse(dStartTime);
				 date = parseDate(dStartTime,false);
			 }
			 else{
				// date = dateFormatYMDHMS.parse(dScheduleStartTime);
				 date = parseDate(dScheduleStartTime,false);

			 }
			 
			 if(dStartTime!=null&&dEndTime==null){//Live Match
				 return null;
				 
			 }
			 
			long startTime = date.getTime();
			if (startTime > currentTime) {
				// match is scheduled in future
				//converting match time from gmt to local timezone
				
				//date=dateFormatYMDHMS.parse(gmtToLocal(dScheduleStartTime));
				date=parseDate(gmtToLocal(dScheduleStartTime),false);
				return match_time_format(date);
			}
			else if(startTime<currentTime && dEndTime==null) { // Then Wrong Device time zet
				if( dStartTime != null )
					{
						//date=dateFormatYMDHMS.parse(gmtToLocal(dStartTime));
						date=parseDate(gmtToLocal(dStartTime),false);
					}
				else
					{
						//date=dateFormatYMDHMS.parse(gmtToLocal(dScheduleStartTime));
						date=parseDate(gmtToLocal(dScheduleStartTime),false);
					}
				StringBuilder str = new StringBuilder();
				boolean is24hr=DateFormat.is24HourFormat(PlayUpActivity.context);
				int dateValue = date.getDate();

				String month = getMonth(date.getMonth());
				String hours = Integer.toString(date.getHours());
				String min = Integer.toString(date.getMinutes());		
				
				if(date.getMinutes()<10)
					min = "0" + min;
				
				if(is24hr && date.getHours()<10) {
					hours = "0" + hours;
				}
				try {
					SimpleDateFormat formater = new SimpleDateFormat(resource.getString(R.string.dateFormat));
					
					str.append(formater.format(date)).append("\n").append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}
				
//				str.append(dateValue).append(" ").append(month).append(" ").
//				append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));

				
				long timeDiff = (currentTime - startTime )/(60*1000);
				if( timeDiff < 30)
					return resource.getString( R.string.starting );
				else	
				   return str.toString();
				
			}

			if (dEndTime == null) {
				// means the match is still live
				return "";
			} else {

				// get end time from string
				//date = dateFormatYMDHMS.parse(dEndTime);
				date = parseDate(dEndTime,false);
				long endTime = date.getTime();

				if (startTime <= currentTime && currentTime <= endTime) {
					// live match
					// get the time difference
					return "";
				} else if (currentTime > endTime) {
					// match has already completed
					return "Completed";
				}
			}

		} catch (Exception e) {
			  Logs.show(e);
		}
		return null;

	}
	
	
	
	public boolean isNotLiveUpdatesMatch(String dStartTime, String dEndTime,String dScheduleStartTime) {
		try {

			if(dStartTime!=null&&dEndTime!=null)
				return false;
			
			// get the current time
			date = new Date();

			int getTimezoneoffset = date.getTimezoneOffset() * 60 * 1000; // In
																			// milliseconds
			long currentTime = date.getTime() + getTimezoneoffset;

			// get start time from string
			 dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			 
			 if(dStartTime != null){
				 //date = dateFormatYMDHMS.parse(dStartTime);
				 date = parseDate(dStartTime,false);
			 }
			 else{
				 //date = dateFormatYMDHMS.parse(dScheduleStartTime);
				 date =parseDate(dScheduleStartTime,false);

			 }
			 
			 if(dStartTime!=null&&dEndTime==null){//Live Match
				 return true;
				 
			 }
			 
			long startTime = date.getTime();
			if (startTime > currentTime) {
				return false;
			}
			else if(startTime<currentTime && dEndTime==null) { // Then Wrong Device time zet
				return true;
			}

			if (dEndTime == null) {
				// means the match is still live
				return true;
			} else {

				// get end time from string
				//date = dateFormatYMDHMS.parse(dEndTime);
				date = parseDate(dEndTime,false);
				long endTime = date.getTime();

				if (startTime <= currentTime && currentTime <= endTime) {
					// live match
					// get the time difference
					return false;
				} else if (currentTime > endTime) {
					// match has already completed
					return false;
				}
			}

		} catch (Exception e) {
			  Logs.show(e);
		}
		return false;

	}
	
	
	

	private String getTimeDifference(long actualTime) {

		// get the time difference
		long seconds = actualTime % (1000 * 60);
		long minutes = actualTime / (1000 * 60);

		String result = null;

		if (minutes < 10) {
			result = "0" + minutes + ":";
		} else {
			result = minutes + ":";
		}

		if (seconds < 10) {
			result += "0" + seconds;
		} else {
			String sec = seconds + "";
			result += sec.substring(0, 2);
		}

		return result;
	}

	public boolean isMatchLive(String startTime, String endTime) {
		try {

			Date dateObjEndTime;
			Date date = new Date();

			int getTimezoneoffset = date.getTimezoneOffset() * 60 * 1000; // In
																			// milliseconds
			long currentTime = date.getTime() + getTimezoneoffset;

			// find start time
			dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//date = dateFormatYMDHMS.parse(startTime);
			date = parseDate(startTime,false);
			long starttime = date.getTime();

			// find endtime

			if (endTime == null) {
				if (starttime <= currentTime) {
					// Live Match
					return true;
				}
			} else {
				//date = dateFormatYMDHMS.parse(endTime);
				date = parseDate(endTime,false);
				long endtime = date.getTime();

				if (starttime <= currentTime && currentTime <= endtime) {
					// Live Match
					return true;
				}
			}

		} catch (Exception e) {
			  Logs.show(e);
		}

		return false;
	}

	public String match_time_format(Date matchDate) {
		
		Date today=new Date();
		StringBuilder str = new StringBuilder();
		boolean is24hr=DateFormat.is24HourFormat(PlayUpActivity.context);
		int date = matchDate.getDate();

		try {
			String month = getMonth(matchDate.getMonth());
			String hours = Integer.toString(matchDate.getHours());
			String min = Integer.toString(matchDate.getMinutes());		
			if(matchDate.getMinutes()<10)
				min = "0" + min;
			
			if(is24hr && matchDate.getHours()<10) {
				hours = "0" + hours;
			}

			long currentTime = today.getTime();
			long matchTime = matchDate.getTime();
			long timeDiffInMins= ( matchTime - currentTime )/(1000*60);

			
			if( timeDiffInMins < (60*48) && (matchDate.getDate() - today.getDate())<2 ) 
			{
				if( timeDiffInMins < (60*24) ) 
				{
					if( timeDiffInMins < 60 ) 
					{
						if(timeDiffInMins < 1) 
						{
							str.append(resource.getString(R.string.starting));
						} else {
						
							str.append(resource.getString(R.string.starts)+"\n").append(timeDiffInMins).append( (timeDiffInMins==1) ? " "+resource.getString(R.string.min)+"" : " "+resource.getString(R.string.mins)+"" );
						}		
					} else {					
						if( matchDate.getDate() == today.getDate() )
						{
							str.append(resource.getString(R.string.today)+"\n").
							append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
						} else {
							str.append(resource.getString(R.string.tomorrow)+"\n").
							append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
						}
					}
				} else {
					str.append(resource.getString(R.string.tomorrow)+"\n").
					append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
				}
			}  else {
			//str.append(date).append(" ").append(month).append(" ").
			//append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
			
			try {
				SimpleDateFormat formater = new SimpleDateFormat(resource.getString(R.string.dateFormat));
			
				str.append(formater.format(matchDate)).append("\n").append( is24hr ? (hours+ ":"+min) : covertTo12hrFormat(hours,min,true));
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logs.show(e);
			}
		} 
		}catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

		return str.toString();
	}

	/*private String getMonth(int month) {
		switch (month) {
		case Calendar.JANUARY:
			return resource.getString(R.string.jan1);
		case Calendar.FEBRUARY:
			return resource.getString(R.string.feb1);
		case Calendar.MARCH:
			return resource.getString(R.string.mar1);
		case Calendar.APRIL:
			return resource.getString(R.string.apr1);
		case Calendar.MAY:
			return resource.getString(R.string.may1);
		case Calendar.JUNE:
			return resource.getString(R.string.jun1);
		case Calendar.JULY:
			return resource.getString(R.string.jul1);
		case Calendar.AUGUST:
			return resource.getString(R.string.aug1);
		case Calendar.SEPTEMBER:
			return resource.getString(R.string.sep1);
		case Calendar.OCTOBER:
			return resource.getString(R.string.oct1);
		case Calendar.NOVEMBER:
			return resource.getString(R.string.nov1);
		case Calendar.DECEMBER:
			return resource.getString(R.string.dec1);
		default:
			return null;
		}
	}*/
	
	
	private String getMonth(int month) {
		cal = Calendar.getInstance(Locale.getDefault());
		cal.set(Calendar.MONTH, month);

		try{
			
			
			
			//return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase();
			return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase();
			
			}catch (Exception e) {
				// TODO: handle exception
				 
				SimpleDateFormat formatter = new SimpleDateFormat("MMM",Locale.getDefault());
				
				return formatter.format(cal.getTime()).toUpperCase(Locale.getDefault());

			}catch (Error e) {
				// TODO: handle exception
				SimpleDateFormat formatter = new SimpleDateFormat("MMM",Locale.getDefault());
				return formatter.format(cal.getTime()).toUpperCase(Locale.getDefault());
			}
		/*switch (month) {
		case Calendar.JANUARY:
			return resource.getString(R.string.jan1);
		case Calendar.FEBRUARY:
			return resource.getString(R.string.feb1);
		case Calendar.MARCH:
			return resource.getString(R.string.mar1);
		case Calendar.APRIL:
			return resource.getString(R.string.apr1);
		case Calendar.MAY:
			return resource.getString(R.string.may1);
		case Calendar.JUNE:
			return resource.getString(R.string.jun1);
		case Calendar.JULY:
			return resource.getString(R.string.jul1);
		case Calendar.AUGUST:
			return resource.getString(R.string.aug1);
		case Calendar.SEPTEMBER:
			return resource.getString(R.string.sep1);
		case Calendar.OCTOBER:
			return resource.getString(R.string.oct1);
		case Calendar.NOVEMBER:
			return resource.getString(R.string.nov1);
		case Calendar.DECEMBER:
			return resource.getString(R.string.dec1);
		default:
			return null;
		}*/
	}
	
	
	public String conversionToDurationFormat(String startTime, String endTime){
		
		
		
		
		String result = "";
		Date date1 = null;
		dateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		
		try {
			if ((startTime != null) && (!startTime.equals(""))) {
				startTime = gmtToLocal(startTime);
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat(resource.getString(R.string.dateFormat));
				try {
					
					//date1=dateFormatYMDHMS.parse(startTime);
					date1=parseDate(startTime,false);
					
					if(date1!=null)
						result+=dateFormatter.format(date1);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}
				
			}	
			if ((endTime != null) && (!endTime.equals(""))) {
				endTime = gmtToLocal(endTime);
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat(resource.getString(R.string.dateFormat));
				try {
					
					//date1=dateFormatYMDHMS.parse(endTime);
					date1=parseDate(endTime,false);
					
					if(date1!=null)
						result+=" - "+dateFormatter.format(date1);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);				
				}
				
			}
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		return result;
	}
	
	

	/**
	 * Returns a time duration which is used in weekly calendar adapter
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public String conversionToDuration(String startTime, String endTime) {
		boolean is24hr;
	
		
		is24hr=DateFormat.is24HourFormat(PlayUpActivity.context);
		String[] months = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
		String result = "";
//		String[] months = { resource.getString(R.string.jan),
//				resource.getString(R.string.feb),
//				resource.getString(R.string.mar),
//				resource.getString(R.string.apr),
//				resource.getString(R.string.may),
//				resource.getString(R.string.jun),
//				resource.getString(R.string.jul),
//				resource.getString(R.string.aug),
//				resource.getString(R.string.sep),
//				resource.getString(R.string.oct),
//				resource.getString(R.string.nov),
//				resource.getString(R.string.dec),
//				
//		};
		String[] dat_time_format, time_details = null;
		int startYear = 0, startMonth = 0, startDate = 0, endYear = 0, endMonth = 0, endDate = 0;
		String startHour = null, startMinute = null, endHour = null, endMinute = null;

		// If startTime is not empty string it will be converted into local time
		// and will be parsed
		if ((startTime != null) && (!startTime.equals(""))) {
			startTime = gmtToLocal(startTime);
		
			dat_time_format = startTime.split("T");
			
			
			/**
			 * Date:18/07/2012
			 * Sprint:20
			 *  */
			// split date into yyyy/mm/dd format yyyy-mm-ddThh:mm:ssZ
			if(dat_time_format!=null && dat_time_format[0]!=null)
				time_details = dat_time_format[0].split("-");
			
			if(time_details!=null && time_details[0]!=null && (!time_details[0].equalsIgnoreCase(""))){
				startYear = Integer.parseInt(time_details[0]);
			}
			if(time_details!=null && time_details[1]!=null && (!time_details[1].equalsIgnoreCase(""))){
				startMonth = Integer.parseInt(time_details[1]);
			}
			if(time_details!=null && time_details[2]!=null && (!time_details[2].equalsIgnoreCase(""))){
				startDate = Integer.parseInt(time_details[2]);
			}
			
			/**
			 * Date:18/07/2012
			 * Sprint:20
			 *  */
			 time_details = null;
			// split time into hh/mm/ss format
			if(dat_time_format!=null && dat_time_format[1]!=null)
				time_details = dat_time_format[1].split(":");
			
			if(time_details!=null && time_details[0]!=null && (!time_details[0].equalsIgnoreCase(""))){
				startHour = time_details[0];
			}
			if(time_details!=null && time_details[1]!=null && (!time_details[1].equalsIgnoreCase(""))){
				startMinute = time_details[1];
			}
			
		}

		
		
		
		
		// If endTime is not empty string it will be converted into local time
		// and will be parsed
		if ((endTime != null) && (!endTime.equals(""))) {
			endTime = gmtToLocal(endTime);
			
			// split end time into two parts date and time
			dat_time_format = endTime.split("T");
			
			
			
			/**
			 * Date:18/07/2012
			 * Sprint:20
			 *  */
			time_details=null;
			// split date into yyyy/mm/dd format
			if(dat_time_format!=null && dat_time_format[0]!=null)
				time_details = dat_time_format[0].split("-");
			
			
			if(time_details!=null && time_details[0]!=null && (!time_details[0].equalsIgnoreCase(""))){
				endYear = Integer.parseInt(time_details[0]);
			}
			if(time_details!=null && time_details[1]!=null && (!time_details[1].equalsIgnoreCase(""))){
				endMonth = Integer.parseInt(time_details[1]);
			}
			if(time_details!=null && time_details[2]!=null && (!time_details[2].equalsIgnoreCase(""))){
				endDate = Integer.parseInt(time_details[2]);
			}
			
			
			/**
			 * Date:18/07/2012
			 * Sprint:20
			 *  */
			
			time_details = null;
			// split time into hh/mm/ss format
			if(dat_time_format!=null && dat_time_format[1]!=null)
				time_details = dat_time_format[1].split(":");
		
			if(time_details!=null && time_details[0]!=null && (!time_details[0].equalsIgnoreCase(""))){
				endHour = time_details[0];
			}
			if(time_details!=null && time_details[1]!=null && (!time_details[1].equalsIgnoreCase(""))){
				endMinute = time_details[1];
			}
			
			
		
			

			
			

		}

		if (((startTime == null) || (startTime.equals("")))
				&& ((endTime == null) || (endTime.equals("")))) {
			// If startTime and endTime are empty empty string will be returned
			result = "";
		} else if ((startTime == null) || (startTime.equals(""))) {
			// If startTime is empty and end time is not empty end time will be
			// returned
//			result = (endDate > 9 ? endDate : ("0" + endDate)) + " "
//					+ months[endMonth - 1] + " " + 
//					( is24hr ? (endHour + ":" + endMinute) : covertTo12hrFormat( endHour, endMinute, true) )+
//					" - "+" ";
			
			
			result = (endDate > 9 ? endDate : ("0" + endDate)) + " "
			+ months[endMonth - 1] + " - " + (endDate > 9 ? endDate : ("0" + endDate)) + " "
			+ months[endMonth - 1];
			
		} else if ((endTime == null) || (endTime.equals(""))) {
			// If endTime is empty and startTime is not empty start time will be
			// returned
//			result = (startDate > 9 ? startDate : ("0" + startDate)) + " "
//					+ months[startMonth - 1] + " " +
//					( is24hr ? (startHour + ":" + startMinute) : covertTo12hrFormat( startHour, startMinute, true) )
//					+ " - "+" ";
			result = (startDate > 9 ? startDate : ("0" + startDate)) + " "
			+ months[startMonth - 1] + " - " +(startDate > 9 ? startDate : ("0" + startDate)) + " "
			+ months[startMonth - 1];
			
		} else if ( startTime.equalsIgnoreCase( endTime ) ) {
			result = (startDate > 9 ? startDate : ("0" + startDate)) + " "
					+ months[startMonth - 1] 
					+ " - "+(startDate > 9 ? startDate : ("0" + startDate)) + " "
					+ months[startMonth - 1]; 
					
		} else {
			// If endTime and startTime are not empty then appropriate string
			// will be returned
			if (startYear == endYear) {
				
				if (startMonth == endMonth) {
					
					if (startDate == endDate) {
					
						// Converting same date,month and year
						// "08 JAN 08:30 - 12:45 "
//						boolean showMeridiem;
//						if(( Integer.parseInt(startHour)<12 && Integer.parseInt(endHour) <12 )||( Integer.parseInt(startHour)>=12 && Integer.parseInt(endHour) >= 12 ))
//							showMeridiem=false;
//						else
//							showMeridiem=true;
						
//						result = (startDate > 9 ? startDate : ("0" + startDate))
//								+ " "
//								+ months[startMonth - 1]
//								+ " "
//								+( is24hr ? (startHour + ":" + startMinute) : covertTo12hrFormat( startHour, startMinute , showMeridiem ) )
//								+ " - "
//								+ ( is24hr ? (endHour + ":" + endMinute) : covertTo12hrFormat( endHour, endMinute, true) );
						
						result = (startDate > 9 ? startDate : ("0" + startDate)) + " "
								+ months[startMonth - 1] + 
								 " - " + (startDate > 9 ? startDate : ("0" + startDate)) + " "
									+ months[startMonth - 1]; 
						
						
					} else {
						// Converting same month, year but different dates
						// "08 - 12 JAN"
						result = (startDate > 9 ? startDate : ("0" + startDate))
								+ " "+months[startMonth - 1]+" - "
								+ (endDate > 9 ? endDate : ("0" + endDate))
								+ " " + months[startMonth - 1];
					}
				} else {
					// Converting different months but same year
					// "08 JAN - 27 JUN"
					result = (startDate > 9 ? startDate : ("0" + startDate))
							+ " " + months[startMonth - 1] + " - "
							+ (endDate > 9 ? endDate : ("0" + endDate)) + " "
							+ months[endMonth - 1];
				}
			} else {
				// Converting different years,months and dates
				// "JAN 2012 - JUN 2014"
				result = months[startMonth - 1] + " " + startYear + " - "
						+ months[endMonth - 1] + " " + endYear;
			}
		}

		return result;
	}
	
	/**
	 * Convert time 24 hrs to 12hrs like 13:30 will return 01:30 PM
	 */
	private String covertTo12hrFormat(String hoursString, String minutesString, boolean showMeridiem) {
		int hours = Integer.parseInt(hoursString);
		//If hours valued > 12 will return hours differed by 12 and preceded by string PM
		if(hours>12)
		{
			hours=hours-12;
			
			return  hours+ ":" + minutesString +( showMeridiem ? " "+getAM_PM_Locale("PM")+"" : "");
			//return ( hours<10 ? ( "0" + hours) : hours ) + ":" + minutesString +( showMeridiem ? " PM" : "");
		}
		else
		{
			/*
			 * If hours value = 0 will return hours as 12 and preceded by string PM
			 * If hours value = 12 will return hours  and preceded by string AM
			 * If hours value  < 12 will return hours  and preceded by string AM
			 */
			if(hours==0)
				return  "12:" + minutesString +( showMeridiem ? " "+getAM_PM_Locale("AM")+"" : "");
			else if( hours == 12)
				return  hours + ":" + minutesString +( showMeridiem ?  " "+getAM_PM_Locale("PM")+"" : "");
			else
				return hours + ":" + minutesString +( showMeridiem  ? " "+getAM_PM_Locale("AM")+"" : "");
				//return  ( hours<10 ? ( "0" + hours) : hours ) + ":" + minutesString +( showMeridiem ? " AM" : "");
		}

	}
	
	
	private String getAM_PM_Locale(String AM_PM){
		
		if(AM_PM.compareTo("AM")==0){
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.set(Calendar.HOUR_OF_DAY, 2);
			String DATE_FORMAT = "a";
			  //Create object of SimpleDateFormat and pass the desired date format.
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
			
			return	sdf.format(mCalendar.getTime());
		}else if(AM_PM.compareTo("PM")==0){
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.set(Calendar.HOUR_OF_DAY, 13);
			String DATE_FORMAT = "a";
			//Create object of SimpleDateFormat and pass the desired date format.
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
			
			return	sdf.format(mCalendar.getTime());
			
		}else{
			return "";
		}
	}

	/**
	 * Converts time to current timezone
	 * 
	 * @param gmtTime
	 * @return
	 */
	public String gmtToLocal(String gmtTime) {
		String localTime = "";
		currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		
		try {
			//date = currentFormat.parse(gmtTime);
			date = parseDate(gmtTime,true);
		
		
		} catch (Exception e) {
			Logs.show(e);
			  
		}
		calendar = Calendar.getInstance();
		currentFormat.setTimeZone(calendar.getTimeZone());
		localTime = currentFormat.format(date);
		return localTime;
	}
	
	
	public String gmtToLocalTiles(String gmtTime) {
		String localTime = "";
		currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = parseDate(gmtTime,true);
			
		} catch (Exception e) {
			  Logs.show(e);
		} 
		calendar = Calendar.getInstance();
		currentFormat.setTimeZone(calendar.getTimeZone());
		localTime = currentFormat.format(date);
		
		return localTime;
	}
	
	
	
	
	public String localToGmt(){
		
		
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("gmt"));
		String gmtTime = df.format(new Date());
		
		
		return gmtTime;
		
	}
	
	
	public int compareDates(String date1,String date2){
		
		try {
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); 
			//Date d1= dateFormat.parse(date1);
	    	Date d1= parseDate(date1,false);
			//Date d2 = dateFormat.parse(date2);
	    	Date d2 =parseDate(date2,false);
		
			return(d1.compareTo(d2));
			
			
		} catch (Exception e) {
			Logs.show(e);
			return -1;
		} 
		
	}

	
	/**
	 * will convert string into two lines for setting in match header
	 */
	
	public String matchHeaderFutureTime( String time ) {
		String upcomingString = "";
		boolean first = true;
		String[] timeArray = time.split(" ");
		if( timeArray.length > 1 ) {
			if( timeArray[0].length()==2 ) 
				first = true;
			else if(timeArray[0].length() > 2 ) 
				first = false;
				
		}
		
		for( int i=0; i< timeArray.length ; i++ ) {
			if( first ) {
				upcomingString+= ( i==2? ("\n"+timeArray[i]) : (" "+timeArray[i]) );
			} else {
				upcomingString+= ( i==1? ("\n"+timeArray[i]) : (" "+timeArray[i]) );
			}
		}
		return upcomingString;
	}
}