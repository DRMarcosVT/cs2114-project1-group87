import java.util.Random;

class FixedRandom extends Random {
    private final int[] values;
    private int next;

    FixedRandom(int... values) { this.values = values; }

    @Override
    public int nextInt(int bound) { return values[next++ % values.length] % bound; }
}
