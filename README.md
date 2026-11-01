# FactionMissionFilter

A small utility mod for Starsector that stops vanilla missions — most noticeably the
"deliver X to Y" bar event — from constantly sending you to boss factions like
**Adversary** and **Hivers**.

## Why

Boss factions are hostile to almost everyone, so their markets are permanently short on
goods. The vanilla delivery bar event weighs destinations heavily by shortage size, so it
ends up picking those markets almost every time — places you usually can't or don't want
to go.

## How it works

Starsector already has a built-in "invalid mission target" flag on markets, and vanilla
missions skip any market with it set. Mods just rarely set it. This mod:

- sets the flag on every market owned by a faction in its list,
- re-checks once per in-game day, so new or conquered markets are covered,
- removes the flag again if a market it flagged is taken over by a normal faction.

Side effect: other vanilla missions and bar events that check the same flag will also stop
targeting those factions. The factions' own content is not affected.

## Default faction list

| Faction | ID |
|---|---|
| Adversary | `adversary` |
| Hiver | `HIVER` |

Factions you don't have installed are ignored.

## Adding factions

Edit `data/config/settings.json` and add the faction ID to `factionMissionFilter_factions`.
Find IDs in that mod's `data/world/factions/` folder (file name without `.faction`) or with
Console Commands: `list factions`. IDs are case-sensitive.

Other mod authors can add their own boss faction from their mod, without touching this one,
by adding the same key to their own `data/config/settings.json`:

```json
{
	"factionMissionFilter_factions":["my_boss_faction"],
}
```

## Installation

1. Extract into `Starsector/mods/` so you have `mods/FactionMissionFilter/mod_info.json`.
2. Enable it in the launcher.

**Save compatibility:** utility mod; can be added to or removed from an existing save.
Nothing from it is stored in the save. Delivery offers you already have keep their
destination; new ones are filtered.

No dependencies. Compiled from source by the game, no jar needed.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## License

MIT, see [LICENSE](LICENSE). Feel free to fold this into your own mod or modpack.
