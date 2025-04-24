package file_reader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;

import main.Word;
import main.Utilities;

public class CorpusReader {
    // To save the data, we need a serializable wrapper around the Comparator class
    private static SerializableComparator<Word> _wordCompare = new SerializableComparator<>();
    private static SerializableComparator<String> _stringCompare = new SerializableComparator<>();
    
    public record threeMaps<K, K2, K3, V, V2, V3>(Map<K, V> map1, Map<K2, V2> map2, Map<K3, V3> map3) implements Serializable { }
    public static threeMaps<Word, String, String, Integer, Set<String>, Set<String>> getWordMaps(String filename) {
        Map<Word, Integer> wordMap = new TreeMap<>(_wordCompare);
        Map<String, Set<String>> afterTags = new TreeMap<>(_stringCompare);
        Map<String, Set<String>> beforeTags = new TreeMap<>(_stringCompare);
        try {
            Scanner scanner = new Scanner(new File ("assets",filename));
            String previousTag = null;
            Word lastWord = null;
            boolean atClauseStart = false;
            while (scanner.hasNext()) {
                String nextWord = scanner.next();
                final Word newWord;
                try {
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
                    
                    // Add the previous tag as the key and a set containing the current tag as
                    // the value; if the key already exists, add the current tag to its set
                    String currentTag = newWord.getTag();
                    if (previousTag != null) {
                        Utilities.mergeIntoSet(currentTag, previousTag, beforeTags);
                        Utilities.mergeIntoSet(previousTag, currentTag, afterTags);
                    }
                    lastWord = newWord;
                    previousTag = currentTag;
                    
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
        return new threeMaps<>(wordMap, beforeTags, afterTags);
    }

}