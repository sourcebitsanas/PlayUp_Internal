package com.playup.android.application;

import java.util.Map;

import android.app.Notification;

import com.playup.android.util.Constants;
import com.playup.android.util.Logs;
import com.urbanairship.push.CustomPushNotificationBuilder;

public class customNotificationBuilder extends CustomPushNotificationBuilder {

	@Override
	public int getNextId(String arg0, Map<String, String> arg1) {

		

		return super.getNextId(arg0, arg1);
	}

	@Override
	public Notification buildNotification(String arg0, Map<String, String> arg1) {

		
			return null;
		}
	}


