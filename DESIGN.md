# Design Document

CS 2114 Project 1 — Group 87
Working title: a text based pirate adventure game

> Sections below are placeholders. Each one records the assignment's question and
> waits for the group's answer. Replace every `TODO` before submitting; nothing here
> is a guess at what the group decided.

---

## 1. What are you building?

*One paragraph a non-CS person could understand, complete but free of technical terms,
with a specific spin rather than a generic category. The assignment's example: not "a
flashcard app", but "a flashcard app that strategically gives you a mix of things you've
gotten right before and things you tend to get wrong".*

You play a pirate captain working the Channel between England and France. You give
orders by typing them and the game answers in words. You sail from port to port, some
of them English, some French, and some of them hidden coves where only pirates put in,
and you choose where to go next and what to do when you get there. Your ship is the
thing you build up and the thing you can lose: it has a hull that takes damage, a crew
whose numbers rise and fall, armour that wears down, and weapons that set which fights
you can survive. All four carry forward from one port to the next, so the ship you limp
into harbour with is the ship you leave in. Nothing is repaired for free between
voyages, so a victory that costs you half your crew and cracks your hull can leave you
in a worse position than the one you were in before you won it.

---

## 2. What is the MVP?

*The smallest version that actually works and solves the user's primary problem.
Features must be specific components or actions ("add card", "add deck"), not
collections of behaviour ("manages decks").*

The smallest version that works is one voyage loop: read the port, sail somewhere,
survive what you meet, spend what you took, and end the game by retiring rich or by
sinking. Everything below is required for that loop to close.

### Functional requirements — what the program must do

| # | Feature | What the player does | What the program does in response |
|---|---------|----------------------|-----------------------------------|
| F1 | Start a voyage | Runs the program | Prints the opening scene, puts the ship at its home cove, and sets hull, crew, armour, weapon and gold to their starting values |
| F2 | Read the current port | Types `look` | Prints the port's name, whether it is English, French or a pirate cove, what can be bought there, and every port reachable from it |
| F3 | Check the ship | Types `status` | Prints hull out of maximum, crew count, armour rating, current weapon and gold |
| F4 | Sail to another port | Types `sail <port>` | Moves the ship when that port is reachable from the current one, describes the crossing, and rolls for one encounter on the way; when the port is unreachable or misspelled, says so and lists the reachable ports without spending a turn |
| F5 | Repair the hull | Types `repair <amount>` at any port | Restores that much hull at the port's price per point, stopping at full hull or at the gold the player holds, and reports what was actually bought |
| F6 | Hire crew | Types `hire <count>` at any port | Adds that many crew at the port's price each, refusing the portion the player cannot pay for and saying how many were hired |
| F7 | Buy a weapon or armour | Types `buy <item>` where the port sells it | Deducts the price, replaces the ship's current weapon or armour, and reports the old rating and the new one |
| F8 | Fight an encounter | Types `fight` when an enemy is present | Resolves the fight in rounds: damage dealt comes from the weapon and the surviving crew, damage taken is reduced by armour and lands on hull and crew, until one side is out of the fight |
| F9 | Break off an encounter | Types `flee` when an enemy is present | Ends the encounter, applies parting damage to the hull, and gives no plunder |
| F10 | Take plunder | Wins a fight | Adds the defeated ship's gold to the player's, names the amount, and returns the player to sailing |
| F11 | Sink | Lets hull reach zero or crew reach zero | Prints how the ship was lost, the final gold total, and ends the game |
| F12 | Retire | Types `retire` at the home cove while holding at least the target gold | Prints the ending, the gold total and the number of ports visited, and ends the game |
| F13 | List the commands | Types `help` | Prints every command with its arguments |
| F14 | Leave the game | Types `quit` | Asks for confirmation and exits without saving |

Starting values, prices, the gold target for retirement and the size of the port map
are numbers the group still has to pick. They are placeholders until section 5's
unknowns are closed.

### Non-functional requirements — how the program must perform

| # | Requirement | How it is measured |
|---|-------------|--------------------|
| N1 | Runs as a console program on the lab Eclipse setup, reading typed lines and writing text, with no window and no library outside the CS 2114 support projects | The grader runs the main class and plays it from the terminal |
| N2 | No input ends the program by exception; every command either takes effect or prints a message and prompts again | A JUnit test feeds the parser the cases listed in section 4 and asserts the game is still accepting input afterwards |
| N3 | All randomness comes from one generator the tests can seed | Two runs with the same seed and the same commands produce identical output, asserted in a JUnit test |
| N4 | Passes Web-CAT with full method coverage and no style warnings under `vtcseclipsestyle.xml` | The Web-CAT submission report |
| N5 | Every response fits one screen, wrapped at 80 characters, and names the ship values it changed | Read from the transcript of a play session |
| N6 | Game state lives in objects rather than static fields, so a second game can be started inside one run | A JUnit test plays a game to a loss, constructs a new game, and asserts the new ship is at full hull |

## 3. Stretch goals

*Features added only if there is time, kept out of the MVP. The TA judges whether each
is genuinely non-essential and genuinely interesting, so a padded or trivial entry
counts against the group.*

| # | Stretch goal | Why it is non-essential | Why it is interesting |
|---|--------------|-------------------------|-----------------------|
| S1 | Two-crown politics: privateering contracts, standing with England and with France, and outlawry when either standing falls far enough | The voyage loop in section 2 closes without it. Ports can stay neutral shops that sell repairs and crew to anyone, and the player can still sail, fight, plunder and retire | Every port stops being a shop and starts being a position. The player has to price a betrayal against the ports it will close, and we have to tune the French payout so that turning on England is worth taking at least once and ruinous as a habit |
| S2 | Buying an estate and holding it against tax collectors and raiders, in place of the ending that just prints a score | The MVP already ends cleanly at `retire`, and the voyage loop never needs a second phase to be playable | Gold stops being a score and becomes something the player has to defend. Where the estate is bought picks which enemy comes for it, so the choice pays off against the standings from S1 and against how much the player is willing to leave in the strongbox |
| S3 | TODO | TODO | TODO |

### S1 in detail: the politics system

The player is Eustace the Monk, who sold his seamanship to whichever crown paid, held
the Channel Islands for the English, then changed sides and carried a French invasion
fleet against them. The game gives the player the same choice repeatedly and makes it
cost something.

**Setting note.** Eustace was captured and beheaded at Sandwich in 1217. The Hundred
Years' War runs from 1337 to 1453, so the two cannot both be true. The group has to
pick one before submission: keep Eustace and set the game in the Anglo-French war of
1215 to 1217, or keep the Hundred Years' War and play a different captain. This
document assumes the first.

What the system adds on top of the MVP:

1. **Two standings.** England and France each hold an opinion of the player, tracked
   as a number that starts neutral. Sinking a French merchant raises English standing
   and lowers French standing by a larger amount than it raises the other.

2. **Contracts.** An English port offers a commission: sink a named French ship, or
   escort a named convoy, for a stated purse. Taking it and finishing it pays the purse
   and raises English standing. Taking it and then selling the target's position to
   France pays a defection bounty instead, and drops English standing hard.

3. **The defection bounty is the design problem.** France must offer enough that
   betraying England is a live option rather than a trap, while the English ports the
   player loses have to hurt enough that doing it every voyage ends the run. The group
   has to set the purse, the bounty, the standing changes and the outlawry floor so
   that a loyal privateer, a French-aligned raider and a captain who switches once are
   all playable to retirement, and a captain who switches every time is not.

4. **Outlawry.** When a standing drops below the floor, that crown declares the player
   an outlaw: its ports refuse repair, refuse to sell, and refuse to hire crew, and its
   warships attack on sight in its half of the Channel. Outlawed by both crowns, the
   player can only refit at pirate coves, which charge more and stock less, so a hull
   at half strength becomes expensive to fix and a lost crew becomes hard to replace.

5. **Coming back in.** Standing recovers by paying tribute at that crown's port or by
   completing a contract for it, at a slower rate than it fell, so a betrayal is
   recoverable and never free.

New commands this adds: `contracts` to list what the current port is offering, `accept
<contract>` to take one, `betray <contract>` to sell it to the other crown, and
`standing` to print where the player sits with each crown.


### S2 in detail: buying an estate and holding it

In the MVP, `retire` ends the game. S2 turns that ending into a second phase the player
has to survive. Once the player's gold passes the price of an estate, buying one sells
the ship, converts the crew into retainers, and starts a yearly clock instead of a
port-to-port one.

Where the estate is bought decides what comes for it:

| Location | Who taxes it | Who raids it |
|----------|--------------|--------------|
| English soil | The English crown, at a rate set by English standing | French raiding parties, and pirates in a bad year |
| French soil | The French crown, at a rate set by French standing | English raiding parties, and pirates in a bad year |
| A pirate cove | Nobody, no tax at all | Other pirates, more often than anywhere else, and both navies when a crown decides the cove is worth burning |

Each year the estate draws events against the player's gold and defences:

1. **Tax.** The crown sends a demand. Paying it costs gold and holds standing steady.
   Refusing keeps the gold, lowers standing with that crown, and raises the chance the
   sheriff arrives next year with soldiers to seize the land.

2. **Raids.** A raiding party arrives with a strength the player has to meet with walls
   and retainers. Beating it costs some retainers. Losing it costs a share of the
   stored gold, and a second loss in consecutive years burns the estate to the ground.

3. **Spending between years.** Gold can go into walls, into hiring retainers, or into
   bribing the local officer so tax demands come in lower. Gold left in the strongbox
   is what a successful raid takes, so hoarding is itself a risk.

The run ends when the player holds the estate through a set number of years, which
prints the final score, or when the estate is lost. Losing it with the ship already
sold ends the game as a pauper; the group can decide whether a lost estate instead
puts the player back to sea with a small boat and nothing else.

Without S1 in place, tax rates are flat numbers and no crown confiscates the land, so
S2 can be built and graded on its own.

New commands this adds: `estates` to list what is for sale and at what price, `buy
estate <place>` to retire onto it, `fortify <walls or retainers>` to spend on defence,
`pay tax` and `refuse tax` when the demand arrives, and `holdings` to print the
estate's walls, retainers, stored gold and years held.

---

## 4. What bad input must it survive?

*Named cases, including adversarial ones: what a careless player types by accident and
what a hostile player types on purpose.*

| # | Input | Where it arrives | Required response |
|---|-------|------------------|-------------------|
| B1 | TODO | TODO | TODO |

---

## 5. What don't you know how to do yet?

*Honest list of the gaps, plus the group's plan for closing them.*

### Open unknowns

| # | Unknown | Who is blocked by it | How we expect to resolve it |
|---|---------|----------------------|-----------------------------|
| U1 | TODO | TODO | TODO |

### Plan for addressing them

TODO — when the group meets, who brings what, which unknowns go to the TA or to office
hours, and which the group researches first.

---

## Team

| Name | GitHub | Responsibilities |
|------|--------|------------------|
| Marcos Salas | DRMarcosVT | TODO |
| TODO | aidanmc906678698 | TODO |
