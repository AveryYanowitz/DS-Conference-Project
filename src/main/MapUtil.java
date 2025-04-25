package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import main.Word.Boundary;

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
    public static <K> void filterValues(Map<K, Set<String>> mapToFilter, List<String> toKeep) {
        Set<Map.Entry<K, Set<String>>> mapEntries = mapToFilter.entrySet();
        Set<String> filterSet = new TreeSet<>(toKeep); // TreeSet because lots of searching
        for (var entry : mapEntries) {
            Set<String> value = entry.getValue();
            value.removeIf((var tag) -> {
                String shortTag = tag.substring(0,2);
                return !filterSet.contains(shortTag);
            });
        }
        
        // Any sets that are now empty should be removed
        // to avoid NullPointerExceptions later
        mapEntries.removeIf((var entry) -> {
            return entry.getValue().size() == 0;
        });

    }

}
