package data.scripts.factionblacklist;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class FactionBlacklistModPlugin extends BaseModPlugin {

	private FactionBlacklistScript script;

	public void onGameLoad(boolean newGame) {
		script = new FactionBlacklistScript();
		script.warnUnknownFactions();
		script.apply();
		// Transient: the script itself is never written into the save.
		Global.getSector().addTransientScript(script);
	}

	// Clear every flag this mod set before the game writes the save, and put them
	// back right after. The save file therefore never contains anything from this
	// mod, so removing it leaves the save exactly as if it had never been installed.
	public void beforeGameSave() {
		if (script != null) script.clearAll();
	}

	public void afterGameSave() {
		if (script != null) script.apply();
	}

	// If saving fails, still restore the flags for the current session.
	public void onGameSaveFailed() {
		if (script != null) script.apply();
	}
}
