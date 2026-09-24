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
     * crew constructor
     * @param count number in crew
     * @param morale morale of crew
     */
    public Crew(int count, int morale) {
        //both crew count and morale are non negative, below max
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
    /**
     * lose men in battle
     * @param men men lost
     * @return the number lost
     */
    public int lose(int men) {
        //cannot lose negative men
        if (men < 0) {
            throw new IllegalArgumentException();
        }
        //cannot lose into negativity
        int removed = Math.min(men, count);
        count -= removed;
        //morale is non negative
        morale = Math.max(0, morale - removed * MORALE_PER_MAN_LOST);
        return removed;
    }

    /**
     * @param men how many to add
     * @return men actually added 
     */
    public int hire(int men) {
        //non negativity
        if (men < 0) {
            throw new IllegalArgumentException();
        }
        //cannot hire too many men
        int added = Math.min(men, MAX_COUNT - count);
        count += added;
        return added;
    }

    /**
     * morale factor scales rng in combat
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
     * 
     *
     * @param bottlesAvailable bottles in the hold
     * @return bottles drunk
     * @throws IllegalArgumentException on a negative
     */
    public int drink(int bottlesAvailable) {
        if (bottlesAvailable < 0) {
            throw new IllegalArgumentException();
        }
        int wanted = Math.max(1, count / MEN_PER_BOTTLE);
        int drunk = Math.min(wanted, bottlesAvailable);
        int deficit = wanted - drunk;
        morale = Math.max(0, morale - deficit * MORALE_PER_DRY_BOTTLE);
        return drunk;
    }

    /**
     * 
     * increment moral by WIN_MORALE 
     * increment greed by a linear equation with plunder as the variable and
     * 1/gold_per_greed as the coefficient
     * @param plunder gold taken
     */
    public void onWin(int plunder) {
        if (plunder < 0) {
            throw new IllegalArgumentException();
        }
        morale = Math.min(MAX_STAT, morale + WIN_MORALE);
        greed = Math.min(MAX_STAT, greed + plunder / GOLD_PER_GREED);
    }

    /**
     * fleeing drops moraly
     */
    public void onFlee() {
        morale = Math.max(0, morale - FLEE_MORALE);
    }

    /**
     * paying the bonus drops greed by the gold/head
     *
     * @param gold the bonus
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
     * @return true at morale 0 or greed 100, game over
     */
    public boolean mutinied() {
        return morale == 0 || greed == MAX_STAT;
    }

    /**
     * crew count, morale, and greed getters
     */
    public int getCount() { return count; }
    public int getMorale() { return morale;}
    public int getGreed() { return greed; }
}