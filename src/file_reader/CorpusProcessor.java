package file_reader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import file_reader.Word.Boundary;
import main.MapUtil;

public class CorpusProcessor {
    // To save the data, we need a serializable wrapper around the Comparator class
    public static final class SerializableComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
        @Override
        public int compare(T o1, T o2) {
            return o1.compareTo(o2);
        }
    }

    private static SerializableComparator<String> _stringCompare = new SerializableComparator<>();
    
    public static Pair<Map<String, Set<Pair<String, Integer>>>, Map<String, Set<String>>> getWordMaps(String filename) {
        Map<String, Set<Pair<String, Integer>>> wordsToTagFreqs = new TreeMap<>(_stringCompare);
        Map<String, Set<String>> legalNextTags = new TreeMap<>(_stringCompare);
        Map<Boundary, List<Boundary>> legalBoundaryContours = MapUtil.getLegalBoundaryContours();

        try {
            Scanner scanner = new Scanner(new File ("assets",filename));
            String lastTag = null;
            Word lastWord = null;
            boolean atClauseStart = false;
            while (scanner.hasNext()) {
                String fullWordString = scanner.next();
                Word thisWord;
                try {
                    // Ignore foreign words
                    if (fullWordString.contains("_FW")) {
                        continue;
                    }
                    // If the "word" is a punctuation mark, 
                    // its first character won't be alphabetic
                    if (!Character.isAlphabetic(fullWordString.charAt(0))) {
                        atClauseStart = true;
                        if (lastWord == null) {
                            continue;
                        }
                        // the last word was actually an instance of Boundary.END, so
                        // we have to decrement its count in wordsToTagFreqs accordingly
                        Set<Pair<String, Integer>> tags = wordsToTagFreqs.get(lastWord.getWord());
                        Pair<String, Integer> tagToRemove = null;
                        for (var tag : tags) {
                            if (tag.first().equals(lastTag)) {
                                tagToRemove = tag;
                                break;
                            }
                        }

                        if (tagToRemove != null) {
                            int freq = tagToRemove.second();
                            tags.remove(tagToRemove);
                            tags.add(tagToRemove.replaceSecond(freq - 1));
                        }

                        // Then we want to add it again, but with the END boundary
                        thisWord = new Word(lastWord.getWord(), lastWord.getTag(), Word.Boundary.END);
                    } else {
                        thisWord = atClauseStart ? new Word(fullWordString, Word.Boundary.START)
                                                : new Word(fullWordString, Word.Boundary.MIDDLE);
                        atClauseStart = false;
                    }

                    Pair<String, Integer> pair = new Pair<>(thisWord.getTag(), 1);
                    Set<Pair<String, Integer>> set = new TreeSet<>();
                    set.add(pair);
                    wordsToTagFreqs.merge(thisWord.getWord(), set, (existingSet, newSet) -> {
                        // Look to see if this tag already exists
                        Pair<String, Integer> sameTag = null;
                        for (var tagPair : existingSet) {
                            if (tagPair.first().equals(thisWord.getTag())) {
                                sameTag = tagPair;
                                break;
                            }
                        }
                        if (sameTag != null) {
                            // If so, increment its size
                            existingSet.remove(sameTag);
                            int freq = sameTag.second() + 1;
                            existingSet.add(sameTag.replaceSecond(freq));
                        } else {
                            // Otherwise, add it in
                            sameTag = new Pair<>(thisWord.getTag(), 1);
                            existingSet.add(sameTag);
                        }
                        return existingSet;
                    });
                    
                    // Reformats the tag to include all possible boundaries, given the previous tag's boundary mark.
                    // Otherwise, some rarer words caused whole sentences to be rejected incorrectly.
                    String currentTag = thisWord.getTag();
                    if (lastTag != null) {
                        Boundary lastBoundary = Word.getBoundary(lastTag);
                        String currentPOS = Word.getPOS(currentTag);
                        List<Boundary> nextBoundaries = legalBoundaryContours.get(lastBoundary);
                        for (Boundary boundary : nextBoundaries) {
                            String newTag = currentPOS+";"+boundary;
                            Set<String> vSet = new TreeSet<>();
                            vSet.add(newTag);
                            legalNextTags.merge(lastTag, vSet, (oldSet, newSet) -> {
                                oldSet.addAll(newSet);
                                return oldSet;
                            });
                        }
                    }
                    lastWord = thisWord;
                    lastTag = currentTag;
                    
                } catch (IllegalArgumentException e) {
                    System.out.println("Couldn't read token '"+fullWordString+"'");
                    System.out.println();
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException("problem reading file: "+filename);
        }
        
        // System.out.println(_totalWords(wordsToTagFreqs));

        return new Pair<>(wordsToTagFreqs, legalNextTags);
    }

    @SuppressWarnings("unused")
    // See commented-out line above; leaving this in here for transparency of how I got ParseTree.TOTAL_WORDS
    private static int _totalWords(Map<String, Set<Pair<String, Integer>>> wordsToTagFreqs) {
        int total = 0;
        for (var tagSet : wordsToTagFreqs.values()) {
            for (Pair<String,Integer> pair : tagSet) {
                total += pair.second();
            }
        }
        return total;
    }

}