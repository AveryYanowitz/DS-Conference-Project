import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;
import java.util.Set;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class CorpusReader {
    public record mapTuple<K, V, K2, V2>(Map<K, V> map1, Map<K2, V2> map2) { }
    public static mapTuple<Word, Integer, String, Set<String>> getWordMaps(String filename) {
        var maps = readFile(new File ("assets",filename));
        return maps;
    }

    private static class SerializableComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
        @Override
        public int compare(T o1, T o2) {
            return o2.compareTo(o1);
        }
    }

    private static SerializableComparator<Word> _wordComp = new SerializableComparator<>();
    private static SerializableComparator<String> _stringComp = new SerializableComparator<>();

    private static mapTuple<Word, Integer, String, Set<String>> readFile(File file) {
        // Sort functions make the maps sorted from A -> Z, not Z -> A
        Map<Word, Integer> wordMap = new TreeMap<>(_wordComp);
        Map<String, Set<String>> tagMap = new TreeMap<>(_stringComp);
        try {
            Scanner scanner = new Scanner(file);
            String previousTag = null;
            Word lastWord = null;
            boolean atClauseStart = false;
            while (scanner.hasNext()) {
                String nextWord = scanner.next();
                final Word newWord;
                try {
                    // If the "word" is a punctuation mark, 
                    // its first character won't be alphabetic
                    char firstChar = nextWord.toCharArray()[0];
                    if (!Character.isAlphabetic(firstChar)) {
                        atClauseStart = true;
                        if (lastWord == null) {
                            continue;
                        }
                        // the last word was actually an instance of Boundary.END, so
                        // we have to decrement its count in wordMap accordingly
                        int previousFreq = wordMap.get(lastWord);
                        wordMap.put(lastWord, previousFreq - 1);

                        // Then we want to add it again, but with the END boundary
                        newWord = new Word(lastWord.getWord(), lastWord.getTag(), Word.Boundary.END);
                    } else {
                        newWord = atClauseStart ? new Word(nextWord, Word.Boundary.START) : new Word(nextWord, Word.Boundary.MIDDLE);
                        atClauseStart = false;
                    }
                    wordMap.merge(newWord, 1, (current, given) -> current + 1);
                    
                    // Add the previous tag as the key and a set containing the current tag as
                    // the value; if the key already exists, add the current tag to its set
                    String currentTag = newWord.getTag();
                    if (previousTag != null) {
                        Utilities.mergeIntoSet(previousTag, currentTag, tagMap);
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
            throw new RuntimeException("problem reading file: "+file);
        }
        return new mapTuple<>(wordMap, tagMap);
    }

}