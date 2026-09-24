/**
 * Head count, morale and greed, and the rules that move them.
 * Every change clamps: count 0..MAX_COUNT, morale and greed 0..100.
 */
public class Crew {
    public static final int MAX_COUNT = 40;
    public static final int MAX_STAT = 100;
    public static final int MORALE_PER_DRY_BOTTLE = 5;
    public static final int MORALE_PER_MAN_LOST = 5;
    public static final int WIN_MORALE = 10;
    public static final int FLEE_MORALE = 15;
    public static final int GOLD_PER_GREED = 20;
    public static final int MEN_PER_BOTTLE = 10;

    private int count;
    private int morale;
    private int greed;

    /**
     * Greed starts at 0.
     *
     * @param count  head count, 0 to MAX_COUNT
     * @param morale starting morale, 0 to MAX_STAT
     * @throws IllegalArgumentException if either is out of range
     */
    public Crew(int count, int morale) {
        if (count < 0 || count > MAX_COUNT || morale < 0 || morale > MAX_STAT) {
            throw new IllegalArgumentException();
        }
        this.count = count;
        this.morale = morale;
        this.greed = 0;
    }

    /**
     * Removes men (not below 0) and takes MORALE_PER_MAN_LOST
     * morale per man actually removed.
     *
     * @param men how many to remove
     * @return men actually removed
     * @throws IllegalArgumentException on a negative
     */
    public int lose(int men) {
        if (men < 0) {
            throw new IllegalArgumentException();
        }
        int removed = Math.min(men, count);
        count -= removed;
        morale = Math.max(0, morale - removed * MORALE_PER_MAN_LOST);
        return removed;
    }

    /**
     * @param men how many to add
     * @return men actually added (not past MAX_COUNT)
     * @throws IllegalArgumentException on a negative
     */
    public int hire(int men) {
        if (men < 0) {
            throw new IllegalArgumentException();
        }
        int added = Math.min(men, MAX_COUNT - count);
        count += added;
        return added;
    }

    /**
     * @return 0.5 + morale / 200.0, from 0.5 at morale 0 to 1.0 at 100
     */
    public double moraleFactor() {
        return 0.5 + morale / 200.0;
    }

    /**
     * Wants one bottle per ten men, rounding up (20 -> 2, 21 -> 3).
     * Drinks what is available; each missing bottle costs
     * MORALE_PER_DRY_BOTTLE.
     *
     * @param bottlesAvailable bottles in the hold
     * @return bottles drunk
     * @throws IllegalArgumentException on a negative
     */
    public int drink(int bottlesAvailable) {
        if (bottlesAvailable < 0) {
            throw new IllegalArgumentException();
        }
        int wanted = (count + MEN_PER_BOTTLE - 1) / MEN_PER_BOTTLE;
        int drunk = Math.min(wanted, bottlesAvailable);
        int deficit = wanted - drunk;
        morale = Math.max(0, morale - deficit * MORALE_PER_DRY_BOTTLE);
        return drunk;
    }

    /**
     * +WIN_MORALE morale, +1 greed per GOLD_PER_GREED gold of plunder.
     *
     * @param plunder gold taken
     * @throws IllegalArgumentException on a negative
     */
    public void onWin(int plunder) {
        if (plunder < 0) {
            throw new IllegalArgumentException();
        }
        morale = Math.min(MAX_STAT, morale + WIN_MORALE);
        greed = Math.min(MAX_STAT, greed + plunder / GOLD_PER_GREED);
    }

    /** Takes FLEE_MORALE morale. */
    public void onFlee() {
        morale = Math.max(0, morale - FLEE_MORALE);
    }

    /**
     * greed -= gold / count, morale += (gold / count) / 2,
     * both rounded down. 100 gold to 20 men: greed -5, morale +2.
     *
     * @param gold the bonus
     * @throws IllegalArgumentException on a negative
     */
    public void receiveBonus(int gold) {
        if (gold < 0) {
            throw new IllegalArgumentException();
        }
        if (count == 0) {
            return;
        }
        int perMan = gold / count;
        greed = Math.max(0, greed - perMan);
        morale = Math.min(MAX_STAT, morale + perMan / 2);
    }

    /**
     * @return true at morale 0 or greed 100
     */
    public boolean mutinied() {
        return morale == 0 || greed == MAX_STAT;
    }

    public int getCount() {
        return count;
    }

    public int getMorale() {
        return morale;
    }

    public int getGreed() {
        return greed;
    }
}