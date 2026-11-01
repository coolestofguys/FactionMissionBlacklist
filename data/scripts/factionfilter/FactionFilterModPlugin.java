package data.scripts.factionfilter;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class FactionFilterModPlugin extends BaseModPlugin {

	public void onGameLoad(boolean newGame) {
		FactionFilterScript script = new FactionFilterScript();
		script.warnUnknownFactions();
		script.apply();
		// Transient: nothing from this mod is written into the save,
		// so it can be removed later without breaking anything.
		Global.getSector().addTransientScript(script);
	}
}
