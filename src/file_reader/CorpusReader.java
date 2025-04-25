package file_reader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import main.Word;
import main.Word.Boundary;
import main.MapUtil;

public class CorpusReader {
    // To save the data, we need a serializable wrapper around the Comparator class
    private static SerializableComparator<Word> _wordCompare = new SerializableComparator<>();
    private static SerializableComparator<String> _stringCompare = new SerializableComparator<>();
    
    public record TwoMaps<K, K2, V, V2>(Map<K, V> map1, Map<K2, V2> map2) implements Serializable { }
    public static TwoMaps<String, String, Set<String>, Set<String>> getWordMaps(String filename) {
        Map<Word, Integer> wordMap = new TreeMap<>(_wordCompare);
        Map<String, Set<String>> legalNextTags = new TreeMap<>(_stringCompare);
        Map<Boundary, List<Boundary>> legalBoundaryContours = MapUtil.getLegalBoundaryContours();

        try {
            Scanner scanner = new Scanner(new File ("assets",filename));
            String lastTag = null;
            Word lastWord = null;
            boolean atClauseStart = false;
            while (scanner.hasNext()) {
                String nextWord = scanner.next();
                final Word newWord;
                try {
                    // Ignore foreign words
                    if (nextWord.contains("_FW")) {
                        continue;
                    }
                    // If the "word" is a punctuation mark, 
                    // its first character won't be alphabetic
                    if (!Character.isAlphabetic(nextWord.charAt(0))) {
                        atClauseStart = true;
                        if (lastWord == null) {
                            continue;
                        }
                        // the last word was actually an instance of Boundary.END,
                        // so we have to decrement its count in wordMap accordingly
                        int previousFreq = wordMap.get(lastWord);
                        wordMap.put(lastWord, previousFreq - 1);

                        // Then we want to add it again, but with the END boundary
                        newWord = new Word(lastWord.getWord(), lastWord.getTag(), Word.Boundary.END);
                    } else {
                        newWord = atClauseStart ? new Word(nextWord, Word.Boundary.START) 
                                                : new Word(nextWord, Word.Boundary.MIDDLE);
                        atClauseStart = false;
                    }
                    wordMap.merge(newWord, 1, (current, given) -> current + 1);
                    
                    // Reformats the tag to include all possible boundaries, given the previous tag's boundary mark.
                    // Otherwise, some rarer words caused whole sentences to be rejected incorrectly.
                    String currentTag = newWord.getTag();
                    if (lastTag != null) {
                        Boundary lastBoundary = Word.getBoundary(lastTag);
                        String currentPOS = Word.getPOS(currentTag);
                        List<Boundary> nextBoundaries = legalBoundaryContours.get(lastBoundary);
                        for (Boundary boundary : nextBoundaries) {
                            String newTag = currentPOS+";"+boundary;
                            MapUtil.mergeIntoSet(lastTag, newTag, legalNextTags);
                        }
                    }
                    lastWord = newWord;
                    lastTag = currentTag;
                    
                } catch (IllegalArgumentException e) {
                    System.out.println("Couldn't read token '"+nextWord+"'");
                    System.out.println();
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException("problem reading file: "+filename);
        }
        Map<String, Set<String>> wordsWithTags = MapUtil.extractTags(wordMap);
        return new TwoMaps<>(wordsWithTags, legalNextTags);
    }

}