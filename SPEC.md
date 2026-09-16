# Eustace the Pirate Monk — Specification

CS 2114 Project 1, Deliverable 2 — Group 87 

Numbers marked *default* are balance values Marcos owns and may tune; the code reads them
from constants in one place so changing them never touches logic.

---

## 1. Class design

| Class | Single job |
| --- | --- |
| `Game` | Runs the read–dispatch–print loop, tracks the current `GameMode`, and is the only class that prints to the console. |
| `CommandParser` | Turns one raw line of text into a `Command` (verb + argument). Owns every typing-slip rule from the scope doc. |
| `Command` | Immutable value: a `Verb` and its argument string. Nothing else. |
| `Ship` | The player's numbers: hull, crew, armour, weapon, gold, notoriety, and the rules for changing them safely. |
| `Item` | Immutable weapon or armour for sale: name, type, rating, price. |
| `Port` | One place on the map: name, nation, prices, stock, and the ports reachable from it. |
| `PortMap` | Builds the fixed map, looks ports up by typed name, and rolls whether a crossing meets an enemy. |
| `Encounter` | One enemy ship and the round-by-round fight or flight against the player's `Ship`. |
| `GameMode` (enum) | `PORT`, `ENCOUNTER`, `OVER`. Which verbs are legal right now. |
| `Verb` (enum) | `LOOK, STATUS, SAIL, REPAIR, HIRE, BUY, FIGHT, FLEE, RETIRE, HELP, QUIT, EMPTY, UNKNOWN`. |
| `Nation` (enum) | `ENGLISH, FRENCH, COVE`. |

No class both parses text and changes ship values; no class both holds state and prints it.

## 2. System diagram


```mermaid
flowchart LR
    Console((Console)) -- "raw line" --> Game
    Game -- "line" --> CommandParser
    CommandParser -- "Command (Verb, arg)" --> Game
    Game -- "repair / hire / buy / damage / gold" --> Ship
    Game -- "lookup(name), neighbours" --> PortMap
    PortMap -- "owns 8" --> Port
    Port -- "stocks" --> Item
    Game -- "fightRound / flee" --> Encounter
    Encounter -- "damage" --> Ship
    Random((Random)) -. "one seeded instance" .-> PortMap
    Random -. -> Encounter
    Game -- "text" --> Console
```

Reading it: text enters at the console, becomes a `Command`, and `Game` decides which
object to call based on the verb and the current mode. `Ship` never talks to `Port` or
`Encounter` directly; `Game` mediates everything so there is one place to look when
something goes wrong. One `Random` is created in `Game`'s constructor and handed to the two
classes that roll dice, so a seeded test reproduces a whole run.

## 3. Data & state

**`Game`** — `Ship ship`; `PortMap map`; `Port current`; `Encounter encounter` (null outside
`ENCOUNTER`); `GameMode mode`; `Random rng`; `Scanner in`; `int portsVisited`;
`static final int GOLD_TARGET = 1000` *default*.

**`CommandParser`** — `Map<String, Verb> verbs` (stateless otherwise). A `HashMap` keyed by
lowercase verb word because lookup is by exact string and order never matters.

**`Command`** — `final Verb verb`; `final String argument` (empty string, never null).

**`Ship`** — `int hull, maxHull (100)`; `int crew, maxCrew (40)`; `Item weapon` (starting
rating 10); `Item armour` (starting rating 0); `int gold (50)`; `int notoriety (0)`. All
*default*. Plain fields, not a collection: the ship has a fixed handful of named values and
nothing is ever iterated.

**`Item`** — `final String name`; `final boolean isWeapon`; `final int rating`;
`final int price`.

**`Port`** — `final String name`; `final Nation nation`; `final int repairPrice`;
`final int hirePrice`; `List<Item> stock`; `List<Port> neighbours`. Both are
`ArrayList`s: stock is 2–4 items printed in order by `look`, neighbours is 2–3 ports scanned
once per `sail`. A `Set` would give no useful speed at this size and would lose the printed
order.

**`PortMap`** — `Map<String, Port> ports` keyed by lowercase name; `Port home`;
`Random rng`; `static final double ENCOUNTER_CHANCE = 0.5` *default*. A `HashMap` because
the only operation is "find the port whose name the player typed", and the map is built
once and never changed. Neighbours live on each `Port` (a `List<Port>`) rather than in a
second `Map<String, List<String>>`, so reachability is answered by the object already in
hand with no second lookup. This is the adjacency-list form of a small undirected graph;
an adjacency matrix was rejected because 8×8 booleans is harder to read in the builder
than each port naming its neighbours.

**`Encounter`** — `final String enemyName`; `int enemyHull`; `int enemyCrew`;
`final int enemyWeapon`; `final int plunder`; `final int fleeDamage`; `Random rng`.

## 4. Method signatures

**`Game`**
- `Game(Scanner in, Random rng)` — builds ship, map and parser; mode `PORT` at the home cove.
- `void run()` — prints the opening scene, then loops `readLine → parse → dispatch` until mode is `OVER`.
- `String dispatch(Command c)` — applies one command in the current mode and returns the text to print. Public so tests can drive the game without a `Scanner`.
- `GameMode getMode()`, `Ship getShip()`, `Port getCurrentPort()`, `int getPortsVisited()` — read-only accessors for tests.

**`CommandParser`**
- `Command parse(String line)` — trims, collapses spaces, lowercases, strips quotes and trailing punctuation from the argument; returns `EMPTY`, `UNKNOWN`, or the matched verb with first argument token. Never throws.
- `static int parseCount(String arg)` — returns a non-negative int or throws `NumberFormatException` for "ten", "10.1", "-5", or empty.

**`Ship`**
- `int repair(int points, int pricePerPoint)` — buys up to `points` of hull, capped by missing hull and by gold; returns points actually repaired.
- `int hire(int count, int pricePerHead)` — adds up to `count` crew, capped by `maxCrew` and gold; returns crew that came aboard.
- `boolean buy(Item item)` — if item's rating differs from the current one and gold covers it, deducts and replaces; returns whether the purchase happened.
- `int takeDamage(int raw)` — subtracts armour rating (floor 0), then removes hull and one crew per 10 hull lost; returns hull damage applied.
- `int attackPower()` — `weapon.rating + crew / 2`.
- `boolean isSunk()` — `hull <= 0 || crew <= 0`.
- `void addGold(int amount)`, `void addNotoriety(int amount)`.
- `static int costOf(int count, int price)` — `Math.multiplyExact(count, price)`; throws `ArithmeticException` on overflow.
- `String status()` — one-line summary used by `status` and after every purchase.
- getters for every field.

**`Item`** — `Item(String name, boolean isWeapon, int rating, int price)`; getters; `String toString()` as `"cannon (weapon 15) 120g"`.

**`Port`**
- `Port(String name, Nation nation, int repairPrice, int hirePrice)`.
- `void addNeighbour(Port p)` — one-directional; `PortMap` calls it both ways.
- `void addStock(Item i)`.
- `boolean canReach(Port p)` — `neighbours.contains(p)`.
- `Item findStock(String itemName)` — the stocked item with that lowercase name, or null.
- `String describe()` — name, nation, stock with prices, reachable ports.
- getters.

**`PortMap`**
- `PortMap(Random rng)` — builds the fixed 8-port map.
- `Port lookup(String name)` — port with that lowercase name, or null.
- `Port getHome()`.
- `Encounter rollEncounter(int notoriety)` — with `ENCOUNTER_CHANCE`, returns a new `Encounter` sized to notoriety; otherwise null.

**`Encounter`**
- `Encounter(String enemyName, int hull, int crew, int weapon, int plunder, int fleeDamage, Random rng)` — fully specified, so a test can force any fight.
- `String fightRound(Ship player)` — player hits enemy for `player.attackPower()` ± small roll; if the enemy survives it hits back via `player.takeDamage(enemyWeapon + enemyCrew / 2)`; returns the round narration.
- `boolean isOver()` — enemy sunk or player sunk.
- `boolean playerWon()`.
- `int flee(Ship player)` — applies `fleeDamage` through `takeDamage`; returns hull lost.
- `int getPlunder()`, `String getEnemyName()`.

## 5. Where validation lives

Case numbers are from DESIGN.md section 4. "Re-prompt" means `dispatch` returns a message and no state changes.

| # | Case | Caught in | Response |
| --- | --- | --- | --- |
| 1 | Blank / whitespace line | `CommandParser.parse` → `EMPTY` | `Game.dispatch` returns `""`, re-prompt, no turn |
| 2 | Unknown verb | `parse` → `UNKNOWN` | unknown-command message + "type help" |
| 3 | Missing argument | `Game.dispatch` (argument empty) | that verb's usage line |
| 4 | Extra words | `parse` keeps first argument token only | echo `"sail dover"` then proceed |
| 5 | Case / spacing | `parse` trims, collapses, lowercases | treated as canonical |
| 6 | Quotes / trailing punctuation | `parse` strips `"'.,!?` | treated as canonical |
| 7 | Real but unreachable port | `Game.dispatch(SAIL)`: `lookup` non-null, `canReach` false | lists reachable ports, no turn (distinct message from null lookup) |
| 8 | `sail help` | `parse` reads verb from first word only | port lookup fails → unknown-port message |
| 9 | Negative amount | `parseCount` throws | "amount must be a positive whole number", no turn |
| 10 | Zero | `parseCount` returns 0; `Ship.repair/hire` do nothing | reports 0 bought, no turn |
| 11 | "ten", "10.1" | `parseCount` throws `NumberFormatException` | same message as 9 |
| 12 | Overflow | `Ship.costOf` throws `ArithmeticException`; `dispatch` catches | "you cannot afford that", no change |
| 13 | More than affordable / holdable | `Ship.repair/hire` cap by gold and max | reports what was actually bought |
| 14 | `fight`/`flee` outside `ENCOUNTER` | `dispatch` mode check | "no enemy in sight", no turn |
| 15 | `repair`/`hire`/`buy` in `ENCOUNTER` | `dispatch` mode check | "you are at sea mid-fight" |
| 16 | Item not stocked | `Port.findStock` null | "this port does not sell that" |
| 17 | `retire` wrong place / poor | `dispatch` checks `current == home` and gold | prints gold still needed, continues |
| 18 | Quit confirmation | `dispatch(QUIT)` reads one more line; anything but `y`/`yes` | returns to prompt |
| 19 | Spam `look`/`status` | those branches never call `rollEncounter` | no clock, no roll |
| 20 | `sail <current>` | `dispatch(SAIL)`: `lookup(arg) == current` | refused, no turn |
| 21 | Repeated `flee` | `Encounter.flee` always applies `fleeDamage` | hull drains; `isSunk` ends run |
| 22 | Rebuy same rating | `Ship.buy` compares rating before deducting | refused, gold untouched |
| 23 | 100k-char line | `parse` trims and matches first word | `UNKNOWN`; a 100 KB string is trivially held |
| 24 | Input stream ends | `Game.run` checks `in.hasNextLine()` | prints "input ended" and sets `OVER` |
| 25 | Invisible characters | `parse` uses `strip()` + regex `\s+` collapse | treated as canonical |
| 26 | Accents / emoji | `PortMap.lookup` / `Port.findStock` return null | unknown-port / unknown-item message |
| 27 | Injection | `parse` only compares against fixed `verbs` map; `lookup` against fixed `ports` map | text is data, never executed |

## 6. Test plan

One JUnit class per production class, all built with `new Random(42)` so results repeat.

| Method | Normal case | Bad-input case |
| --- | --- | --- |
| `CommandParser.parse` | `"  SAIL   Dover. "` → `SAIL`, `"dover"` | `"\t\n"` → `EMPTY`; `"saul dover"` → `UNKNOWN` |
| `CommandParser.parseCount` | `"12"` → 12 | `"ten"`, `"10.1"`, `"-3"` each throw `NumberFormatException` |
| `Ship.repair` | hull 60/100, 50 gold, price 2, `repair(20)` → 20, hull 80, gold 10 | hull 90/100, `repair(50)` → 10 repaired, then gold 0 → returns 0 |
| `Ship.hire` | crew 20, price 5, gold 50, `hire(4)` → 4 | crew 39, `hire(10)` → 1 |
| `Ship.buy` | gold 200, buy cannon (15, 120g) → true, weapon 15, gold 80 | buy item with rating equal to current → false, gold unchanged |
| `Ship.takeDamage` | armour 5, `takeDamage(15)` → hull −10, crew −1 | `takeDamage(3)` with armour 5 → 0 damage, nothing changes |
| `Ship.costOf` | `costOf(3, 5)` → 15 | `costOf(Integer.MAX_VALUE, 5)` throws `ArithmeticException` |
| `Ship.isSunk` | fresh ship → false | hull 0 → true; crew 0 → true |
| `Port.canReach` | Dover → Calais true | Dover → Dover false |
| `Port.findStock` | `"cannon"` at a port stocking it → the item | `"cañon"` → null |
| `Port.describe` | contains name, nation and every neighbour name | port with empty stock still lists neighbours |
| `PortMap.lookup` | `"dover"` → Dover | `"atlantis"` → null; `""` → null |
| `PortMap.rollEncounter` | seed 42, notoriety 0, ten rolls → at least one non-null and one null | notoriety −1 clamps to 0, no exception |
| `Encounter.fightRound` | forced enemy hull 1 → one round, `isOver`, `playerWon` | player with crew 0 → player sunk, `playerWon` false |
| `Encounter.flee` | fleeDamage 10, armour 0 → hull −10, encounter over | armour 20 → 0 hull lost, still over |
| `Game.dispatch` | `LOOK` at home → text contains home name, mode unchanged | `FIGHT` in `PORT` → "no enemy", mode unchanged |
| `Game.run` | scripted `"look\nstatus\nquit\nyes\n"` → mode `OVER`, ship untouched | stream with no trailing newline / EOF → ends cleanly, no exception |
| Seeded determinism | two `Game`s with `Random(7)` and the same 20 commands produce identical output | — (non-functional requirement 3) |

## 7. Division of work

| Owner | Classes | Also |
| --- | --- | --- |
| Aidan | `Ship`, `Item`, `CommandParser`, `Command`, `Verb`, `Game` skeleton (loop, mode switch, `LOOK/STATUS/HELP/QUIT/RETIRE` branches) | validation table cases 1–13, 17–18, 22–27 |
| Marcos | `Port`, `PortMap`, `Nation`, `Encounter`, `Game` branches for `SAIL/FIGHT/FLEE/BUY/REPAIR/HIRE` | the 8-port map, all *default* balance numbers, cases 7, 14–16, 19–21 |

Integration: one GitHub repository, `main` protected, each class on its own branch with its
JUnit class before merge. We agree the signatures in section 4 first so each side can stub
the other's classes and work in parallel. Merge at the Tuesday 7 pm meeting; the merged
build must pass the 20-command determinism script and Web-CAT with no style warnings
before it counts as integrated. Aidan owns the final Web-CAT submission; Marcos owns the
play-through transcript for the oral defense.

## 8. Revised scope

| Change | Why | Origin |
| --- | --- | --- |
| "At sea" is no longer a separate mode; a crossing is one atomic `sail` that may contain an `ENCOUNTER` | The MVP has no command that is legal at sea except fight/flee, so a third mode added a state with nothing in it | Group discussion while doing class design |
| Notoriety added as a `Ship` field | The plain-English paragraph promised difficulty scaling; the MVP functional list had silently dropped it | Own reflection comparing sections 1 and 2 |
| `Encounter` gets a fully specified constructor plus `PortMap.rollEncounter` as the only random factory | Resolves unknown "how to force a fight in JUnit" — tests construct the enemy directly | GenAI review of section 5 |
| One `Random` injected through `Game` → `PortMap` / `Encounter` | Resolves unknown "how to pass Random everywhere" without static state | GenAI review |
| Stretch-goal detail sections cut from DESIGN.md; kept as a separate appendix | Scope doc overran the one-page limit | GenAI review |
| Crew bonus text fixed: bigger crew makes a bonus *less* effective per head | Original sentence contradicted the per-head rule | GenAI review |
| Bad-input case 23 justification rewritten (line is read, then rejected) | Original reasoning was wrong about where the memory cost is | GenAI review |
| Provisional balance defaults (hull 100, crew 20/40, gold 50, target 1000, 50% encounter chance) | A spec someone can build from needs numbers; they live in constants so tuning is free | Group discussion |
| Section 5 plan rewritten with owners and dates | Rubric asked how, not just when | GenAI review |
