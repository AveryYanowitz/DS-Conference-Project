import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Scanner;

public class Tagger {
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter filename: ");
        String filename = scanner.next();

        // Timed it 100 times; the processing is almost exactly 1 sec on average
        var freqsAndTags = FileReader.getWordMaps(filename);
        Map<Word, Integer> wordsWithFreqs = freqsAndTags.map1();
        Map<String, Set<String>> followingTags = freqsAndTags.map2();
        Map<Word, Set<String>> wordsWithTags = MapTools.extractTags(wordsWithFreqs);

        do {
            System.out.println();

            record WordAndBound (String rawWord, Word.Boundary boundary) {}
            String words = _getString("Enter a sentence to tag: ");
            String[] rawWords = words.split(" ");
            List<WordAndBound> wordList = new ArrayList<>();
            
            boolean startOfClause = false;    
            for (String wordString : rawWords) {
                char firstChar = wordString.toCharArray()[0];
                WordAndBound newWord;
                if (!Character.isAlphabetic(firstChar)) {
                    startOfClause = true;
                    WordAndBound last = wordList.getLast();
                    if (last == null) {
                        continue;
                    }
                    wordList.removeLast();
                    newWord = new WordAndBound(wordString, Word.Boundary.END);
                } else if (startOfClause) {
                    newWord = new WordAndBound(wordString, Word.Boundary.START);
                    startOfClause = false;
                } else {
                    newWord = new WordAndBound(wordString, Word.Boundary.END);
                }
                wordList.add(newWord);
            }

            Set<List<String>> timelines = new TreeSet<>((x, y) -> x.get(x.size() - 1).compareTo(y.get(y.size() - 1)));
            for (Word word : wordList) {
                Set<String> tagEntries = wordsWithTags.get(word);
                if (tagEntries != null) {
                    timelines = _updateTimelines(word, timelines, wordsWithTags, followingTags);
                }
                else {
                    System.out.println(word + " - no entries found");
                }
            }
            System.out.println(timelines);
        } while (_getYN("Add another sentence?"));

        System.out.println("Thanks for stopping by!");
    }
    
    private static Set<List<String>> _updateTimelines(Word word, Set<List<String>> timelines,
                                        Map<Word, Set<String>> wordsWithTags, 
                                        Map<String, Set<String>> followingTags) {
        Set<String> possibleTags = wordsWithTags.get(word);
        // Force timelines to be a TreeSet so comparator() works
        var cmp = ((TreeSet<List<String>>)timelines).comparator();
        Set<List<String>> timelinesUpdated = new TreeSet<>(cmp);
        if (timelines.size() == 0) {
            for (String tag : possibleTags) {
                List<String> newTimeline = new ArrayList<>();
                newTimeline.add(tag);
                timelinesUpdated.add(newTimeline);
            }
        } else {
            for (var timeline : timelines) {
                Set<String> followables = followingTags.get(timeline.getLast());
                List<String> validTags = _intersectionOf(followables, possibleTags);
                int size = validTags.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        List<String> newTimeline = new ArrayList<>(timeline);
                        newTimeline.add(validTags.get(i));
                        timelinesUpdated.add(newTimeline);
                    }
                }
            }
        }
    return timelinesUpdated;
    }

    private static List<String> _intersectionOf(Set<String> set1, Set<String> set2) {
        List<String> intersection = new ArrayList<>();
        for (String elem : set1) {
            if (set2.contains(elem)) {
                intersection.add(elem);
            }
        }
        return intersection;
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