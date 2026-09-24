# Eustace the Pirate Monk — Specification

CS 2114 Project 1, Deliverable 2 — Group 87 (Marcos Salas, Aidan McIlvenni)

## 1. Class design

1. `Game`: runs the read–dispatch–print loop, checks which commands are allowed where the
   player is, and is the only class that prints. Holds `main`.
2. `CommandParser`: splits one raw line into a `Command`, holds the table of verbs and their
   templates, and turns typed amounts into numbers.
3. `Command`: an immutable value, a verb word and its argument string.
4. `ChannelMap`: builds the fixed 18-stop map and finds a stop by the text typed.
5. `Location`: one stop, with its key, name, kind, encounter chance, description and the
   stops one move away.
6. `Port`: a `Location` where the ship moors, with its prices.
7. `Ship` (abstract): hull, cannons, armour, gold, rum and a `Crew`, plus dealing and taking
   damage.
8. `PlayerShip`: where the player is, notoriety, ports visited, and every purchase.
9. `EnemyShip`: a ship the player meets, built by one of the static factories `merchant`,
   `pirate` and `coastGuard` from that kind's base numbers and the player's notoriety.
10. `Crew`: head count, morale and greed, and the rules that move them.
11. `Encounter`: one meeting with an enemy, from the roll that creates it through the fight
    or the flight to the plunder.

Stop kinds are the `String` constants `Location.PORT`, `SEA_LANE` and `HIGH_SEAS`. The
twelve verbs `look`, `status`, `sail`, `repair`, `hire`, `buy`, `bonus`, `fight`, `flee`,
`retire`, `help` and `quit` are the keys of `CommandParser.templates`.

`Encounter` calls `attackStrength()` and `takeDamage()` on either `Ship`. Morale scales
attack on both sides and a mutiny ends the fight for either. Cannons are levels 1 to 5 and
armour 0 to 5, on one price list; Sark sells rum at half price. `dispatch` reads two facts
before running a command: a fight is on while `encounter != null`, and the player is in
port when their stop's kind is `Location.PORT`.

The map has seven ports (Southampton, Winchelsea, Dover, Barfleur, Dieppe, Boulogne and
Sark, the home port), eight sea lanes each joining two ports, and three high-seas stretches
(`west channel`, `mid channel`, `east channel`). `look` describes the current stop and lists
its neighbours by the key to type; `sail` moves to one of them: from Dover, `sail dover
boulogne` enters the lane, then `sail boulogne` reaches port. `find` looks the typed text up
unchanged, so `sail Dover` and `sail dover-boulogne` find nothing.

## 2. System diagram

![System diagram](docs/system-diagram.png)

Each arrow points the way the labelled thing travels. One turn: `Game` reads a line,
`CommandParser` returns a `Command`, and `Game` checks the verb is allowed where the player
is, runs it and prints the result between two rules of 60 `=`.

## 3. Data & state

Where a field has bounds, such as hull from 0 to `maxHull` or morale from 0 to 100, the
methods that change it clamp to those bounds.

- `Game`: `Scanner in`; final `CommandParser parser`, `ChannelMap map` and `PlayerShip
  player`; `Encounter encounter`, null unless a fight is on; final `Random random`, the
  only one in the program; `boolean running`; `boolean confirmingQuit`, true between `quit`
  and the next line; `static final int GOLD_TARGET = 1000`.
- `CommandParser`: final `HashMap<String, String> templates`, a `LinkedHashMap` filled in
  the constructor with the twelve verbs in `help` order, each mapped to its template, such
  as `sail` to `sail <stop>`. `Command`: final `String verb`, the text before the line's
  first space, or the whole line when it has none; final `String argument`, the text after
  that space, `""` when absent. Neither is trimmed, lower-cased or stripped of punctuation.
- `ChannelMap`: final `HashMap<String, Location> stops`, the 18 stops keyed by their name
  in lower case with a space between words, such as `dover boulogne`; `static final int
  SEA_LANE_CHANCE = 35` and `HIGH_SEAS_CHANCE = 60`. The constructor creates each stop with
  one `new Port(...)` or `new Location(...)`, puts it into `stops` under its key, then makes
  the 25 links with `stops.get(a).connect(stops.get(b))`. Nothing changes the map after the
  constructor returns. `getHome()` is `stops.get("sark")`.
- `Location`: final `String key`, `name`, `kind` and `description`; final `int
  encounterPercent`, 0 for a port, 35 for a sea lane and 60 for the high seas; final
  `Location[] neighbours` of `MAX_NEIGHBOURS = 4` slots and `int neighbourCount`, 2 to 4
  once the map is built. If A lists B then B lists A, and a stop never lists itself or a
  duplicate. No method hands the array out; `connect` is the only writer, and
  `neighbourList()` gives the keys in connection order.
- `Port` adds no fields, only `REPAIR_PRICE = 2`, `HIRE_PRICE = 15`, `RUM_PRICE = 4` and
  `UPGRADE_PRICE = 100` gold per unit, with level n costing `UPGRADE_PRICE × n`.
- `Ship`: final `String name`; `int hull`, 0 to `maxHull`; final `int maxHull`; `int
  cannons`, 1 to 5; `int armour`, 0 to 5; `int gold` and `int rum`, never negative; final
  `Crew crew`. The package-private setters `setHull`, `setCannons` and `setArmour` clamp to
  those bounds and are the only way a subclass changes those three fields.
- `PlayerShip` adds `Location location`, never null; `int notoriety`; `int portsVisited`;
  `static final int MAX_LEVEL = 5`. It starts at the home port with hull 100, crew 20 at
  morale 70, cannons 1, armour 1, gold 200 and rum 30.
- `EnemyShip` adds final `int notorietyGain`, the notoriety a win over it pays, and final
  `String description`, the lookout's view, which `describe()` prints before the numbers.
  Each factory passes its kind's name and base numbers, in the order hull, crew, cannons,
  armour, gold, morale, notoriety gain, to one private constructor: `merchant` "Merchant",
  40, 8, 1, 0, 150, 30, 1; `pirate` "Pirate", 60, 15, 2, 1, 100, 60, 2; `coastGuard` "Coast
  guard", 80, 20, 3, 2, 50, 80, 3. The constructor adds 2 hull per point of the player's
  notoriety, 1 crew per 2 points (capped at 40), 1 cannon and 1 armour per 10 points (each
  capped at 5), and 5 gold per point. Rum starts equal to the crew count and morale is the
  base morale, so at notoriety 10 a merchant has hull 60, crew 13, cannons 2, armour 1 and
  gold 200.
- `Crew`: `int count`, 0 to `MAX_COUNT = 40`; `int morale` and `int greed`, both 0 to
  `MAX_STAT = 100`, where greed starts at 0 and only the player's crew ever gains it. Steps:
  each man lost costs `MORALE_PER_MAN_LOST = 5`, each bottle of rum deficit costs
  `MORALE_PER_DRY_BOTTLE = 5`, a win pays `WIN_MORALE = 10`, a flee costs `FLEE_MORALE =
  15`, and every `GOLD_PER_GREED = 20` gold of plunder adds 1 greed. The crew wants one
  bottle per `MEN_PER_BOTTLE = 10` men. A bonus of `g` gold to `c` men lowers greed by `g ÷
  c` and raises morale by half of `g ÷ c`, both rounded down: 100 gold to 20 men takes 5 off
  greed and adds 2 to morale.
- `Encounter`: final `PlayerShip player`, `EnemyShip enemy` and `Random random`;
  `FLEE_DAMAGE = 10` and `ROLL_RANGE = 6`.

How the numbers move:

1. `moraleFactor()` is 0.5 + morale ÷ 200.0, from 0.5 at morale 0 to 1.0 at morale 100.
   `attackStrength()` multiplies that factor by 5 points per cannon plus 1 point per two
   crew, drops the fraction and never gives less than 1. A fresh player ship has 1 cannon
   and 20 crew at morale 70: (5 + 10) × 0.85 = 12.75, which floors to 12.
2. `takeDamage(raw)` lets raw − 2 × armour through, at least 1 when raw is above 0. The
   hull drops by that number, floored at 0, and the crew loses one man per 5 points of it,
   rounded down: 12 raw against armour 1 gets 10 through, so hull drops 10 and 2 men die.
3. `isDefeated()` is true at hull 0, at crew 0, or when `crew.mutinied()` is true, which is
   morale 0 or greed 100. The player's crew mutinying ends the run; an enemy crew
   mutinying stops its fire, printed as a surrender.
4. A fight round: the player fires `attackStrength() + random.nextInt(ROLL_RANGE)`; if the
   enemy still stands it fires back the same way, and rounds repeat until one side is
   defeated. A win adds the enemy's gold and rum to the player's, adds
   `getNotorietyGain()` to notoriety, and calls `crew.onWin(gold)`.
5. Each sail into a sea stop: the crew wants one bottle per full ten men and never fewer
   than one, so 9 men want 1 and 20 or 21 men want 2. It drinks what the hold has, and the
   deficit, bottles wanted minus bottles drunk, costs `MORALE_PER_DRY_BOTTLE` per bottle:
   20 men with 1 bottle drink it and lose 5 morale, and with an empty hold lose 10. Then
   `roll` draws `nextInt(100)` against the stop's chance, and a second draw picks the
   factory: in a sea lane below 60 is a merchant, else the coast guard; on the high seas
   below 50 is a pirate, else a merchant.
6. A purchase buys the smallest of three numbers: the amount typed, how many units the
   gold covers (gold ÷ price, rounded down), and the room left before hull, crew or a level
   reaches its cap. It then charges that many units times the price.

## 4. Method signatures

"Refuses" means the method returns 0 or false and changes nothing, and `dispatch` turns
that into the message the player sees. IAE means it throws `IllegalArgumentException`.

`Game`: `Game(Scanner in, ChannelMap map, Random random)`, IAE on a null map; `static void
main(String[] args)` builds a game on `System.in`, `new ChannelMap()` and `new Random()`,
or `new Random(seed)` when the first argument is a number, and runs it; `void run()` prints
the opening scene and loops read, parse, dispatch, print while `running` is true, and
prints "Input ended" when the input runs out first; `String dispatch(Command c)` applies
one command and returns the text to print, and when the command sinks the ship, mutinies
the crew, retires the player or confirms `quit`, sets `running` to false and ends its text
with how the run ended, the final gold and the ports visited; `boolean isRunning()`;
`PlayerShip getPlayer()`; `Encounter getEncounter()`. `quit` asks "Quit without saving?
Type yes."; the next line ends the game if it is `yes` and prints "Carrying on." otherwise.

`CommandParser`: `CommandParser()` fills the verb table; `Command parse(String line)`
splits the line at its first space and returns a `Command` holding the two parts
unchanged; `boolean isVerb(String word)`; `String templateOf(String verb)` gives the
template or null; `String allTemplates()` joins every template with newlines for `help`;
`static int parseCount(String text)` returns an int of 0 or more, or throws
`NumberFormatException` for anything else.

`Command`: `Command(String verb, String argument)`, IAE on a null verb, a null argument
becomes `""`; `String getVerb()`; `String getArgument()`; `String getWord(int i)` gives the
i-th space-separated word of the argument or `""`.

`ChannelMap`: `ChannelMap()` adds the 18 stops and links the 25 pairs with
`Location.connect`; `Location find(String name)` gives the stop or null; `Port getHome()`.

`Location`: `Location(String key, String name, String kind, int encounterPercent, String
description)`; `void connect(Location other)` adds each to the other once, IAE on itself
or when either already has 4; `boolean isNextTo(Location other)`; `String neighbourList()`
gives the neighbours' keys joined by ", ", such as "barfleur sark, west channel"; `String
describe()` gives "name, kind. description You can sail to: list."; getters for key, name,
kind, encounterPercent and description. `Port`: `Port(String key, String name, String
description)` passes kind `PORT` and chance 0 upward; `int rumPrice()`, halved at Sark;
`int upgradePrice(int nextLevel)`; `describe()` appends the prices.

`Ship`: `Ship(String name, int maxHull, int cannons, int armour, Crew crew, int gold, int
rum)`, IAE when `maxHull` is 0 or less or `crew` is null, starting at full hull, with gold
and rum floored at 0 and cannons and armour clamped to their levels; `int
attackStrength()`; `int takeDamage(int raw)` returns hull lost, IAE on a negative; `boolean
isDefeated()`; `void addGold(int)`, IAE on a negative; `void spendGold(int)`, IAE on a
negative or beyond what is held; `void addRum(int)`, IAE on a negative; `int takeRum(int)`
returns bottles taken, capped at the hold, IAE on a negative; a getter per field;
`abstract String describe()`.

`PlayerShip`: `PlayerShip(String name, Port home)`; `void moveTo(Location d)`, IAE unless
d is a neighbour, counting a port arrival; `int repair(int points, int pricePerPoint)`,
`int hire(int men, int pricePerMan)` and `int buyRum(int bottles, int pricePerBottle)` each
buy the smallest of the amount asked for, what the gold covers and the room left before
the cap, and return the units bought; `boolean upgradeCannons(int price)` and `boolean
upgradeArmour(int price)` refuse at `MAX_LEVEL` or without the gold; `boolean
payBonus(int gold)` refuses beyond what is held, else spends the gold and calls
`crew.receiveBonus(gold)`; `void addNotoriety(int)`, IAE on a negative; getters;
`describe()` is the `status` report: "Hull 100/100. Crew 20, morale 70, greed 0. Cannons
1, armour 1. Rum 30. Gold 200. Notoriety 0. At Sark."

`EnemyShip`: `static EnemyShip merchant(int notoriety)`, `static EnemyShip pirate(int
notoriety)` and `static EnemyShip coastGuard(int notoriety)` each return a new ship of that
kind, IAE on a negative notoriety; the constructor is private; `int getNotorietyGain()`;
`describe()` is "A Merchant. description Hull 40, 8 men, 1 cannons, armour 0.", what `look`
prints during a meeting.

`Crew`: `Crew(int count, int morale)`, IAE when either is out of range; `int lose(int men)`
and `int hire(int men)` return the number removed or added after capping, IAE on a
negative; `double moraleFactor()`; `int drink(int bottlesAvailable)` returns bottles drunk
and charges morale for the deficit, IAE on a negative; `void onWin(int plunder)`, IAE on a
negative; `void onFlee()`; `void receiveBonus(int gold)`, IAE on a negative, no change with
no crew; `boolean mutinied()`; getters.

`Encounter`: every random decision in a meeting is a draw from the `Random` passed in.
`Encounter(PlayerShip player, EnemyShip enemy, Random random)`, IAE on any null; `static
Encounter roll(Location where, PlayerShip player, Random random)` draws whether an
encounter happens and which factory to call, passes it `player.getNotoriety()` and returns
a new encounter, or null when none happens; `String fight()` resolves the whole battle,
adding a `random` draw to every shot, and returns one "Round n: you deal a, they deal b."
line per round followed by "You are beaten.", "Their crew surrenders." or "They sink."
and the plunder; `String flee()` calls `player.takeDamage(FLEE_DAMAGE)`, then
`crew.onFlee()`, and returns the text to print; `EnemyShip getEnemy()`.

## 5. Where validation lives

`dispatch` checks the player's state first: `fight` and `flee` need `encounter != null`,
`sail` is refused while it is, and `repair`, `hire`, `buy` and `bonus` also need the
player's stop to be a port.

1. Blank line: `parse` gives verb `""` and `dispatch` returns `""`.
2. Unknown verb, such as `sial`: `isVerb` is false, so `dispatch` prints "I don't
   understand 'sial'. Type help."
3. Missing argument: `dispatch` sees an empty argument and prints that verb's template,
   such as `sail <stop>`.
4. Extra words: `find("dover now please")` gives null and `parseCount("5 men")` throws, so
   `sail dover now please` gets "No such place." and `hire 5 men` gets "Type a whole number
   of 0 or more."
5. Case, spacing, quotes, punctuation: `parse` keeps the text as typed, so `SAIL dover`
   fails `isVerb` and gets "I don't understand 'SAIL'. Type help.", `  sail dover` gives
   verb `""` and is treated as a blank line, and `sail Dover.` gets "No such place."
6. A real stop that is not adjacent: `find` succeeds, `isNextTo` is false, so `dispatch`
   prints "Not from here. You can sail to: " and the current stop's `neighbourList()`.
7. `sail help`: the verb is the first word only, `find("help")` gives null, and the reply
   is "No such place."
8. A negative amount, "ten" or "10.1": `parseCount` throws `NumberFormatException` and
   `dispatch` prints "Type a whole number of 0 or more." `buy rum` with no count gets the
   same, since `getWord(1)` is `""`.
9. Zero: `parseCount` gives 0, the purchase returns 0, the reply is "Hired 0 men."
10. Overflow: `PlayerShip.buy` divides gold by price before multiplying, so `hire
    2147483647` by a fresh player buys 200 ÷ 15 = 13 men for 195 gold.
11. A verb in the wrong state: `dispatch` prints "Nothing to fight.", "Not with an enemy
    alongside. Fight or flee." or "You must be in port."
12. An unknown item, such as `buy swords`: `dispatch` prints "You can buy cannons, armour
    or rum."
13. `retire` away from home or short of gold: `dispatch` prints "You can only retire at
    Sark." or "You need n more gold to retire."
14. Repeated `look` or `status`, and `sail <current stop>`: those branches never call
    `roll` or `drink`, and a target equal to the current stop gets "You are already there."
15. An upgrade at the maximum: the upgrade methods check the level before the gold, and
    the reply is "No: already level 5, or not enough gold."
16. A 100,000-character line: `isVerb` looks the first word up in the fixed verb table, and
    `find` and `buy` compare against fixed keys and three item words.
17. Input ends: `run` checks `hasNextLine()` before every read, prints "Input ended" and
    sets `running` to false.
18. Accents and emoji: an accented verb gets "I don't understand", and `find("dövér")`
    gives null.

## 6. Test plan

`Game`:

- `Game(...)`: a new game starts at Sark with no encounter and `isRunning()` true. Bad: a
  null map throws IAE.
- `run`: given a `Scanner` holding `look`, `status`, `quit` and `yes`, the output contains
  "Sark", "Hull 100/100" and "You quit", and `isRunning()` is false. Bad: a blank line,
  then `sial`, then no more lines prints "I don't understand 'sial'" and "Input ended".
- `dispatch`: with `FixedRandom(0)`, a `Random` whose `nextInt` returns the given values in
  turn, `sail barfleur sark` reaches the lane with rum 28 and a merchant alongside; `fight`
  ends with gold 350 and no encounter; `hire 5` gives crew 25 and gold 125; `hire
  2147483647` then hires 8; `buy cannons` at 200 gold gives level 2; `bonus 100` cheers;
  `retire` on 200 gold needs 800 more, and on 1000 at Sark ends the game; `quit` then `no`
  carries on, `quit` then `yes` ends. Bad: every reply in section 5 above, with gold still
  200 afterwards; a fight at hull 1 ends "Your ship is lost"; a sail with greed 100 ends on
  "greed", and with morale 0 on "morale broke".

`CommandParser` and `Command`:

- `parse`: `sail dover boulogne` gives verb "sail" and argument "dover boulogne". Bad:
  `SAIL "Dover".` gives verb "SAIL" and argument `"Dover".` unchanged; a blank line and
  `  sail dover` both give verb `""`.
- `isVerb`, `templateOf`, `allTemplates`: true and `sail <stop>` for `sail`; twelve lines
  containing `bonus <gold>`. Bad: `sial` gives false and null.
- `parseCount`: "25" gives 25, "0" gives 0, "2147483647" gives `Integer.MAX_VALUE`. Bad:
  "-50", "ten", "10.1", "99999999999", "5 men" and "" throw `NumberFormatException`.
- `Command(...)`, `getWord`: `Command("buy", "rum 5")` gives words "rum", "5" and then `""`.
  Bad: a null verb throws IAE.

`ChannelMap`, `Location` and `Port`:

- `ChannelMap()`: 18 stops found by key, home `sark` with neighbours "barfleur sark, west
  channel", the Dover–Boulogne lane at chance 35 linked both ways to both ports, `mid
  channel` at 60, `dieppe` a port, and no empty description.
- `ChannelMap.find`: `dover boulogne` gives the lane. Bad: `dover now please`,
  `dover-boulogne`, `atlantis`, `help`, `dövér`, `Dover` and `""` give null.
- `Location(...)`: a lane built with chance 35 reports its key, kind and 35.
- `Location.connect`: afterwards each stop lists the other; connecting a pair twice lists
  each once. Bad: `a.connect(a)` and a fifth link throw IAE.
- `Location.isNextTo`: true for a connected stop, false for any other.
- `Location.neighbourList` and `describe`: Sark gives "barfleur sark, west channel" and its
  description contains it.
- `Port.rumPrice`: 4 at Dover, 2 at Sark. `Port.upgradePrice`: `upgradePrice(2)` gives 200.
- `Port.describe`: Dover's text contains "Dover", its description and "rum 4".

`Ship`, `PlayerShip` and `EnemyShip`:

- `Ship(...)`: a test subclass with maxHull 40 starts at hull 40, and cannons 0 and armour
  9 clamp to 1 and 5. Bad: maxHull 0 or a null crew throws IAE.
- `Ship.attackStrength`: 12 for a fresh player, 2 with no crew, 7 with the crew at morale 0.
- `Ship.isDefeated`: false for a fresh player, true after `setHull(0)` or a mutiny.
- `Ship.takeDamage`: 12 against a fresh player returns 10, leaving hull 90, crew 18 and
  morale 60; 1 returns 1, 0 returns 0; 500 returns the hull left and empties it. Bad: a
  negative throws IAE.
- `Ship.addGold` and `spendGold`: 50 gives 250, then 150. Bad: `spendGold(500)` throws IAE
  with gold unchanged; a negative throws IAE.
- `Ship.addRum` and `takeRum`: `addRum(10)` gives 40; `takeRum(5)` returns 5; `takeRum(50)`
  returns 35 and empties the hold. Bad: a negative throws IAE.
- `Ship` setters: `setHull(500)` leaves 100, `setCannons(9)` 5, `setArmour(-1)` 0.
- `PlayerShip(...)`: starts at Sark with notoriety 0, hull 100, gold 200 and no ports
  visited.
- `PlayerShip.moveTo`: sailing to the lane and back counts 1 port visited. Bad: Sark to
  Dover throws IAE and leaves the player at Sark.
- `PlayerShip.hire`, `repair`, `buyRum`: `hire(5, 15)` gives crew 25 and gold 125, then
  `hire(Integer.MAX_VALUE, 15)` hires 8 and leaves 5 gold; at hull 90, `repair(50, 2)`
  repairs 10 for 20 gold; `buyRum(10, 4)` buys 10, then `buyRum(100, 4)`
  buys 35 and empties the gold. Bad: `hire(0, 15)` hires 0.
- `PlayerShip.upgradeCannons` and `upgradeArmour`: `upgradeCannons(200)` at level 1 returns
  true, leaving level 2 and gold 0. Bad: at level 5 or with 50 gold either returns false
  with gold unchanged.
- `PlayerShip.payBonus`: `payBonus(100)` after `onWin(600)` gives greed 25, morale 82 and
  gold 100. Bad: `payBonus(500)` returns false with gold unchanged.
- `PlayerShip.addNotoriety` and `describe`: 2 gives 2, and the status text holds every
  field. Bad: a negative throws IAE.
- `EnemyShip.merchant`, `pirate`, `coastGuard`: `merchant(10)` has hull 60, crew 13,
  cannons 2, armour 1, gold 200, rum 13, morale 30 and gain 1; `pirate(0)` hull 60 and gain
  2; `coastGuard(0)` hull 80 and gain 3; `coastGuard(100)` caps at 5 cannons and 40 crew.
  Bad: each throws IAE for a notoriety of -1.
- `EnemyShip.describe`: each kind's text names the kind and contains its description.

`Crew` and `Encounter`:

- `Crew(...)`: `Crew(40, 100)` and `Crew(0, 0)` build. Bad: count 41 or -1, or morale 101 or
  -1, throws IAE.
- `Crew.lose` and `hire`: losing 2 of 20 leaves 18 at morale 60; losing 50 removes 18 and
  leaves morale 0; hiring 5 adds 5 and hiring 100 adds 35. Bad: a negative throws IAE.
- `Crew.moraleFactor`: 0.85 at morale 70, 0.5 at 0, 1.0 at 100.
- `Crew.drink`: 20 men at morale 70 drink 2 of 30 bottles and keep 70; with 1 bottle drink 1
  and drop to 65; with 0 drop a further 10; 21 men want 2 and 9 men want 1. Bad: a
  negative throws IAE.
- `Crew.onWin`, `onFlee`, `mutinied`: `onWin(150)` gives morale 80 and greed 7;
  `onWin(5000)` caps greed at 100 and `mutinied()` is true; `onFlee()` drops 70 to 55;
  morale 0 is a mutiny.
- `Crew.receiveBonus`: 100 gold after `onWin(600)` gives greed 25 and morale 82. Bad: with
  no crew, morale is unchanged.
- `Encounter(...)`: Bad: a null player or enemy throws IAE.
- `Encounter.roll` and `getEnemy`: a sea lane with `FixedRandom(0)` gives a merchant, with
  `FixedRandom(34, 59)` a merchant and `FixedRandom(34, 60)` a coast guard; the high seas
  with `FixedRandom(0)` give a pirate and `FixedRandom(59)` a merchant. Bad: a port, a lane
  with `FixedRandom(35)`, or the high seas with `FixedRandom(99)`, gives null.
- `Encounter.fight`: a fresh player against `merchant(0)` with `FixedRandom(0)` wins in
  round 3 by surrender, leaving hull 97, gold 350, rum 38, morale 80, greed 7 and notoriety
  1; a merchant whose crew stands at hull 0 sinks. Bad: after `setHull(5)`, a fight against
  `pirate(0)` is lost in round 1 with hull 0 and gold still 200.
- `Encounter.flee`: a fresh player is left with hull 92, crew 19, morale 50 and gold 200.
  Bad: after `setHull(5)`, the hull is emptied.

## 7. Division of work

Aidan owns `CommandParser`, `Command`, `Ship`, `PlayerShip`, `EnemyShip`, `Crew` and
`Encounter`; Marcos owns `Game`, `ChannelMap`, `Location` and `Port` and the default
numbers. 
