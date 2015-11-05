package com.watabou.pixeldungeon.windows.elements;

import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.windows.WndTabbed;

public class Tab extends Button {
	
	private final WndTabbed parent;

	protected Tab(WndTabbed wndTabbed) {
		parent = wndTabbed;
	}

	protected final int CUT = 5;
	
	protected boolean selected;
	
	protected NinePatch bg;
	
	@Override
	protected void layout() {
		super.layout();
		
		if (bg != null) {
			bg.x = x;
			bg.y = y;
			bg.size( width, height );
		}
	}
	
	public void select( boolean value ) {
		
		active = !(selected = value);
		
		if (bg != null) {
			remove( bg );
		}
		
		bg = Chrome.get( selected ? 
			Chrome.Type.TAB_SELECTED : 
			Chrome.Type.TAB_UNSELECTED );
		addToBack( bg );
		
		layout();
	}
	
	@Override
	protected void onClick() {	
		Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
		parent.onClick( this );
	}
}