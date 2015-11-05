
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class FireArrow extends Arrow {

	public FireArrow() {
		this( 1 );
	}
	
	public FireArrow( int number ) {
		super();
		quantity(number);
		
		baseMin = 1;
		baseMax = 6;
		baseDly = 0.75;
		
		image = ItemSpriteSheet.ARROW_FIRE;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity() * 5;
	}

	@Override
	public void proc( Char attacker, Char defender, int damage ) {
		if(activateSpecial(attacker, defender, damage)) {
			Buff.affect( defender, Burning.class ).reignite( defender );
		}
		super.proc( attacker, defender, damage );
	}
}
