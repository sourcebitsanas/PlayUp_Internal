package com.playup.android.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;

public class PollManager {
	
	/*
	private Hashtable<String, Hashtable<String,TimerTask>>  pollTask ;
	private  Hashtable<String, Hashtable<String,Timer>>  pollTimer ;
	
	private Hashtable<String,TimerTask> taskTable;
	private Hashtable<String,TimerTask> timerTable;

	public PollManager() {
		pollTask = new Hashtable<String, Hashtable<String,TimerTask>>();
		pollTimer = new Hashtable<String, Hashtable<String,Timer>>();
	}
	
	
	
	*//**
	 *  polling the url for fragment 
	 *//*
	public void pollUrl(final String Url, final String fromFragment, final Bundle refreshData, final int parseType ) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();				
					int cacheTime = Integer.parseInt ( dbUtil.getCacheTime ( Url ) );
					
					if( fromFragment != null ) {
						
						if ( pollTask != null ) {							
							if ( pollTask.get(fromFragment)!=null ) {
							
								if( pollTask.get(fromFragment)!=null && pollTask.get(fromFragment).get(Url)!=null ) {
									pollTask.get(fromFragment).get(Url).cancel();
								}
							} else {
								pollTask.put(fromFragment, new Hashtable<String, TimerTask>());
							}
						}
						
						pollTask.get(fromFragment).put ( Url, new TimerTask() {

							@Override
							public void run() {
								Message msg = new Message();
								Bundle bundle;
								if( refreshData == null ) {
									bundle = new Bundle();
									bundle.putString("pollUrl", Url);
									bundle.putInt("parseType", parseType);
								} else {
									bundle = refreshData;
									bundle.putString("pollUrl", Url);
								}
								
								msg.setData( bundle );
								PlayupLiveApplication.callRefreshOnFragments(msg);
							}
						});

						
						if ( pollTimer != null ) {
							if ( pollTimer.get(fromFragment)!=null ) {
								
								if( pollTimer.get(fromFragment)!=null && pollTimer.get(fromFragment).get(Url)!=null ) {
									pollTimer.get(fromFragment).get(Url).cancel();
								}
							} else {
								pollTimer.put(fromFragment, new Hashtable<String, Timer>());
							}
						}
						pollTimer.get(fromFragment).put(Url, new Timer() );
		
						if(cacheTime > 0) {
							pollTimer.get(fromFragment).get(Url).schedule( pollTask.get( fromFragment).get(Url), (cacheTime * 1000), (cacheTime * 1000));
						}
					}

				} catch ( Exception e )  {
					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();

	}
	
	*//**
	 * cancelling polling for all the urls for specific fragment
	 *//*
	public void cancelPollingForFragment(String fromFragment ) {
		try {		
			if( fromFragment!=null ) {
				Iterator it ;
				if( pollTask!= null && pollTask.get(fromFragment)!= null ) {
					it = pollTask.get(fromFragment).values().iterator();
					while(it.hasNext()){
						((TimerTask) it.next()).cancel();
					}
					pollTask.get(fromFragment).clear();
				}
				
				if( pollTimer!= null && pollTimer.get(fromFragment)!= null ) {
					it = pollTimer.get(fromFragment).values().iterator();
					while(it.hasNext()){
						((Timer) it.next()).cancel();
					}
					pollTimer.get(fromFragment).clear();
				}
				
			}		
			
		} catch (Exception e) {
			Logs.show(e);
		}
		
		
	}
	
	
	*//**
	 * checking whether the url is in polling for current fragment
	 *//*
	public boolean isUrlInPolling ( String fromFragment, String pollUrl ) {
		try {
			if( pollTask!=null && pollTask.get(fromFragment)!=null  && pollTask.get( fromFragment ).get( pollUrl)!=null )
				return true;			
		} catch (Exception e) {
			Logs.show(e);
		}
		
		return false;
	}
	*/
}