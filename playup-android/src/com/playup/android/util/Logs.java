package com.playup.android.util;

public final class Logs {

	private static final boolean show = Keys.LOG_SHOW ;
	public static void show ( Exception e ) {

		if (  show ) {
			e.printStackTrace();
		}
	}
	
	public static void show ( Error e ) {

		if (  show ) {
			e.printStackTrace();
		}
	}

	public static void show ( String str ) {

		try {
			if (  show ) {

				if(str.length() > 4000) {
					System.out.println( str.substring(0, 4000) );
					show ( str.substring(4000));
				} else {
					System.out.println( str );
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		} catch ( Error e ) {
			e.printStackTrace();
		}
		
	}

}
