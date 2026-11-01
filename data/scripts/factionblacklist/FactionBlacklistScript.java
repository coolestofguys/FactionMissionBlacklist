package data.scripts.factionblacklist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.IntervalUtil;

/**
 * Flags every market owned by a blacklisted faction as an "invalid mission target".
 * Vanilla DeliveryBarEvent (and other vanilla missions) skip such markets when
 * picking a destination.
 *
 * Re-checks once per in-game day so newly founded / conquered markets are handled,
 * and un-flags markets this mod flagged if they change hands to a normal faction.
 * The mod plugin calls clearAll() before every save and apply() after, so nothing
 * from this mod is ever stored in a save.
 *
 * Written without generics/autoboxing so the game's built-in Janino compiler
 * can compile it from source (no jar needed).
 */
public class FactionBlacklistScript implements EveryFrameScript {

	public static final String SETTINGS_KEY = "factionMissionBlacklist_factions";
	public static final String FLAG_KEY = "$factionMissionBlacklist_flagged";

	// Keys used by pre-release / 1.0.0 builds (BossMissionFilter, FactionMissionFilter).
	// Markets carrying them are cleaned up so old saves end up fully clean too.
	private static final String[] LEGACY_FLAG_KEYS = {
		"$factionMissionFilter_flagged",
		"$bossMissionFilter_flagged"
	};

	private static final Logger log = Global.getLogger(FactionBlacklistScript.class);

	private final IntervalUtil interval = new IntervalUtil(1f, 1f);
	private final Set excluded = new HashSet();

	public FactionBlacklistScript() {
		try {
			JSONArray arr = Global.getSettings().getJSONArray(SETTINGS_KEY);
			for (int i = 0; i < arr.length(); i++) {
				excluded.add(arr.getString(i));
			}
		} catch (Exception e) {
			log.error("FactionMissionBlacklist: could not read '" + SETTINGS_KEY + "' from settings.json", e);
		}
	}

	public void warnUnknownFactions() {
		Iterator it = excluded.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			if (Global.getSector().getFaction(id) == null) {
				log.warn("FactionMissionBlacklist: no faction with id '" + id + "' exists (check spelling/case)");
			}
		}
	}

	private boolean isFlaggedByUs(MemoryAPI mem) {
		if (mem.getBoolean(FLAG_KEY)) return true;
		for (int i = 0; i < LEGACY_FLAG_KEYS.length; i++) {
			if (mem.getBoolean(LEGACY_FLAG_KEYS[i])) return true;
		}
		return false;
	}

	private void unflag(MarketAPI market, MemoryAPI mem) {
		market.setInvalidMissionTarget(Boolean.FALSE);
		mem.unset(FLAG_KEY);
		for (int i = 0; i < LEGACY_FLAG_KEYS.length; i++) {
			mem.unset(LEGACY_FLAG_KEYS[i]);
		}
	}

	/** Flag blacklisted markets; unflag ones we flagged that are no longer blacklisted. */
	public void apply() {
		int flagged = 0;
		int unflagged = 0;

		List markets = Global.getSector().getEconomy().getMarketsCopy();
		for (int i = 0; i < markets.size(); i++) {
			MarketAPI market = (MarketAPI) markets.get(i);
			MemoryAPI mem = market.getMemoryWithoutUpdate();

			boolean isExcluded = excluded.contains(market.getFactionId());
			boolean ours = isFlaggedByUs(mem);

			if (isExcluded) {
				if (!market.isInvalidMissionTarget()) {
					market.setInvalidMissionTarget(Boolean.TRUE);
					mem.set(FLAG_KEY, Boolean.TRUE);
					flagged++;
				}
			} else if (ours) {
				// Market changed hands to a normal faction; restore it.
				unflag(market, mem);
				unflagged++;
			}
		}

		if (flagged > 0 || unflagged > 0) {
			log.info("FactionMissionBlacklist: flagged " + flagged + " market(s), unflagged " + unflagged);
		}
	}

	/** Remove every flag this mod has set (called right before saving). */
	public void clearAll() {
		List markets = Global.getSector().getEconomy().getMarketsCopy();
		for (int i = 0; i < markets.size(); i++) {
			MarketAPI market = (MarketAPI) markets.get(i);
			MemoryAPI mem = market.getMemoryWithoutUpdate();
			if (isFlaggedByUs(mem)) {
				unflag(market, mem);
			}
		}
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		interval.advance(days);
		if (interval.intervalElapsed()) {
			apply();
		}
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}
