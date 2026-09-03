# Design Document

CS 2114 Project 1 — Group 87
Working title: a text based pirate adventure game

---

## 1. What are you building?

What are you building? One paragraph a non-CS person could understand. Challenge yourself to describe it without technical terms but while still being complete. Try to think of something with a fun spin on it. For example, don't just make "a flashcard app". Make "a flashcard app that strategically gives you a mix of things you've gotten right before and things you tend to get wrong".

You play a pirate captain working the Channel between England and France. You give
orders by typing them and the game answers in words. You sail from port to port, some
of them English, some French, and some of them hidden coves where only pirates put in,
and you choose where to go next and what to do when you get there. Your ship is the
thing you build up and the thing you can lose: it has a hull that takes damage, a crew
whose numbers rise and fall, armour that wears down, and weapons that set which fights
you can survive. All four carry forward from one port to the next, and nothing is
repaired for free between voyages, so a victory that costs you half your crew and
cracks your hull can leave you in a worse position than the one you were in before you
won it.

---

## 2. What is the MVP?

MVP (Minimum Viable Product) - Considering what core functionality is absolutely necessary to solve the user's primary problem. The smallest version that actually works. Take your description and transform it into a set of features your product should have. Consider what functional requirements (what your program must do) and non-functional requirements (how your program must perform) are a good fit for your project. Ensure your features are specific components or actions (e.g., add card, add deck) rather than very high level descriptions of collections of features (e.g., manages deck). 

The smallest version that works is one voyage loop: read the port, sail somewhere,
survive what you meet, spend what you took, and end the game by retiring rich or by
sinking. Everything below is required for that loop to close.

### Functional requirements — what the program must do

1. **F1.** Running the program starts a voyage: the game prints the opening scene, puts the ship at its home cove, and sets hull, crew, armour, weapon and gold to their starting values.
2. **F2.** Typing `look` reads the current port, so the game prints its name, whether it is English, French or a pirate cove, what can be bought there, and every port reachable from it.
3. **F3.** Typing `status` checks the ship, which prints hull out of maximum, crew count, armour rating, current weapon and gold.
4. **F4.** Typing `sail <port>` moves the ship when that port is reachable from the current one, describing the crossing and rolling for one encounter on the way, and when the port is unreachable or misspelled it names the reachable ports and spends no turn.
5. **F5.** Typing `repair <amount>` at a port buys hull back at the port's price per point, stopping at full hull or at the gold the player holds and reporting what was actually bought.
6. **F6.** Typing `hire <count>` at a port adds that many crew at the port's price each, refusing the portion the player cannot pay for and saying how many came aboard.
7. **F7.** Typing `buy <item>` where the port stocks it deducts the price, replaces the ship's current weapon or armour, and reports the old rating beside the new one.
8. **F8.** Typing `fight` during an encounter resolves the battle in rounds, where damage dealt comes from the weapon and the surviving crew and damage taken is reduced by armour before it lands on hull and crew, until one side is out of the fight.
9. **F9.** Typing `flee` during an encounter breaks it off, applying parting damage to the hull and giving no plunder.
10. **F10.** Winning a fight takes plunder, so the game adds the defeated ship's gold to the player's, names the amount, and returns the player to sailing.
11. **F11.** Letting hull or crew reach zero sinks the ship, and the game prints how it was lost and the final gold total before ending.
12. **F12.** Typing `retire` at the home cove while holding at least the target gold wins the run, printing the ending, the gold total and the number of ports visited.
13. **F13.** Typing `help` prints every command with its arguments.
14. **F14.** Typing `quit` asks for confirmation and exits without saving.

Starting values, prices, the gold target for retirement and the size of the port map
are numbers the group still has to pick. They are placeholders until section 5's
unknowns are closed.

### Non-functional requirements — how the program must perform

1. **N1.** The game runs as a console program on the lab Eclipse setup, reading typed lines and writing text with no window and no library outside the CS 2114 support projects, checked by the grader running the main class from a terminal.
2. **N2.** No input ends the program by exception, because every command either takes effect or prints a message and prompts again, checked by a JUnit test that feeds the parser the cases listed in section 4 and asserts the game still accepts input.
3. **N3.** All randomness comes from one generator the tests can seed, checked by a JUnit test asserting that two runs with the same seed and the same commands produce identical output.
4. **N4.** The submission passes Web-CAT with full method coverage and no style warnings under `vtcseclipsestyle.xml`, checked by the Web-CAT submission report.
5. **N5.** Every response fits one screen, wraps at 80 characters, and names the ship values it changed, checked by reading the transcript of a play session.
6. **N6.** Game state lives in objects rather than static fields so a second game can start inside one run, checked by a JUnit test that plays a game to a loss, constructs a new game, and asserts the new ship is at full hull.

## 3. Stretch goals

things you'd add if there's time, separated from the MVP. These are the things that take you from “Meets” to “Exceeds”.
Note: your TA will evaluate whether these stretch goals are truly non-essential but interesting and innovative features. Do not attempt to subvert this by making overly easy or achievable “exceeds” specifications.

1. **S1.** Two-crown politics, where the player takes privateering contracts, holds a standing with England and with France that rises and falls with every raid, and is outlawed by whichever crown the player angers past its floor.
2. **S2.** Buying an estate with the retirement gold and holding it through yearly tax demands and raids, replacing the ending that prints a score and stops.
3. **S3.** TODO

### S1 in detail: the politics system

The player is Eustace the Monk, who sold his seamanship to whichever crown paid, held
the Channel Islands for the English, then changed sides and carried a French invasion
fleet against them. The game gives the player the same choice repeatedly.

**Setting note.** Eustace was captured and beheaded at Sandwich in 1217. The Hundred
Years' War runs from 1337 to 1453, so the two cannot both be true. The group has to
pick one before submission: keep Eustace and set the game during the French invasion
of England, 1216 to 1217, or keep the Hundred Years' War and play a different captain.
This document assumes the first.

What the system adds on top of the MVP:

1. England and France each hold an opinion of the player, tracked as a number that
   starts neutral. Sinking a French merchant raises English standing and lowers French
   standing, with the French drop larger than the English rise.

2. An English port offers a commission: sink a named French ship, or escort a named
   convoy, for a stated purse. Taking it and finishing it pays the purse and raises
   English standing. Taking it and then selling the target's position to France pays a
   defection bounty instead, and drops English standing hard.

3. Each crown has a floor. When a standing drops below that crown's floor, it declares
   the player an outlaw: its ports refuse repair, refuse to sell, and refuse to hire
   crew, and its warships attack on sight in its half of the Channel. Outlawed by both
   crowns, the player can only refit at pirate coves, which charge more for hull repair
   and crew than any crown port and carry a smaller stock of weapons and armour.

4. Setting the defection bounty is the design problem. France must offer a bounty
   larger than the English purse the player gives up, while the English ports that
   close have to cost enough that doing it every voyage ends the run. The group has to
   set the purse, the bounty, the standing changes and the two floors so that a loyal
   privateer, a French-aligned raider and a captain who switches once are all playable
   to retirement, and a captain who switches every time is not.

5. Standing recovers by paying tribute at that crown's port or by completing a contract
   for it, at a slower rate than it fell.

New commands this adds: `contracts` to list what the current port is offering, `accept
<contract>` to take one, `betray <contract>` to sell it to the other crown, and
`standing` to print where the player sits with each crown.


### S2 in detail: buying an estate and holding it

In the MVP, `retire` ends the game. S2 turns that ending into a second phase the player
has to survive. Once the player's gold passes the price of an estate, buying one sells
the ship, converts the crew into retainers, and starts a yearly clock instead of a
port-to-port one.

Where the estate is bought decides what comes for it. An estate on English soil is
taxed by the English crown at a rate set by English standing, and French raiding
parties come for it, with pirates arriving in a bad year. An estate on French soil is
taxed by the French crown at a rate set by French standing, and the raiders are English
instead. An estate at a pirate cove pays no tax to anyone, and other pirates raid it
more often than they raid anywhere else, with both navies turning up when a crown
decides the cove is worth burning.

Each year the estate draws events against the player's gold and defences. The crown
sends a tax demand: paying it costs gold and holds standing steady, while refusing
keeps the gold, lowers standing with that crown, and raises the chance the sheriff
arrives next year with soldiers to seize the land. A raiding party arrives with a
strength the player has to meet with walls and retainers; beating it costs some
retainers, losing it costs a share of the stored gold, and a second loss in consecutive
years burns the estate to the ground. Between years, gold can go into walls, into
hiring retainers, or into bribing the local officer so tax demands come in lower. Gold
left in the strongbox is what a successful raid takes.

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
| Aidan | aidanmc906678698 | TODO |
