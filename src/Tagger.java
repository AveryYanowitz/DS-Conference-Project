import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class Tagger {
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
    
    private static void _tagSentence(Map<String, Set<String>> wordsWithTags, Map<String, Set<String>> followingTags) {
        do {        
            System.out.println();
            String words = _getString("Enter a sentence to tag: ");
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
        } while (_getYN("Add another sentence?"));
    }

    @SuppressWarnings("resource")
    private static String _getString(String prompt) {
        Scanner reader = new Scanner(System.in);
        System.out.print(prompt);
        return reader.nextLine();
    }

    @SuppressWarnings("resource")
    private static boolean _getYN(String prompt) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(prompt+" (Y/N) ");
            String s = scanner.next();
            if (s.toLowerCase().equals("y") || s.toLowerCase().equals("yes")) {
                return true;
            }
            if (s.toLowerCase().equals("n") || s.toLowerCase().equals("no")) {
                return false;
            }
            System.out.println("Didn't understand, please try again.");
        }
    }

}