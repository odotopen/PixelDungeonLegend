package com.watabou.pixeldungeon;

import android.app.Application;
import android.content.Context;

public class MyPixelDungeon extends Application {
	private static MyPixelDungeon instance;
	
	public static MyPixelDungeon getInstance() {
		return instance;
	}

	public static Context getContext() {
		return instance;
		// or return instance.getApplicationContext();
	}

	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}

}
