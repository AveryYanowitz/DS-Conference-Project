package main;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public class MapUtil {

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

        final int MAX_LENGTH = 1000;
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

    // Not sure what to call this function, but what it does is: if key exists, 
    // it adds value to the Set<V> mapped to K; if not, it creates a new set 
    // around the given value and adds the new key-value mapping to the given map
    public static <K, V> void mergeIntoSet(K destinationKey, V destinationValue, 
                                            Map <K, Set<V>> destinationMap) {
        Set<V> vSet = new TreeSet<>();
        vSet.add(destinationValue);
        destinationMap.merge(destinationKey, vSet, (oldSet, newSet) -> {
            oldSet.addAll(newSet);
            return oldSet;
        });
    }

    // Removes all keys not found in toKeep list
    public static <K, V> void filterKeys(Map<K, V> mapToFilter, Predicate<Map.Entry<K, V>> removeIfTrue) {
        var mapEntries = mapToFilter.entrySet();
        mapEntries.removeIf((var entry) -> {
            return removeIfTrue.test(entry);
        });
    }

    // Removes all elements not found in toKeep from each set
    public static <K, V> void filterValues(Map<K, Set<V>> mapToFilter, Predicate<V> removeIfTrue) {
        var mapEntries = mapToFilter.entrySet();
        for (var entry : mapEntries) {
            Set<V> value = entry.getValue();
            value.removeIf((var valueMember) -> {
                return removeIfTrue.test(valueMember);
            });
        }

        // Any sets that are now empty should be removed
        // to avoid NullPointerExceptions later
        mapEntries.removeIf((var entry) -> {
            return entry.getValue().size() == 0;
        });

    }

}
