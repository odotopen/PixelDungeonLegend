package com.watabou.pixeldungeon.ui;

import com.nyrds.android.util.ModdingMode;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.GamesInProgress;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class ModdingButton extends Button {

	private Image image;
	private Text  text;

	public ModdingButton() {
		super();

		width = image.width;
		height = image.height;
	}

	private void updateLook() {
		if(PixelDungeon.moddingMode()) {
			image.brightness(1.5f);
			text.setVisible(true);
			ModdingMode.mode(true);
		} else {
			image.brightness(0.1f);
			text.setVisible(false);
			ModdingMode.mode(false);
		}
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.MODDING_MODE.get();
		
		text = Text.createBasicText(PixelScene.font1x);
		text.text("modding mode");
		
		updateLook();
		
		add( text );
		add( image );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		image.x = x;
		image.y = y;
		
		text.x = x;
		text.y = image.y + image.height + 2;
	}
	
	@Override
	protected void onTouchDown() {
		Sample.INSTANCE.play(Assets.SND_CLICK);
	}

	@Override
	protected void onTouchUp() {
		image.resetColor();
	}

	@Override
	protected void onClick() {
		PixelDungeon.moddingMode(!PixelDungeon.moddingMode());
		GamesInProgress.removeAll();
		updateLook();
		
		PixelDungeon.instance().doRestart();
	}
}
