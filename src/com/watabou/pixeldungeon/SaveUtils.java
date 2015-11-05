package com.watabou.pixeldungeon;

import java.io.File;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class SaveUtils {
	private static final String RG_GAME_FILE = "game.dat";
	private static final String RG_DEPTH_FILE = "depth%d.dat";

	private static final String RG2_GAME_FILE = "rogue.dat";
	private static final String RG2_DEPTH_FILE = "rogue%d.dat";

	private static final String WR_GAME_FILE = "warrior.dat";
	private static final String WR_DEPTH_FILE = "warrior%d.dat";

	private static final String MG_GAME_FILE = "mage.dat";
	private static final String MG_DEPTH_FILE = "mage%d.dat";

	private static final String RN_GAME_FILE = "ranger.dat";
	private static final String RN_DEPTH_FILE = "ranger%d.dat";

	private static final String EL_GAME_FILE = "elf.dat";
	private static final String EL_DEPTH_FILE = "elf%d.dat";

	static private boolean hasClassTag(HeroClass cl, String fname) {
		switch (cl) {
		default:
		case ROGUE:
			return fname.equals(RG_GAME_FILE) || fname.contains("depth")
					|| fname.contains("rogue");
		case WARRIOR:
			return fname.contains("warrior");
		case HUNTRESS:
			return fname.contains("ranger");
		case MAGE:
			return fname.contains("mage");
		case ELF:
			return fname.contains("elf");
		}
	}

	public static void loadGame(String slot, HeroClass heroClass) {

		deleteGameFile(heroClass);
		deleteLevels(heroClass);

		String[] files = FileSystem.getInteralStorageFile(slot).list();

		for (String file : files) {
			if (file.endsWith(".dat") && hasClassTag(heroClass, file)) {
				FileSystem.copyFile(
						FileSystem.getInteralStorageFile(slot + "/" + file)
								.getAbsolutePath(), FileSystem
								.getInteralStorageFile(file).getAbsolutePath());
			}
		}

		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		Game.switchScene(InterlevelScene.class);
		Dungeon.heroClass = heroClass;
	}

	public static boolean slotUsed(String slot, HeroClass cl) {
		String[] slotFiles = FileSystem.getInteralStorageFile(slot)
				.getAbsoluteFile().list();
		if (slotFiles == null) {
			return false;
		}

		for (String file : slotFiles) {
			if (file.endsWith(gameFile(cl))) {
				return true;
			}
		}

		return false;
	}

	public static void copySaveToSlot(String slot, HeroClass cl) {

		String[] slotFiles = FileSystem.getInteralStorageFile(slot)
				.getAbsoluteFile().list();

		if (slotFiles != null) {
			for (String file : slotFiles) {
				if (file.endsWith(".dat") && hasClassTag(cl, file)
						|| file.endsWith(gameFile(cl))) {
					GLog.i("deleting: %s", file);
					new File(file).delete();
				}
			}
		}

		String[] files = Game.instance().fileList();

		for (String file : files) {
			if (file.endsWith(".dat") && hasClassTag(cl, file)) {
				FileSystem.copyFile(FileSystem.getInteralStorageFile(file)
						.getAbsolutePath(),
						FileSystem.getInteralStorageFile(slot + "/" + file)
								.getAbsolutePath());
			}
		}
	}

	public static void deleteLevels(HeroClass cl) {
		String[] files = Game.instance().fileList();

		for (String file : files) {
			if (file.endsWith(".dat") && hasClassTag(cl, file)) {
				// GLog.i("deleting: %s", file);
				Game.instance().deleteFile(file);
			}
		}
	}

	public static void deleteGameFile(HeroClass cl) {
		Game.instance().deleteFile(gameFile(cl));
	}

	public static String gameFile(HeroClass cl) {

		if (ModdingMode.mode()) {
			return "modding.dat";
		}

		switch (cl) {
		case WARRIOR:
			return WR_GAME_FILE;
		case ROGUE:
			if (FileSystem.getFile(RG2_GAME_FILE).exists()) {
				return RG2_GAME_FILE;
			}
			return RG_GAME_FILE;
		case MAGE:
			return MG_GAME_FILE;
		case HUNTRESS:
			return RN_GAME_FILE;
		case ELF:
			return EL_GAME_FILE;
		default:
			return RG_GAME_FILE;
		}
	}

	public static String saveDepthFile(HeroClass cl, int depth, String levelKind) {
		return Utils.format(levelKind + "_" + _depthFile2(cl), depth);
	}

	public static String loadDepthFile(HeroClass cl, int depth, String levelKind) {

		String fname = Utils.format(levelKind + "_" + _depthFile2(cl), depth);
		// GLog.i("trying: %s", fname);
		if (FileSystem.getFile(fname).exists()) {
			return fname;
		}

		return Utils.format(_depthFile(cl), depth);
	}

	private static String _depthFile2(HeroClass cl) {

		if (ModdingMode.mode()) {
			return "modding%d.dat";
		}

		switch (cl) {
		case WARRIOR:
			return WR_DEPTH_FILE;
		case ROGUE:
			return RG2_DEPTH_FILE;
		case MAGE:
			return MG_DEPTH_FILE;
		case HUNTRESS:
			return RN_DEPTH_FILE;
		case ELF:
			return EL_DEPTH_FILE;
		default:
			return RG_DEPTH_FILE;
		}
	}

	private static String _depthFile(HeroClass cl) {

		if (ModdingMode.mode()) {
			return "modding%d.dat";
		}

		switch (cl) {
		case WARRIOR:
			return WR_DEPTH_FILE;
		case ROGUE:
			return RG_DEPTH_FILE;
		case MAGE:
			return MG_DEPTH_FILE;
		case HUNTRESS:
			return RN_DEPTH_FILE;
		case ELF:
			return EL_DEPTH_FILE;
		default:
			return RG_DEPTH_FILE;
		}
	}
}
