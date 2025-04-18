import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Scanner;

public class Tagger {
    record WordAndBound (String rawWord, Word.Boundary boundary) { 
        public WordAndBound(String rawWord, Word.Boundary boundary) {
            this.rawWord = rawWord.toLowerCase();
            this.boundary = boundary;
        }
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter filename: ");
        String filename = scanner.next();

        // Timed it 100 times; the processing is almost exactly 1 sec on average
        var freqsTagsTuple = FileReader.getWordMaps(filename);
        Map<Word, Integer> wordsWithFreqs = freqsTagsTuple.map1();
        Map<String, Set<String>> followingTags = freqsTagsTuple.map2();
        Map<String, Set<String>> wordsWithTags = Utilities.extractTags(wordsWithFreqs);

        do {
            System.out.println();

            String words = _getString("Enter a sentence to tag: ");
            String[] rawWords = words.split(" ");
            List<WordAndBound> wordList = new ArrayList<>();
            
            boolean startOfClause = true;
            for (String wordString : rawWords) {
                char firstChar = wordString.toCharArray()[0];
                WordAndBound newWord;
                if (!Character.isAlphabetic(firstChar)) {
                    startOfClause = true;
                    if (wordList.getLast() == null) {
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
            for (WordAndBound word : wordList) {
                Set<String> tags = wordsWithTags.get(word.rawWord());
                if (tags != null) {
                    // Need wrapper so lastBoundary can be updated but not returned.
                    // This is very inelegant, so I'll (hopefully) fix it later.
                    Word.Boundary[] lastBoundary = { Word.Boundary.START };
                    timelines = _updateTimelines(word, timelines, wordsWithTags, followingTags, lastBoundary);
                    // If no possible timelines are left, we're done
                    if (timelines.size() == 0) {
                        break;
                    }
                }
                else {
                    System.out.println(word.rawWord() + " - no entries found");
                }
            }
            System.out.println(timelines);
        } while (_getYN("Add another sentence?"));

        System.out.println("Thanks for stopping by!");
    }
    
    private static Set<List<String>> _updateTimelines(WordAndBound word, Set<List<String>> timelines,
                                        Map<String, Set<String>> wordsWithTags, 
                                        Map<String, Set<String>> followingTags,
                                        Word.Boundary[] lastBoundary) {

        Set<String> possibleTags = wordsWithTags.get(word.rawWord());
        // Force timelines to be a TreeSet so comparator() works
        var cmp = ((TreeSet<List<String>>)timelines).comparator();
        Set<List<String>> timelinesUpdated = new TreeSet<>(cmp);
        Word.Boundary currentBoundary = lastBoundary[0];
        
        // Special case: no timelines added yet
        if (timelines.size() == 0) {
            if (word.boundary() != currentBoundary) {
                // Return empty TL if first word can't start sentence;
                // this will reject the entire timeline
                return timelinesUpdated;
            }
            for (String tag : possibleTags) {
                List<String> newTimeline = new ArrayList<>();
                newTimeline.add(tag);
                timelinesUpdated.add(newTimeline);
                lastBoundary[0] = Word.Boundary.START;
            }
        } else {
            for (List<String> timeline : timelines) {
                Set<String> followables = followingTags.get(timeline.getLast());
                List<String> validTags = _intersectionOf(followables, possibleTags);
                for (String tag : validTags) {
                    List<String> newTimeline = new ArrayList<>(timeline);
                    newTimeline.add(tag);
                    timelinesUpdated.add(newTimeline);
                }
            }
        }
        return timelinesUpdated;
    }

    private static <T> List<T> _intersectionOf(Set<T> set1, Set<T> set2) {
        List<T> intersection = new ArrayList<>();
        for (T elem : set1) {
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