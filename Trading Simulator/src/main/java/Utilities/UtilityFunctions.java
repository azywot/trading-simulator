package Utilities;

import Universe.World;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The class provides a few useful static methods used repeatedly.
 */
public class UtilityFunctions {

    private static final String[] suffixes = {" Stock Exchange", " Commodity Exchange", " Currency Exchange"};

    /**
     * Checks whether there exists a market in the world of a specified name.
     * @param marketName A String representing a market name.
     * @return A boolean value: true if the market of a name specified already exists in the world, false otherwise.
     */
    public static boolean marketNameChecker(String marketName){
        World w = World.INSTANCE;
        marketName = "The " + marketName;
        return w.getMarkets().containsKey(marketName + suffixes[0]) ||
                w.getMarkets().containsKey(marketName + suffixes[1]) ||
                w.getMarkets().containsKey(marketName + suffixes[2]);
    }

    /**
     * Generates a random floating-point number within a specified range.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return A random floating-point number.
     */
    public static float generateRandomFloat(float min, float max) {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    /**
     * Generates a random integer within a specified range.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return A random integer.
     */
    public static int generateRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * Gets a random, length-specified sublist based on the values of a map.
     * @param map Map for the sublist to be generated.
     * @param n The length of the sublist.
     * @param <K> The type of keys in a map.
     * @param <E> The type of values in a map.
     * @return A random sublist of size n.
     */
    public static <K, E> List<E> shuffleSelectN(Map<K, E> map, int n) {
        List<E> list = new ArrayList<>(map.values());
        Collections.shuffle(list);
        return list.subList(0, n);
    }
}
