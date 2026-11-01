package data.scripts.factionfilter;

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
 * Flags every market owned by a listed faction as an "invalid mission target".
 * Vanilla DeliveryBarEvent (and other vanilla missions) skip such markets when
 * picking a destination.
 *
 * Re-checks once per in-game day so newly founded / conquered markets are handled,
 * and un-flags markets this mod flagged if they change hands to a normal faction.
 *
 * Written without generics/autoboxing so the game's built-in Janino compiler
 * can compile it from source (no jar needed).
 */
public class FactionFilterScript implements EveryFrameScript {

	public static final String SETTINGS_KEY = "factionMissionFilter_factions";
	public static final String FLAG_KEY = "$factionMissionFilter_flagged";

	private static final Logger log = Global.getLogger(FactionFilterScript.class);

	private final IntervalUtil interval = new IntervalUtil(1f, 1f);
	private final Set excluded = new HashSet();

	public FactionFilterScript() {
		try {
			JSONArray arr = Global.getSettings().getJSONArray(SETTINGS_KEY);
			for (int i = 0; i < arr.length(); i++) {
				excluded.add(arr.getString(i));
			}
		} catch (Exception e) {
			log.error("FactionMissionFilter: could not read '" + SETTINGS_KEY + "' from settings.json", e);
		}
	}

	public void warnUnknownFactions() {
		Iterator it = excluded.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			if (Global.getSector().getFaction(id) == null) {
				log.warn("FactionMissionFilter: no faction with id '" + id + "' exists (check spelling/case)");
			}
		}
	}

	public void apply() {
		int flagged = 0;
		int unflagged = 0;

		List markets = Global.getSector().getEconomy().getMarketsCopy();
		for (int i = 0; i < markets.size(); i++) {
			MarketAPI market = (MarketAPI) markets.get(i);
			MemoryAPI mem = market.getMemoryWithoutUpdate();

			boolean isExcluded = excluded.contains(market.getFactionId());
			boolean flaggedByUs = mem.getBoolean(FLAG_KEY);

			if (isExcluded) {
				if (!market.isInvalidMissionTarget()) {
					market.setInvalidMissionTarget(Boolean.TRUE);
					mem.set(FLAG_KEY, Boolean.TRUE);
					flagged++;
				}
			} else if (flaggedByUs) {
				// Market changed hands to a normal faction; restore it.
				market.setInvalidMissionTarget(Boolean.FALSE);
				mem.unset(FLAG_KEY);
				unflagged++;
			}
		}

		if (flagged > 0 || unflagged > 0) {
			log.info("FactionMissionFilter: flagged " + flagged + " market(s), unflagged " + unflagged);
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
