/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.watabou.pixeldungeon.scenes;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;

import com.nyrds.android.util.ModdingMode;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Font;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.Text;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.effects.BadgeBanner;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.BitmapCache;

public class PixelScene extends Scene {

	// Minimum virtual display size for portrait orientation
	public static final float MIN_WIDTH_P = 128;
	public static final float MIN_HEIGHT_P = 224;

	// Minimum virtual display size for landscape orientation
	public static final float MIN_WIDTH_L = 224;
	public static final float MIN_HEIGHT_L = 160;

	public static float defaultZoom = 0;
	public static float minZoom;
	public static float maxZoom;

	public static Camera uiCamera;

	public static Font font1x;
	public static Font font25x;
	
	public static Font font;

	@Override
	public void create() {

		super.create();

		float minWidth, minHeight;

		if (PixelDungeon.landscape()) {
			minWidth = MIN_WIDTH_L;
			minHeight = MIN_HEIGHT_L;
		} else {
			minWidth = MIN_WIDTH_P;
			minHeight = MIN_HEIGHT_P;
		}

		defaultZoom = 20;

		while ((Game.width() / defaultZoom < minWidth || Game.height()
				/ defaultZoom < minHeight)
				&& defaultZoom > 1) {

			defaultZoom--;
		}

		if (PixelDungeon.scaleUp()) {
			while (Game.width() / (defaultZoom + 1) >= minWidth
					&& Game.height() / (defaultZoom + 1) >= minHeight) {

				defaultZoom++;
			}
		}

		minZoom = 1;
		maxZoom = defaultZoom * 2;

		GLog.i("%d %d %f", Game.width(), Game.height(), defaultZoom);

		//Camera.reset(new PixelCamera(defaultZoom + PixelDungeon.zoom()));
		Camera.reset(new PixelCamera(defaultZoom));

		float uiZoom = defaultZoom;
		uiCamera = Camera.createFullscreen(uiZoom);
		Camera.add(uiCamera);

		createFonts();
	}

	private void createFonts() {
		if (font1x == null) {
			// 3x5 (6)
			font1x = Font.colorMarked(BitmapCache.get(Assets.FONTS1X),
					0x00000000, Font.LATIN_FULL);
			font1x.baseLine = 6;
			font1x.tracking = -1;
			
			// 7x12 (15)
			font25x = Font.colorMarked( 
				BitmapCache.get( Assets.FONTS25X ), 17, 0x00000000, Font.ALL_CHARS);
			font25x.baseLine = 13;
			font25x.tracking = -1;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		Touchscreen.event.removeAll();
	}

	public static float scale;

	public static void chooseFont(float size) {
		float pt = size * defaultZoom;

		if (pt >= 19) {

			scale = pt / 19;
			if (1.5 <= scale && scale < 2) {
				font = font25x;
				scale = (int) (pt / 14);
			} else {
				// font = font3x;
				scale = (int) scale;
			}

		} else if (pt >= 14) {

			scale = pt / 14;
			if (1.8 <= scale && scale < 2) {
				// font = font2x;
				scale = (int) (pt / 12);
			} else {
				font = font25x;
				scale = (int) scale;
			}

		} else if (pt >= 12) {

			scale = pt / 12;
			if (1.7 <= scale && scale < 2) {
				// font = font15x;
				scale = (int) (pt / 10);
			} else {
				// font = font2x;
				scale = (int) scale;
			}

		} else if (pt >= 10) {

			scale = pt / 10;
			if (1.4 <= scale && scale < 2) {
				font = font1x;
				scale = (int) (pt / 7);
			} else {
				// font = font15x;
				scale = (int) scale;
			}

		} else {

			font = font1x;
			scale = Math.max(1, (int) (pt / 7));

		}

		scale /= defaultZoom;

		font = font25x;
	}

	public static Text createText(float size) {
		return createText(null, size);
	}

	private static float computeFontScale() {
		float scale = 0.5f + 0.01f*PixelDungeon.fontScale();
		
		if(scale < 0.1f) return 0.1f;
		if(scale > 4)   return 4;
		
		return scale;
		
	}
	
	public static Text createText(String text, float size) {

		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, size * 1.2f, false,  computeFontScale());
		}
		
		chooseFont(size);

		Text result = Text.create(text, font);
		result.Scale().set(scale);

		return result;
	}

	public static Text createMultiline(float size) {
		return createMultiline(null, size);
	}

	public static Text createSystemText(String text, float size) {
		return new SystemText(text, size*1.2f, false, computeFontScale());
	}
	
	public static Text createMultiline(String text, float size) {

		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, size*1.2f, true, computeFontScale());
		}
		
		chooseFont(size);

		Text result = Text.createMultiline(text, font);
		result.Scale().set(scale);

		return result;
	}

	public static float align(Camera camera, float pos) {
		return ((int) (pos * camera.zoom)) / camera.zoom;
	}

	// This one should be used for UI elements
	public static float align(float pos) {
		return ((int) (pos * defaultZoom)) / defaultZoom;
	}

	public static void align(Visual v) {
		Camera c = v.camera();
		v.x = align(c, v.x);
		v.y = align(c, v.y);
	}

	public static boolean noFade = false;

	protected void fadeIn() {
		if (noFade) {
			noFade = false;
		} else {
			fadeIn(0xFF000000, false);
		}
	}

	protected void fadeIn(int color, boolean light) {
		add(new Fader(color, light));
	}

	public static void showBadge(Badges.Badge badge) {
		if(!Game.inMainThread()) {
			return;
		}
		
		if (uiCamera != null && !Game.paused) {
			BadgeBanner banner = BadgeBanner.show(badge.image);
			banner.camera = uiCamera;
			banner.x = align(banner.camera,
					(banner.camera.width - banner.width) / 2);
			banner.y = align(banner.camera,
					(banner.camera.height - banner.height) / 3);
			Game.scene().add(banner);
		}
	}

	public static Font font() {
		return font;
	}

	public static void font(Font font) {
		PixelScene.font = font;
	}

	protected static class Fader extends ColorBlock {

		private static float FADE_TIME = 1f;

		private boolean light;

		private float time;

		public Fader(int color, boolean light) {
			super(uiCamera.width, uiCamera.height, color);

			this.light = light;

			camera = uiCamera;

			alpha(1f);
			time = FADE_TIME;
		}

		@Override
		public void update() {

			super.update();

			if ((time -= Game.elapsed) <= 0) {
				alpha(0f);
				getParent().remove(this);
			} else {
				alpha(time / FADE_TIME);
			}
		}

		@Override
		public void draw() {
			if (light) {
				GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
				super.draw();
				GLES20.glBlendFunc(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
			} else {
				super.draw();
			}
		}
	}

	static class PixelCamera extends Camera {

		public PixelCamera(float zoom) {
			super(
					(int) (Game.width() - Math.ceil(Game.width() / zoom) * zoom) / 2,
					(int) (Game.height() - Math.ceil(Game.height() / zoom)* zoom) / 2,
					(int) Math.ceil(Game.width() / zoom),
					(int) Math.ceil(Game.height() / zoom),
					zoom);
		}

		@Override
		protected void updateMatrix() {
			float sx = align(this, scroll.x + shakeX);
			float sy = align(this, scroll.y + shakeY);

			matrix[0] = +zoom * invW2;
			matrix[5] = -zoom * invH2;

			matrix[12] = -1 + x * invW2 - sx * matrix[0];
			matrix[13] = +1 - y * invH2 - sy * matrix[5];

		}
	}
}
