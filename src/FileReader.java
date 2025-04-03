import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;

public class FileReader {
    public record mapTuple<K, V, J, W>(Map<K, V> map1, Map<J, W> map2) { }
    public static mapTuple<Word, Integer, String, Set<String>> getWordMaps(String filename) {
        File txtFile = getFile(filename);
        var maps = readFile(txtFile);
        return maps;
    }

    // @SuppressWarnings("resource")
    private static File getFile(String filename) {
        File file = new File("assets",filename);
        return file;
    }

    private static mapTuple<Word, Integer, String, Set<String>> readFile(File file) {
        // Sort functions make the maps sorted from A -> Z, not Z -> A
        Map<Word, Integer> wordMap = new TreeMap<>((word1, word2) -> 
                                                    word2.compareTo(word1));
        Map<String, Set<String>> tagMap = new TreeMap<>((tag1, tag2) -> 
                                                    tag2.compareTo(tag1));
        try {
            Scanner scanner = new Scanner(file);
            String previous_tag = null;
            while (scanner.hasNext()) {
                String nextWord = scanner.next();
                try {
                    // If the 'word' is punctuation, its first character won't be
                    // alphabetic, so we want to exclude it.
                    char firstChar = nextWord.toCharArray()[0];
                    if (!Character.isAlphabetic(firstChar)) {
                        continue;
                    }
                    Word newWord = new Word(nextWord);
                    wordMap.merge(newWord, 1, (current, given) -> current + 1);
                    
                    // Add the previous tag as the key and a set containing the current tag as
                    // the value; if the key already exists, add the current tag to its set
                    String current_tag = newWord.getTag();
                    // System.out.println(current_tag);
                    if (previous_tag != null) {
                        MapTools.mergeIntoSet(previous_tag, current_tag, tagMap);
                    }
                    previous_tag = current_tag;
                    
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