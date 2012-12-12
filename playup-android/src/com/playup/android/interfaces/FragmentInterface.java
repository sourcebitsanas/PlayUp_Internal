package com.playup.android.interfaces;

import android.os.Bundle;
import android.os.Message;

public interface FragmentInterface {
	
	public void onAgainActivated( Bundle args );
	
	public void onUpdate ( Message msg );
	
	public void onConnectionChanged(boolean isConnectionActive);
//	
//	public void onRefresh( Message msg );

	
}
