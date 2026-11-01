# Changelog

## 1.1.0
- Renamed from FactionMissionFilter to **Faction Mission Blacklist**
  (mod ID `faction_mission_blacklist`, settings key `factionMissionBlacklist_factions`).
- Nothing is stored in the save anymore: flags are cleared right before every save and
  restored right after, so the mod can be removed at any time with no leftovers.
- Flags left behind by 1.0.0 are cleaned up automatically on the next save.

## 1.0.0
- Initial release as FactionMissionFilter.
- Markets of listed factions (default: Adversary, Hiver) are flagged as invalid mission
  targets, so vanilla delivery bar events and other vanilla missions skip them.
