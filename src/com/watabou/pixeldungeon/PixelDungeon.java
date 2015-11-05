/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import java.io.IOException;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.nyrds.android.util.ModdingMode;
import com.watabou.noosa.Game;
import com.watabou.noosa.GameWithGoogleIap;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.scenes.WelcomeScene;
import com.watabou.pixeldungeon.utils.GLog;

public class PixelDungeon extends GameWithGoogleIap {
	public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
	public PixelDungeon() {
		super(TitleScene.class);
		
		// remix 0.5
		com.watabou.utils.Bundle.addAlias(
				com.watabou.pixeldungeon.items.food.Ration.class,
				"com.watabou.pixeldungeon.items.food.Food");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedpreferences.edit(); 
		int countShowAds = sharedpreferences.getInt("countShowAds", 0);
		
		//String placeModFilesHere = "placeModFilesHere";
		
		PixelDungeon.moddingMode(false);
		ModdingMode.mode(PixelDungeon.moddingMode());
		/*
		if(!FileSystem.getExternalStorageFile(placeModFilesHere).exists()) {
			try {
				FileSystem.getExternalStorageFile(placeModFilesHere).createNewFile();
			} catch (IOException e) {
				GLog.i(e.getMessage());
				e.printStackTrace();
			}
		}
		*/
		PixelDungeon.instance().initIap();
		
		if(PixelDungeon.uiLanguage().equals("ko")) {
			PixelDungeon.classicFont(false);
		}
		
		ModdingMode.setClassicTextRenderingMode(PixelDungeon.classicFont());
		
		useLocale(uiLanguage());

		updateImmersiveMode();

		DisplayMetrics metrics = new DisplayMetrics();
		instance().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		boolean landscape = metrics.widthPixels > metrics.heightPixels;

		if (Preferences.INSTANCE.getBoolean(Preferences.KEY_LANDSCAPE, false) != landscape) {
			landscape(!landscape);
		}

		Music.INSTANCE.enable(music());
		Sample.INSTANCE.enable(soundFx());
		secondQuickslot();
		
		if (PixelDungeon.version() != Game.versionCode) {
			switchScene(WelcomeScene.class);
		}
		
		if(countShowAds >= 2){	
			final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
			if (activeNetwork != null && activeNetwork.isConnected()) {
				Intent intent = new Intent(this, Ads.class);
			    startActivity(intent);	
			} 
			editor.putInt("countShowAds", 0);			
		}else{
			if(countShowAds == 0){			                
		        editor.putInt("countShowAds", 1);       
			}else{
				editor.putInt("countShowAds", countShowAds + 1);
			}
		}
		editor.commit(); 
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			if (Dungeon.hero != null) {
				Dungeon.saveAll();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			updateImmersiveMode();
		}
	}

	public static void switchNoFade(Class<? extends PixelScene> c) {
		PixelScene.noFade = true;
		switchScene(c);
	}

	public static boolean canDonate() {
		if(! (instance() instanceof GameWithGoogleIap) ) {
			return true;
		} else {
			return instance().iapReady();
		}
	}
	
	/*
	 * ---> Preferences
	 */

	public static void landscape(boolean value) {
		Game.instance()
				.setRequestedOrientation(value ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Preferences.INSTANCE.put(Preferences.KEY_LANDSCAPE, value);
	}

	public static boolean landscape() {
		return width() > height();
	}

	// *** IMMERSIVE MODE ****

	private static boolean immersiveModeChanged = false;

	@SuppressLint("NewApi")
	public static void immerse(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_IMMERSIVE, value);

		instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateImmersiveMode();
				immersiveModeChanged = true;
			}
		});
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);

		if (immersiveModeChanged) {
			requestedReset = true;
			immersiveModeChanged = false;
		}
	}

	@SuppressLint("NewApi")
	public static void updateImmersiveMode() {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			if (instance() != null) {
				instance().getWindow()
						.getDecorView()
						.setSystemUiVisibility(
								immersed() ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE
										| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_FULLSCREEN
										| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
										: 0);
			}
		}
	}

	public static boolean immersed() {
		return Preferences.INSTANCE
				.getBoolean(Preferences.KEY_IMMERSIVE, false);
	}

	// *****************************

	public static void scaleUp(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_SCALE_UP, value);
		switchScene(TitleScene.class);
	}

	public static boolean scaleUp() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_SCALE_UP, true);
	}

	public static void zoom(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_ZOOM, value);
	}

	public static int zoom() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_ZOOM, 0);
	}

	public static void music(boolean value) {
		Music.INSTANCE.enable(value);
		Preferences.INSTANCE.put(Preferences.KEY_MUSIC, value);
	}

	public static boolean music() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_MUSIC, true);
	}

	public static void soundFx(boolean value) {
		Sample.INSTANCE.enable(value);
		Preferences.INSTANCE.put(Preferences.KEY_SOUND_FX, value);
	}

	public static boolean soundFx() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_SOUND_FX, true);
	}

	public static void brightness(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_BRIGHTNESS, value);
		if (scene() instanceof GameScene) {
			((GameScene) scene()).brightness(value);
		}
	}

	public static boolean brightness() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_BRIGHTNESS,
				false);
	}

	private static void donated(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_DONATED, value);
	}

	public static int donated() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_DONATED, 0);
	}

	public static void lastClass(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_LAST_CLASS, value);
	}

	public static int lastClass() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_LAST_CLASS, 0);
	}

	public static void challenges(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_CHALLENGES, value);
	}

	public static int challenges() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_CHALLENGES, 0);
	}

	public static void intro(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_INTRO, value);
	}

	public static boolean intro() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_INTRO, true);
	}

	public static String uiLanguage() {
		String deviceLocale = Locale.getDefault().getLanguage();
		GLog.i("Device locale: %s", deviceLocale);
		return Preferences.INSTANCE.getString(Preferences.KEY_LOCALE,
				deviceLocale);
	}

	public static void uiLanguage(String lang) {
		Preferences.INSTANCE.put(Preferences.KEY_LOCALE, lang);

		instance().doRestart();
	}

	public static void secondQuickslot(boolean checked) {
		Preferences.INSTANCE.put(Preferences.KEY_SECOND_QUICKSLOT, checked);
		if (scene() instanceof GameScene) {
			((GameScene) scene()).updateToolbar(checked);
		}
	}

	public static boolean secondQuickslot() {
		//return Preferences.INSTANCE.getBoolean(Preferences.KEY_SECOND_QUICKSLOT, false);
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_SECOND_QUICKSLOT, true);
	}

	public static void thirdQuickslot(boolean checked) {
		Preferences.INSTANCE.put(Preferences.KEY_THIRD_QUICKSLOT, checked);
		if (scene() instanceof GameScene) {
			((GameScene) scene()).updateToolbar(checked);
		}
	}
	
	public static boolean thirdQuickslot() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_THIRD_QUICKSLOT, false);
	}
	
	public static void version( int value)  {
        Preferences.INSTANCE.put( Preferences.KEY_VERSION, value );
    }

    public static int version() {
        return Preferences.INSTANCE.getInt( Preferences.KEY_VERSION, 0 );
    }
	
	public static void fontScale(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_FONT_SCALE, value);
	}

	public static int fontScale() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_FONT_SCALE, 0);
	}
	
	public static boolean classicFont() {
		boolean val = Preferences.INSTANCE.getBoolean(Preferences.KEY_CLASSIC_FONT, true);
		ModdingMode.setClassicTextRenderingMode(val);
		return val;
	}

	public static void classicFont(boolean value) {
		ModdingMode.setClassicTextRenderingMode(value);
		Preferences.INSTANCE.put(Preferences.KEY_CLASSIC_FONT, value);
	}

	public static boolean moddingMode() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_MODDING_MODE, false);
	}

	public static void moddingMode(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_MODDING_MODE, value);
	}
	
	
	public static boolean realtime() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_REALTIME, false);
	}

	public static void realtime(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_REALTIME, value);
	}
	
	/*
	 * <--- Preferences
	 */

	public static void reportException(Exception e) {
		Log.e("PD", Log.getStackTraceString(e));
	}

	/*
	 * <---Purchases
	 */
	
	public void setDonationLevel(int level) {
		if (level < donated()) {
			return;
		}

		if (donated() == 0 && level != 0) {
			Sample.INSTANCE.play(Assets.SND_GOLD);
			Badges.validateSupporter();
		}
		donated(level);
	}
}