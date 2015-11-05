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
package com.watabou.pixeldungeon.scenes;

import java.io.FileNotFoundException;




import java.io.IOException;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndStory;
import com.nyrds.android.util.ModdingMode;
import com.odotopen.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.Position;

public class InterlevelScene extends PixelScene {

	private static final float TIME_TO_FADE = 0.3f;

	private static final String TXT_DESCENDING = Game
			.getVar(R.string.InterLevelScene_Descending);
	private static final String TXT_ASCENDING = Game
			.getVar(R.string.InterLevelScene_Ascending);
	private static final String TXT_LOADING = Game
			.getVar(R.string.InterLevelScene_Loading);
	private static final String TXT_RESURRECTING = Game
			.getVar(R.string.InterLevelScene_Resurrecting);
	private static final String TXT_RETURNING = Game
			.getVar(R.string.InterLevelScene_Returning);
	private static final String TXT_FALLING = Game
			.getVar(R.string.InterLevelScene_Falling);

	private static final String ERR_FILE_NOT_FOUND = Game
			.getVar(R.string.InterLevelScene_FileNotFound);
	private static final String ERR_GENERIC = Game
			.getVar(R.string.InterLevelScene_ErrorGeneric);

	public static enum Mode {
		DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, MODDING
	}

	public static Mode mode;

	public static Position returnTo;

	public static boolean noStory = false;

	public static boolean fallIntoPit;

	private enum Phase {
		FADE_IN, STATIC, FADE_OUT
	}

	volatile private Phase phase;
	volatile private float timeLeft;

	private Text message;

	private Thread thread;

	volatile private String error = null;

	@Override
	public void create() {
		super.create();

		String text = "";
		switch (mode) {
		case DESCEND:
			text = TXT_DESCENDING;
			break;
		case ASCEND:
			text = TXT_ASCENDING;
			break;
		case CONTINUE:
			text = TXT_LOADING;
			break;
		case RESURRECT:
			text = TXT_RESURRECTING;
			break;
		case RETURN:
			text = TXT_RETURNING;
			break;
		case FALL:
			text = TXT_FALLING;
			break;
		case MODDING:
			text = "modding test mode";
			break;
		}

		message = PixelScene.createText(text, 9);
		message.measure();
		message.x = (Camera.main.width - message.width()) / 2;
		message.y = (Camera.main.height - message.height()) / 2;
		add(message);

		phase = Phase.FADE_IN;
		timeLeft = TIME_TO_FADE;

		thread = new Thread() {
			@Override
			public void run() {

				try {

					Generator.reset();

					Sample.INSTANCE.load(Assets.SND_OPEN, Assets.SND_UNLOCK,
							Assets.SND_ITEM, Assets.SND_DEWDROP,
							Assets.SND_HIT, Assets.SND_MISS, Assets.SND_STEP,
							Assets.SND_WATER, Assets.SND_DESCEND,
							Assets.SND_EAT, Assets.SND_READ,
							Assets.SND_LULLABY, Assets.SND_DRINK,
							Assets.SND_SHATTER, Assets.SND_ZAP,
							Assets.SND_LIGHTNING, Assets.SND_LEVELUP,
							Assets.SND_DEATH, Assets.SND_CHALLENGE,
							Assets.SND_CURSED, Assets.SND_EVOKE,
							Assets.SND_TRAP, Assets.SND_TOMB, Assets.SND_ALERT,
							Assets.SND_MELD, Assets.SND_BOSS, Assets.SND_BLAST,
							Assets.SND_PLANT, Assets.SND_RAY,
							Assets.SND_BEACON, Assets.SND_TELEPORT,
							Assets.SND_CHARMS, Assets.SND_MASTERY,
							Assets.SND_PUFF, Assets.SND_ROCKS,
							Assets.SND_BURNING, Assets.SND_FALLING,
							Assets.SND_GHOST, Assets.SND_SECRET,
							Assets.SND_BONES, Assets.SND_MIMIC,
							Assets.SND_ROTTEN_DROP, Assets.SND_GOLD);

					if (ModdingMode.mode()) {
						testMode();
					} else {

						switch (mode) {
						case DESCEND:
							descend();
							break;
						case ASCEND:
							ascend();
							break;
						case CONTINUE:
							restore();
							break;
						case RESURRECT:
							resurrect();
							break;
						case RETURN:
							returnTo();
							break;
						case FALL:
							fall();
							break;
						}
					}
					if ((Dungeon.depth % 5) == 0) {
						Sample.INSTANCE.load(Assets.SND_BOSS);
					}

				} catch (FileNotFoundException e) {

					error = ERR_FILE_NOT_FOUND;

				} catch (IOException e) {

					e.printStackTrace();
					error = ERR_GENERIC + "\n" + e.getMessage();

				}

				if (phase == Phase.STATIC && error == null) {
					phase = Phase.FADE_OUT;
					timeLeft = TIME_TO_FADE;
				}
			}
		};
		thread.start();
	}

	@Override
	public void update() {
		super.update();

		float p = timeLeft / TIME_TO_FADE;

		switch (phase) {

		case FADE_IN:
			message.alpha(1 - p);
			if ((timeLeft -= Game.elapsed) <= 0) {
				if (!thread.isAlive() && error == null) {
					phase = Phase.FADE_OUT;
					timeLeft = TIME_TO_FADE;
				} else {
					phase = Phase.STATIC;
				}
			}
			break;

		case FADE_OUT:
			message.alpha(p);

			if (mode == Mode.CONTINUE
					|| (mode == Mode.DESCEND && Dungeon.depth == 1)) {
				Music.INSTANCE.volume(p);
			}
			if ((timeLeft -= Game.elapsed) <= 0) {
				Game.switchScene(GameScene.class);
			}
			break;

		case STATIC:
			if (error != null) {
				add(new WndError(error) {
					public void onBackPressed() {
						super.onBackPressed();
						Game.switchScene(StartScene.class);
					};
				});
				error = null;
			}
			break;
		}
	}

	private void testMode() {
		Actor.fixTime();
		Dungeon.init();

		Level level;
		level = Dungeon.testLevel();

		Dungeon.switchLevel(level, level.entrance);
	}

	private void descend() throws IOException {

		Actor.fixTime();
		
		if (Dungeon.hero == null) {
			Dungeon.init();
			if (noStory) {
				Dungeon.chapters.add(WndStory.ID_SEWERS);
				noStory = false;
			}
		} else {
			Dungeon.saveLevel();
		}
		
		Position next = DungeonGenerator.descend(Dungeon.currentPosition());
		Dungeon.depth = next.levelDepth;
		Level level = Dungeon.loadLevel(next);
		if(level == null) {
			level = Dungeon.newLevel(next);
		}
		
		Dungeon.switchLevel(level, level.entrance);
	}

	private void fall() throws IOException {

		Actor.fixTime();
		Dungeon.saveLevel();
		
		Position next = DungeonGenerator.descend(Dungeon.currentPosition());
		Dungeon.depth = next.levelDepth;
		Level level = Dungeon.loadLevel(next);
		if(level == null) {
			level = Dungeon.newLevel(next);
		}
		
		Dungeon.switchLevel(level,
				fallIntoPit ? level.pitCell() : level.randomRespawnCell());
	}

	private void ascend() throws IOException {
		Actor.fixTime();
		
		Position next = DungeonGenerator.ascend(Dungeon.currentPosition());
		
		Dungeon.saveLevel();
		Dungeon.depth=next.levelDepth;
		
		Level level = Dungeon.loadLevel(next);
		
		if(next.cellId == -2 && level.secondaryExit > 0) {
			Dungeon.switchLevel(level, level.secondaryExit);
		} else {
			Dungeon.switchLevel(level, level.exit);
		}
	}

	private void returnTo() throws IOException {

		Actor.fixTime();

		Dungeon.saveLevel();
		Dungeon.depth = returnTo.levelDepth;
		
		Level level = Dungeon.loadLevel(returnTo);
		Dungeon.switchLevel(level, returnTo.cellId);
	}

	private void problemWithSave() {
		Dungeon.deleteGame(true);
		Game.switchScene(StartScene.class);
		return;
	}

	private void restore() throws IOException{

		Actor.fixTime();

		Dungeon.loadGame();

		if (Dungeon.hero == null) {
			problemWithSave();
			return;
		}

		if (Dungeon.depth == -1) {
			Dungeon.depth = Statistics.deepestFloor;
			
			Dungeon.switchLevel(Dungeon.loadLevel(Dungeon.currentPosition()), -1);
		} else {
			Level level = Dungeon.loadLevel(Dungeon.currentPosition());
			if (level == null) { // save file fucked up :(
				problemWithSave();
				return;
			}
			Dungeon.switchLevel(level, Dungeon.hero.pos);
		}
	}

	private void resurrect() {

		Actor.fixTime();

		if (Dungeon.bossLevel()) {
			Dungeon.hero.resurrect(Dungeon.depth);
			Level level = Dungeon.newLevel(Dungeon.currentPosition());
			Dungeon.switchLevel(level, level.entrance);
		} else {
			Dungeon.hero.resurrect(-1);
			Dungeon.resetLevel();
		}
	}

	@Override
	protected void onBackPressed() {
	}
}
