/*
Copyright 2009-2011 Urban Airship Inc. All rights reserved.


Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.playup.android.receiver;
import java.util.Hashtable;

import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Util;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

public class IntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, final Intent intent) {
      
		
		
		String action = intent.getAction();
		
		 Log.e("234", "action===============>>>"+action);

		if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {
		
			

			new Thread ( new Runnable () {

				@Override
				public void run() {
					PlayUpActivity.refreshNotification();
					
				}
				
			}).start();
			
			
			new Util().getUserNotificationData(true);
			
			
			
			
			String vPushId = intent.getStringExtra(PushManager.EXTRA_PUSH_ID);

			String url = intent.getStringExtra("href");
			
			Log.e("234", "url======>>>>href====>>>"+url);
			
			
			if(url != null && url.trim().length() > 0){
				DatabaseUtil.getInstance().setPushNotification(vPushId,url,intent.getStringExtra("from"),-1,intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,-1),0);
				new Util().getConversationForPushNotification(url,vPushId,intent);
			}else{
				DatabaseUtil.getInstance().setPushNotification(vPushId,"",intent.getStringExtra("from"),2,intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,-1),0);
			}
			logPushExtras(intent);
			
			

		} else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
			
					if ( Constants.isCurrent ) {
						new Thread ( new Runnable () {

							@Override
							public void run() {
								PlayUpActivity.refreshNotification();
								
							}
							
						}).start();
					}
					logPushExtras(intent);		
					
					Intent launch = new Intent(Intent.ACTION_MAIN);	
					String vPushId = intent.getStringExtra(PushManager.EXTRA_PUSH_ID);
					
					
					while(Constants.pushNotificationDownload != null && Constants.pushNotificationDownload.containsKey(vPushId)){
						if(Constants.pushNotificationDownload.get(vPushId)){
							continue;
						}else{
							break;
						}
						
					}
					
					
					if(Constants.pushNotificationDownload != null && Constants.pushNotificationDownload.containsKey(vPushId))
						Constants.pushNotificationDownload.remove(vPushId);
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String url = intent.getStringExtra("href");
				
					Log.e("234", "url======>>>>href====>>>"+url);
					
					int pushType = dbUtil.getPushType(vPushId);
					
					if(url != null && url.trim().length() > 0){
						if(pushType == 0){
							Hashtable<String, List<String>>data  = dbUtil.getDirectConversationData(vPushId);
							Bundle bundle = new Bundle ();
							bundle.putInt( "pushType",0);
							Log.e("234", "vDirectConversationUrl=======>>>"+data.get("vDirectConversationUrl").get(0));
							Log.e("234", "url======>>>>vShortUrl=======>>>"+data.get("vShortUrl").get(0));
							Log.e("234", "url======>>>>vUserSelfUrl====>>>"+data.get("vUserSelfUrl").get(0));
							
							bundle.putString( "vShortUrl",data.get("vShortUrl").get(0));
							bundle.putString( "vDirectConversationUrl",data.get("vDirectConversationUrl").get(0));
							bundle.putString( "vUserSelfUrl",data.get("vUserSelfUrl").get(0));
							launch.putExtra("data", bundle);
						}else if(pushType == 1){
								String conversationId = DatabaseUtil.getInstance().getConversationId(vPushId);
								dbUtil.setConversationNotificationRead ( conversationId );						
								Bundle bundle = new Bundle ();
								bundle.putInt( "pushType",1);
								bundle.putString( "vConversationId",conversationId);
								Log.e("234", "url======>>>>conversationId====>>>"+conversationId);
								launch.putExtra("data", bundle);
								dbUtil = null;						
						}else if(pushType == 3){
							String conversationId = DatabaseUtil.getInstance().getConversationId(vPushId);
							dbUtil.setConversationNotificationRead ( conversationId );						
							Bundle bundle = new Bundle ();
							bundle.putInt( "pushType",3);
							
							bundle.putString( "vConversationId",conversationId);
							launch.putExtra("data", bundle);
							dbUtil = null;					
					}
					}else if(pushType == 2){

						Bundle bundle = new Bundle ();
						bundle.putInt( "pushType",2);
						launch.putExtra("data", bundle);					
					}
					
					launch.setClass(UAirship.shared().getApplicationContext(), PlayUpActivity.class);
					launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);			
					UAirship.shared().getApplicationContext().startActivity(launch);
		}

	}
	
	
	

	/**
	 * Log the values sent in the payload's "extra" dictionary.
	 * 
	 * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
	 */
	private void logPushExtras(Intent intent) {
		Set<String> keys = intent.getExtras().keySet();
		for (String key : keys) {

			
			if(key.equalsIgnoreCase(PushManager.EXTRA_NOTIFICATION_ID)){
			//	Logs.show(key+"============================================================================"+intent.getIntExtra(key,-1));
			}
			else{
			//	Logs.show(key+"============================================================================"+intent.getStringExtra(key));
			}





		}
	}
}
