package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import file_reader.Word.Boundary;

import java.util.TreeMap;

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

    // Returns a map showing which boundaries follow each other
    public static Map<Boundary, List<Boundary>> getLegalBoundaryContours() {
        Map<Boundary, List<Boundary>> legalBoundaryContours = new TreeMap<>();
        final Boundary[] start = {Boundary.START};
        final Boundary[] nonStart = {Boundary.MIDDLE, Boundary.END};
        legalBoundaryContours.put(Boundary.START, Arrays.asList(nonStart));
        legalBoundaryContours.put(Boundary.MIDDLE, Arrays.asList(nonStart));
        legalBoundaryContours.put(Boundary.END, Arrays.asList(start));
        return legalBoundaryContours;
    }

    // Removes all keys not found in toKeep list
    public static <V> void filterKeys(Map<String, V> mapToFilter, List<String> toKeep) {
        Set<Map.Entry<String, V>> mapEntries = mapToFilter.entrySet();
        Set<String> filterSet = new TreeSet<>(toKeep); // TreeSet because lots of searching
        mapEntries.removeIf((var entry) -> {
            // toKeep only contains the first two characters of each tag, because
            // those indicate the broad "tag" categories. It would be too annoying
            // for the user to have to type out ALL of the tags they want to include.
            String shortKey = entry.getKey().substring(0,2);
            return !filterSet.contains(shortKey);
        });
    }

    // Removes all elements not found in toKeep from each set
    public static <K, V> void filterValues(Map<K, Set<V>> mapToFilter, Predicate<V> filterFunc) {
        var mapEntries = mapToFilter.entrySet();
        for (var entry : mapEntries) {
            Set<V> value = entry.getValue();
            value.removeIf((var tagPair) -> {
                return filterFunc.test(tagPair);
            });
        }

        // Any sets that are now empty should be removed
        // to avoid NullPointerExceptions later
        mapEntries.removeIf((var entry) -> {
            return entry.getValue().size() == 0;
        });

    }

}
