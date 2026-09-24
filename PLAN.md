# Completion plan — one day to code, test, document and present

Group 87 (Marcos Salas, Aidan McIlvenni). Source of truth for every signature and number:
`SPEC.md` §3, §4 and §6. This file says who writes what, in what order, and the smallest
form each piece takes. Times are elapsed from the start of the working day.

## 0. Unknowns to settle before starting (10 min, Marcos)

| Unknown | Why it changes the plan | Fix |
| --- | --- | --- |
| Presentation format and length | Slides versus a talk, and how many minutes, decide how §7 below is spent. | Read the Project 1 presentation page on Canvas. Absent one, use the eight headings in §7 as slides. |
| Whether Javadoc or Checkstyle is graded | Javadoc on ~100 public members is an hour of typing that adds no behaviour. | Read the implementation rubric. If graded, add one-line `/** */` comments in a single pass at H+6:00, never while writing. |

## 1. Timetable

| Elapsed | Marcos | Aidan |
| --- | --- | --- |
| 0:00–0:20 | §2: delete the placeholder classes, commit the pending changes | Read `SPEC.md` §3–§4 for his seven classes; write `FixedRandom` |
| 0:20–1:00 | §3: stub all 11 production classes, push | §3: six test skeletons with one empty `@Test` per §6 row, pull, push |
| 1:00–2:00 | `Location`, `Port`, their `ChannelMapTest` rows | `Command`, `CommandParser`, `CommandParserTest`; `Crew`, `CrewTest` |
| 2:00–3:00 | `ChannelMap`, remaining `ChannelMapTest` rows, push | `Ship`, its `ShipTest` rows |
| 3:00–4:00 | `Game` pass 1: constructor, `run`, guards, `look`, `status`, `help`, `quit`, `sail`; their `GameTest` rows | `EnemyShip`, `PlayerShip`, their `ShipTest` rows, push |
| 4:00–5:00 | `Game` pass 2: `repair`, `hire`, `buy`, `bonus`, `retire`; rows; start §7 outline while waiting | `Encounter`, `EncounterTest`, push |
| 5:00–6:00 | `Game` pass 3: `fight`, `flee`, endings, `main` seed; seeded playthrough | Run the whole suite in Eclipse, fix reds in his classes, push |
| 6:00–6:45 | §6: sync `SPEC.md`, `DESIGN.md`, `README.md`; commit | Same suite run on the merged tree; commit |
| 6:45–8:30 | §7: slides | §7: `docs/demo.txt` from the chosen seed; rehearse |

Behind at 5:00? Apply the cuts in §8 in order, and record each in `SPEC.md` §8.

## 2. Build path (Marcos, 5 min, done)

`.classpath` now asks for `JavaSE-17`, the bundled JustJ JRE 17.0.9 that Eclipse and the
lab projects already use, and adds the JUnit 5 container, so Java 17 syntax compiles and
the tests resolve. The `/CS2-Support/student.jar` entry stays because the lab projects use
it and it resolves in a workspace that holds `CS2-Support`. In a workspace without that
project Eclipse reports "The project cannot be built until build path errors are resolved";
the fix is to import `CS2-Support` or delete that one `classpathentry`, since the spec uses
nothing from it.

Then:

```
git rm src/Aidan.java src/Marcos.java
git add -A
git commit -m "Fold EnemyType into EnemyShip factories, drop Describable, make map helpers private, remove stale PDF"
git push
```

Git for the rest of the day: each person edits only the files listed as theirs in §1, so
no merge ever conflicts. `git pull --rebase` before every push. Push straight to `main`
after each class and its tests are green.

## 3. Stubs (40 min, both)

Marcos creates one file per class in `src/`, default package, transcribing every
signature in `SPEC.md` §4 with a body of `return 0;`, `return null;`, `return false;` or
nothing, every field declared, every constant set to its §3 default. `Ship` is `abstract`
with `abstract String describe()`. Push when all 11 compile.

Aidan meanwhile writes:

`src/FixedRandom.java`

```java
import java.util.Random;

class FixedRandom extends Random {
    private final int value;
    FixedRandom(int value) { this.value = value; }
    @Override public int nextInt(int bound) { return value % bound; }
}
```

and six test skeletons in `src/`, default package, each `@Test` method empty and named
after its §6 row, `@BeforeEach` building `map = ChannelMap.standard()` and
`player = new PlayerShip("Eustace", map.getHome())` where the class needs them:

| Test class | Covers | Owner | Rows in §6 |
| --- | --- | --- | --- |
| `GameTest` | `Game` | Marcos | 2 bullets, about 9 tests |
| `CommandParserTest` | `CommandParser`, `Command` | Aidan | 1 bullet, about 10 tests including `Command`'s null-verb IAE and `getWord` |
| `ChannelMapTest` | `ChannelMap`, `Location`, `Port` | Marcos | 11 bullets, about 14 tests |
| `ShipTest` | `Ship`, `PlayerShip`, `EnemyShip` | Aidan | 13 bullets, about 22 tests |
| `CrewTest` | `Crew` | Aidan | 4 bullets, about 8 tests |
| `EncounterTest` | `Encounter` | Aidan | 5 bullets, about 8 tests |

Tests use `assertEquals`, `assertTrue`, `assertFalse`, `assertNull` and
`assertThrows(IllegalArgumentException.class, () -> ...)` only. No test prints. `Ship` is
abstract, so `ShipTest` tests the base class through a two-line anonymous subclass with
`maxHull` 40.

## 4. Code-size rules (every class)

1. No Javadoc while writing (see §0).
2. Every getter is one line. Every `describe()` is one `return String.format(...)`.
3. Bounds are applied inline with `Math.max` and `Math.min`; there is no clamp helper.
4. Every IAE is one line, no message: `if (raw < 0) throw new IllegalArgumentException();`. No test reads a message.
5. `Ship`'s setters are package-private (no modifier): shorter than `protected`, and the tests in the same package can call `setHull(5)`.
6. Nothing beyond §4: no `equals`, no `hashCode`, no `toString`, no interface, no getter that neither `Game` nor a test reads.
7. A `switch` case in `dispatch` longer than four lines becomes a private method; shorter ones stay inline.
8. Strings the player sees are literals at the point of use. No message constants, no message class.

## 5. Class by class

Order within each owner's column follows the dependency tiers: a class is written only
after everything it calls exists.

### Tier 0

**`Command`** (Aidan). Two `final String` fields, constructor with one IAE line on a null
verb, two getters, `getWord(int i)` as `String[] w = argument.split(" "); return i < w.length ? w[i] : "";`.
About 20 lines.

**`Crew`** (Aidan). Fields `count`, `morale`, `greed`. Constructor clamps both arguments.

- `lose(men)`: IAE negative; `removed = Math.min(men, count)`; `count -= removed`; morale falls 5 per man, clamped; return `removed`.
- `hire(men)`: IAE negative; `added = Math.min(men, MAX_COUNT - count)`; add; return `added`.
- `moraleFactor()`: `0.5 + morale / 200.0`.
- `drink(available)`: IAE negative; `wanted = (count + 9) / 10`; `drunk = Math.min(wanted, available)`; morale falls `MORALE_PER_DRY_BOTTLE * (wanted - drunk)`; return `drunk`.
- `onWin(plunder)`: morale +10, greed `+ plunder / 20`, both clamped.
- `onFlee()`: morale −15.
- `receiveBonus(gold)`: `if (count == 0) return;` `per = gold / count`; greed −`per`; morale `+ per / 2`.
- `mutinied()`: `morale == 0 || greed == 100`.

**`Location`** (Marcos). Fields per §3, `neighbours = new Location[MAX_NEIGHBOURS]`.

- `connect(other)`: one IAE line for `other == this || neighbourCount == MAX_NEIGHBOURS || other.neighbourCount == MAX_NEIGHBOURS`; `if (isNextTo(other)) return;`; two array writes with `++`.
- `isNextTo(other)`: loop to `neighbourCount`, `==` comparison.
- `neighbourList()`: a `String` built with `+=`, keys joined by `", "`, in connection order. The Sark test expects `"barfleur sark, west channel"`, which the link order in `ChannelMap` guarantees.
- `describe()`: name, kind, `neighbourList()`.

### Tier 1

**`CommandParser`** (Aidan). `HashMap<String, String> templates` filled in the constructor
with the twelve verbs; `parse(line)`: `int i = line.indexOf(' ')`; verb is the whole line
when `i < 0`, else `substring(0, i)` and `substring(i + 1)`; nothing trimmed.
`parseCount(text)`: `int n = Integer.parseInt(text); if (n < 0) throw new NumberFormatException(); return n;`
because `parseInt("-50")` succeeds on its own. `allTemplates()` joins `templates.values()`
with newlines.

**`Port`** (Marcos). `super(key, name, PORT, 0)`. `rumPrice()` is
`nation.equals(PIRATE) ? RUM_PRICE / 2 : RUM_PRICE`. `upgradePrice(n)` is `UPGRADE_PRICE * n`.
`describe()` is `super.describe()` plus the four prices.

**`Ship`** (Aidan). Constructor: IAE on `maxHull <= 0 || crew == null`; `hull = maxHull`.

- `attackStrength()`: `Math.max(1, (int) ((5 * cannons + crew.getCount() / 2) * crew.moraleFactor()))`.
- `takeDamage(raw)`: IAE negative; `through = raw == 0 ? 0 : Math.max(1, raw - 2 * armour)`; `lost = Math.min(hull, through)`; `hull -= lost`; `crew.lose(through / 5)`; return `lost`.
- `isDefeated()`: `hull == 0 || crew.getCount() == 0 || crew.mutinied()`.
- `spendGold(n)`: IAE on `n < 0 || n > gold`. `addGold`, `addRum`: IAE negative. `takeRum(n)`: IAE negative; `taken = Math.min(n, rum)`; return `taken`.
- Package-private `setHull`, `setCannons`, `setArmour`, each bounded with `Math.max`/`Math.min` to the §3 limits.

### Tier 2

**`ChannelMap`** (Marcos). Two tables and two loops in `standard()`. Keys are the text the
player types. Ports carry a nation in the third column; sea stops carry their kind.

```java
static final String[][] STOPS = {
    {"southampton", "Southampton", Port.ENGLISH}, {"winchelsea", "Winchelsea", Port.ENGLISH},
    {"dover", "Dover", Port.ENGLISH}, {"barfleur", "Barfleur", Port.FRENCH},
    {"dieppe", "Dieppe", Port.FRENCH}, {"boulogne", "Boulogne", Port.FRENCH},
    {"sark", "Sark", Port.PIRATE},
    {"southampton winchelsea", "Southampton-Winchelsea lane", Location.SEA_LANE},
    {"winchelsea dover", "Winchelsea-Dover lane", Location.SEA_LANE},
    {"barfleur dieppe", "Barfleur-Dieppe lane", Location.SEA_LANE},
    {"dieppe boulogne", "Dieppe-Boulogne lane", Location.SEA_LANE},
    {"barfleur sark", "Barfleur-Sark run", Location.SEA_LANE},
    {"southampton barfleur", "Southampton-Barfleur crossing", Location.SEA_LANE},
    {"winchelsea dieppe", "Winchelsea-Dieppe crossing", Location.SEA_LANE},
    {"dover boulogne", "Dover-Boulogne crossing", Location.SEA_LANE},
    {"west channel", "West Channel", Location.HIGH_SEAS},
    {"mid channel", "Mid Channel", Location.HIGH_SEAS},
    {"east channel", "East Channel", Location.HIGH_SEAS}};
```

Rows 0–6 become `new Port(key, name, nation)`; the rest `new Location(key, name, kind, chance)`
with 35 for `SEA_LANE` and 60 for `HIGH_SEAS`. The 25 links, lanes first so Sark's
neighbour order matches the test:

```java
static final String[][] LINKS = {
    {"southampton winchelsea", "southampton"}, {"southampton winchelsea", "winchelsea"},
    {"winchelsea dover", "winchelsea"}, {"winchelsea dover", "dover"},
    {"barfleur dieppe", "barfleur"}, {"barfleur dieppe", "dieppe"},
    {"dieppe boulogne", "dieppe"}, {"dieppe boulogne", "boulogne"},
    {"barfleur sark", "barfleur"}, {"barfleur sark", "sark"},
    {"southampton barfleur", "southampton"}, {"southampton barfleur", "barfleur"},
    {"winchelsea dieppe", "winchelsea"}, {"winchelsea dieppe", "dieppe"},
    {"dover boulogne", "dover"}, {"dover boulogne", "boulogne"},
    {"west channel", "southampton"}, {"west channel", "sark"}, {"west channel", "barfleur"},
    {"west channel", "mid channel"},
    {"mid channel", "winchelsea"}, {"mid channel", "dieppe"}, {"mid channel", "east channel"},
    {"east channel", "dover"}, {"east channel", "boulogne"}};
```

Neighbour counts this produces: Sark 2; Southampton, Dover, Boulogne, East Channel 3;
Winchelsea, Barfleur, Dieppe, West Channel, Mid Channel 4; every lane 2. None exceeds
`MAX_NEIGHBOURS`. `home = (Port) stops.get("sark")`. `add` and `connect` are private with
no checks.

**`EnemyShip`** (Aidan). Java forbids a statement before `super(...)`, so:

```java
static EnemyShip merchant(int n)   { return new EnemyShip("Merchant", 40, 8, 1, 0, 150, 30, 1, check(n)); }
static EnemyShip pirate(int n)     { return new EnemyShip("Pirate", 60, 15, 2, 1, 100, 60, 2, check(n)); }
static EnemyShip coastGuard(int n) { return new EnemyShip("Coast guard", 80, 20, 3, 2, 50, 80, 3, check(n)); }
private static int check(int n) { if (n < 0) throw new IllegalArgumentException(); return n; }
```

The private constructor passes `hull + 2 * n`, `Math.min(5, cannons + n / 10)`,
`Math.min(5, armour + n / 10)`, `new Crew(Math.min(40, crew + n / 2), morale)`,
`gold + 5 * n` and rum `Math.min(40, crew + n / 2)` to `super`, then sets `notorietyGain`.
`describe()`: name, hull, crew, cannons.

**`PlayerShip`** (Aidan). `super("Eustace", 100, 1, 1, new Crew(20, 70), 200, 30)`; IAE on a
null home; `location = home`.

- `moveTo(d)`: IAE unless `location.isNextTo(d)`; `location = d`; `if (d.getKind().equals(Location.PORT)) portsVisited++`.
- One private helper for all three purchases:
  `private int buy(int asked, int price, int room) { int n = Math.min(asked, Math.min(getGold() / price, room)); spendGold(n * price); return n; }`
  `n <= gold / price` makes `n * price <= gold`, so no overflow and no failed `spendGold`.
  `repair` passes room `getMaxHull() - getHull()` then `setHull(getHull() + n)`; `hire` passes `Crew.MAX_COUNT - count` then `crew.hire(n)`; `buyRum` passes `Integer.MAX_VALUE - getRum()` then `addRum(n)`.
- `upgradeCannons(price)`: `if (getCannons() == MAX_LEVEL || getGold() < price) return false; spendGold(price); setCannons(getCannons() + 1); return true;`. `upgradeArmour` the same.
- `payBonus(gold)`: `if (gold > getGold()) return false; spendGold(gold); getCrew().receiveBonus(gold); return true;`.
- `addNotoriety`: IAE negative.
- `describe()`: one `String.format` with every field, in the order `status` lists them in `DESIGN.md` functional requirement 3.

### Tier 3

**`Encounter`** (Aidan). Constructor IAE on a null enemy.

- `roll(where, player, random)`: `if (random.nextInt(100) >= where.getEncounterPercent()) return null;` then `int t = random.nextInt(100), n = player.getNotoriety();` and one conditional expression: sea lane `t < 70 ? merchant(n) : coastGuard(n)`, otherwise `t < 50 ? pirate(n) : merchant(n)`. A port's chance is 0, so the first line returns null there.
- `fight()`: a `StringBuilder`; `for (int round = 1; !player.isDefeated() && !enemy.isDefeated(); round++)`: the player fires `attackStrength() + random.nextInt(ROLL_RANGE)`; if the enemy still stands it fires back; append one line per round. After the loop, if the enemy is defeated: move `getGold()` and `getRum()` across, `player.addNotoriety(enemy.getNotorietyGain())`, `player.getCrew().onWin(gold)`, and append "surrenders" when the enemy's hull is above 0 (its crew mutinied) or "sinks" otherwise. Else append the loss line. Return the text.
- `flee()`: `player.takeDamage(FLEE_DAMAGE); player.getCrew().onFlee();` return the text.

Hand-checked against the §3 rules, so when one of these tests fails, suspect the code
first: `takeDamage(12)` on a fresh player returns 10 (hull 90, crew 18, morale 60);
`flee()` leaves hull 92, crew 19, morale 50; the `FixedRandom(0)` fight against
`merchant(0)` ends in round 3 with hull 97, gold 350, rum 38, morale 80, greed 7;
`payBonus(100)` after `onWin(600)` gives greed 25, morale 82; `merchant(10)` has hull 60,
crew 13, cannons 2, armour 1, gold 200; after `setHull(5)` the fight against `pirate(0)` is
lost in round 1.

### Tier 4

**`Game`** (Marcos), in three passes so it compiles and tests against stubs.

Fields per §3 plus `boolean confirmingQuit`. Constructor: IAE on a null map;
`parser = new CommandParser()`; `player = new PlayerShip("Eustace", map.getHome())`.

`run()`:

```java
System.out.println(map.getHome().describe());
while (running && in.hasNextLine()) System.out.println(dispatch(parser.parse(in.nextLine())));
if (running) System.out.println(end("input ended"));
```

`end(String how)`: sets `running = false`, returns `how` plus final gold and ports visited.
Every ending (sunk, mutiny, retired, quit, input ended) calls it.

`dispatch(Command c)`, in this order:

1. `if (confirmingQuit) { confirmingQuit = false; return c.getVerb().equals("yes") ? end("quit") : "carrying on"; }`
2. Blank verb: return `""`. Unknown verb: `"I don't understand '" + verb + "'. Type help."`.
3. Guards: `fight`/`flee` need `encounter != null`; `sail` needs `encounter == null`; `repair`, `hire`, `buy`, `bonus` need `encounter == null` and `player.getLocation().getKind().equals(Location.PORT)`.
4. A verb with a template argument and an empty argument returns the template.
5. `switch (c.getVerb())` with one case per verb.

Pass 1 (H+3:00): `look` (`encounter != null ? encounter.getEnemy().describe() : player.getLocation().describe()`), `status` (`player.describe()`), `help` (`parser.allTemplates()`), `quit` (sets `confirmingQuit`, returns the prompt), `sail`. `sail`: `find` gives null → "no such place"; equal to the current stop → "you are already there"; not adjacent → `neighbourList()`; else `moveTo`, and when the kind is not `PORT`: `player.takeRum(player.getCrew().drink(player.getRum()))`, then `encounter = Encounter.roll(stop, player, random)`. Return the stop's `describe()`, plus the enemy's `describe()` when an encounter began. Then the mutiny check below. With `roll` still stubbed to null, `sail` works before Aidan's tier 3 lands.

Pass 2 (H+4:00): `Port here = (Port) player.getLocation()` in a private `port()`. `repair`: `player.repair(parseCount(arg), Port.REPAIR_PRICE)`; `hire` with `HIRE_PRICE`; `buy` switches on `c.getWord(0)`: `cannons` → `upgradeCannons(here.upgradePrice(getCannons() + 1))`, `armour` likewise, `rum` → `buyRum(parseCount(c.getWord(1)), here.rumPrice())`, anything else → the three item words. `bonus`: `payBonus(parseCount(arg))`. `parseCount` throws `NumberFormatException`; one `catch` around the switch returns "type a whole number of 0 or more". `retire`: at `map.getHome()` with gold ≥ `GOLD_TARGET` → `end("retired")`, else the gold still needed or "you must be at Sark".

Pass 3 (H+5:00): `fight` → `String text = encounter.fight(); encounter = null;`; `flee` → `encounter.flee()` then `encounter = null`. After `fight`, `flee` and `sail`: `if (player.getHull() == 0 || player.getCrew().getCount() == 0) return text + end("sunk"); if (player.getCrew().mutinied()) return text + end("mutiny");`. `main`:

```java
Random r = args.length > 0 ? new Random(Long.parseLong(args[0])) : new Random();
new Game(new Scanner(System.in), ChannelMap.standard(), r).run();
```

The seed argument makes the demo reproducible; record it in `SPEC.md` §4 and §8.

`GameTest` for `run` captures output in three lines:
`ByteArrayOutputStream out = new ByteArrayOutputStream(); System.setOut(new PrintStream(out));`
then `assertTrue(out.toString().contains("Sark"))`. Every other `Game` test calls
`dispatch(new Command(...))` and checks the returned string or the player's fields.

Seeded playthrough (H+5:30): `javac -d bin src/*.java` will not compile the tests without
JUnit on the path, so compile in Eclipse and run `java -cp bin Game 7` from a terminal in
the project folder. Try seeds until the first `sail barfleur sark` produces a merchant;
play `look`, `status`, `sail barfleur sark`, `fight`, `sail barfleur`, `repair 10`,
`hire 5`, `buy rum 10`, `sail barfleur sark`, `sail sark`, `bonus 50`, `retire` (fails, gold
short), `quit`, `yes`. Save the terminal text as `docs/demo.txt`.

## 6. Doc sync (Marcos, 45 min)

1. `SPEC.md` §4: add the seed argument to `main`'s line; correct any signature that changed while coding.
2. `SPEC.md` §8: one row for the seed argument, one per cut from §8 below if any were taken.
3. `SPEC.md` §6: where a test's expected number was wrong and the code right, correct the row; where the code was wrong, the code was fixed instead.
4. `DESIGN.md` §3: delete stretch goal 3, which lists crew morale and greed as a stretch while functional requirements 15–17 make them MVP.
5. `README.md`: two lines. Run: `java -cp bin Game [seed]`. Test: Eclipse, Run As → JUnit Test on `src`.
6. Commit: `Implement every class from SPEC.md with tests; add seed argument and demo transcript`.

## 7. Presentation (both, 1:45)

Format unknown until §0 is done. Absent a rubric, eight slides, each one image or one
list, no paragraph text:

1. The pitch: the `DESIGN.md` §1 paragraph, read aloud, on screen as one sentence.
2. The map: `docs/channel-map.png`; the three stop kinds and what each allows.
3. One turn: `docs/system-diagram.png`, tracing a typed line to `Command` to `dispatch` to the reply.
4. The 11 classes with their one-line jobs from `SPEC.md` §1, and the trim from 13: `EnemyType` folded into three factories, `Describable` removed because no method took it.
5. Combat: `attackStrength`, `takeDamage`, and one worked round against the merchant from the `fight` test (12 raw, armour 0, hull 40 to 28, 2 men lost).
6. Bad input: `hire 2147483647` (buys 13 men for 195 gold), `sail dover` from Sark (neighbour list), input ending (`hasNextLine` in `run`).
7. Testing: `FixedRandom`, the test count from the Eclipse runner, the fight row shown.
8. Since the scope document: `SPEC.md` §8 rows, then the two stretch goals as what comes next.

Demo: run `java -cp bin Game <seed>` live from `docs/demo.txt`, typing the same lines.
Aidan rehearses it twice; if the fight is lost on the chosen seed, pick another.

## 8. Cuts if behind at H+5:00

Apply in order; each removes only the named code and one `SPEC.md` §8 row records it.

1. `bonus` and greed: delete `PlayerShip.payBonus`, `Crew.receiveBonus`, `Crew.getGreed`, the `greed` field, the greed half of `onWin` and `mutinied`, the `bonus` verb and its template, and their test rows. Only `Crew`, `PlayerShip`, `CommandParser` and `Game`'s one case are touched.
2. `quit` confirmation: delete `confirmingQuit`; `quit` calls `end("quit")` at once.
3. `Port.describe` prices and `Location.describe` beyond the name and neighbour list.

Do not cut tests for methods that stay; the §6 rule of one normal and one bad case per public method is what the spec promised.

## 9. Done means

- Eclipse shows no build-path error and the project compiles at Java 17 compliance.
- Every JUnit test in the six classes is green in the Eclipse runner.
- `java -cp bin Game <seed>` plays `docs/demo.txt` from Sark through a won fight to `quit`.
- Every method named in `SPEC.md` §4 exists with that signature, and no public method exists that §4 does not name.
- `SPEC.md` §8 records the seed argument and every cut.
- Slides and `docs/demo.txt` are in the repo, and `main` is pushed.
