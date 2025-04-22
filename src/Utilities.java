import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;

public class Utilities {

    public static <K, V> void printMap(Map<K, V> mapToPrint, int numToPrint, boolean backwards) {
        ArrayList<Map.Entry<K, V>> wordList = new ArrayList<>(mapToPrint.entrySet());
        int index;
        int index_change;
        if (backwards) {
            index = wordList.size() - 1;
            index_change = -1;
        } else {
            index = 0;
            index_change = 1;
        }

        final int MAX_LENGTH = 100;
        for (int i = 0; i < numToPrint; i++) {
            Map.Entry<K,V> entry = wordList.get(index);
            
            String key_string = entry.getKey().toString();
            if (key_string.length() > MAX_LENGTH) {
                key_string = key_string.substring(0, 
                Math.min(MAX_LENGTH, key_string.length())) + "...";
            }
            String val_string = entry.getValue().toString();
            if (val_string.length() > MAX_LENGTH) {
                val_string = val_string.substring(0, 
                Math.min(MAX_LENGTH, key_string.length())) + "...";
            }

            System.out.println(key_string);
            System.out.print("Value: ");
            System.out.println(val_string);
            System.out.println();
            
            index += index_change;
        }
    }

    // Takes a map of <K, V> and returns the equivalent map of <V, Set<K>>, where
    // repeat Vs in the original map have their Ks added to Set<K>
    public static <K,V> Map<V, Set<K>> invertMap(Map <K, V> mapToInvert) {
        Set<K> keySet = mapToInvert.keySet();
        Map<V, Set<K>> inverseMap = new TreeMap<>();
        for (K key : keySet) {
            mergeIntoSet(mapToInvert.get(key), key, inverseMap);
        }
        return inverseMap;
    }

    // Rounds all values to their nearest power of ten (thousands to the nearest thousand,
    // hundreds to the nearest hundred, etc.) ***Way too complicated, I can probably use logs
    // and modulo to make it simpler later.***
    public static <V> Map<Integer, Set<V>> roundMap(Map<Integer, Set<V>> mapToRound) {
        Map<Integer, Set<V>> roundedMap = new TreeMap<>();
        Set<Integer> allKeys = mapToRound.keySet();
        for (Integer key : allKeys) {
            Set<V> val = mapToRound.get(key);
            int leftmost = key;
            while (leftmost > 9) {
                leftmost /= 10;
            }
            int magnitude = (int) Math.pow(10, (int) Math.log10(key));
            int roundedKey = leftmost * magnitude;
            roundedMap.merge(roundedKey, val, (oldSet, newSet) -> {
                        oldSet.addAll(newSet);
                        return oldSet;
                    });
        }
        return roundedMap;
    }

    public static Map<String, Set<String>> extractTags(Map <Word, Integer> extractMap) {
        Map<String, Set<String>> tagMap = new TreeMap<>();
        Set<Word> keySet = extractMap.keySet();
        for (Word key : keySet) {
            mergeIntoSet(key.getWord(), key.getTag(), tagMap);
        }
        return tagMap;
    }

    // I have no idea what to call this function
    public static <K, V> void mergeIntoSet(K destinationKey, V destinationValue, Map <K, Set<V>> destinationMap) {
        Set<V> vSet = new TreeSet<>();
        vSet.add(destinationValue);
        destinationMap.merge(destinationKey, vSet, (oldSet, newSet) -> {
            oldSet.addAll(newSet);
            return oldSet;
        });
    }

    public static String stripNonAlpha(String oldString) {
        StringBuilder sb = new StringBuilder();
        for (char ch : oldString.toCharArray()) {
            if (isExpandedAlpha(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static boolean hasNonAlpha(String s) {
        for (char ch : s.toCharArray()) {
            if (!isExpandedAlpha(ch)) {
                return true;
            }
        }
        return false;
    }

    // I don't know what to name this
    public static boolean isExpandedAlpha(char ch) {
        return acceptAnyway(ch) || Character.isAlphabetic(ch);
    }

    private static boolean acceptAnyway(char ch) {
        return ch == '\'' || ch == '"' || ch == '_';
    }
    
}
