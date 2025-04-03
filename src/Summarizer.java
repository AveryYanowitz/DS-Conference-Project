import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class Summarizer {
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter filename: ");
        String filename = scanner.next();

        // Timed it 100 times; the processing is almost exactly 1 sec on average
        var twoMaps = FileReader.getWordMaps(filename);
        Map<Word, Integer> wordsWithFreqs = twoMaps.map1();
        Map<String, Set<String>> followingTags = twoMaps.map2();
        Map<String, Set<String>> wordsWithTags = MapTools.extractTags(wordsWithFreqs);
        // Map<Integer, Set<Word>> freqsWithWords = MapTools.invertMap(wordsWithFreqs);
        // Map<Integer, Set<Word>> roundedFreqs = MapTools.roundMap(freqsWithWords);
        
        _tagSentence(wordsWithTags, followingTags);
        System.out.println("Thanks for stopping by!");
    }

    @SuppressWarnings("resource")
    public static String getString(String prompt) {
        Scanner reader = new Scanner(System.in);
        System.out.print(prompt);
        return reader.nextLine();
    }
    
    private static void _tagSentence(Map<String, Set<String>> wordsWithTags, Map<String, Set<String>> followingTags) {
        do {        
            System.out.println();
            String words = getString("Enter a sentence to tag: ");
            // Remove apostrophes and punctuation to match Word objects
            StringBuilder sb = new StringBuilder();
            for (char ch : words.toCharArray()) {
                if (ch == ' ' || Character.isAlphabetic(ch)) {
                    sb.append(ch);
                }
            }
            
            String[] wordList = sb.toString().split(" ");
            for (String word : wordList) {
                Set<String> entry = wordsWithTags.get(word.toLowerCase());
                if (entry != null) {
                    System.out.println(word + " - " + entry.toString());
                }
                else {
                    System.out.println(word + " - no entries found");
                }
            }
        } while (getString("Add another sentence? (Y/N) ").toLowerCase().equals("y"));
    }

}