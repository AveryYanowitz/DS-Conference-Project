import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Scanner;

public class Tagger {
    record WordAndBound (String rawWord, Word.Boundary boundary) { 
        public WordAndBound(String rawWord, Word.Boundary boundary) {
            this.rawWord = rawWord.toLowerCase();
            this.boundary = boundary;
        }
    }

    public static void main(String[] args) {
        // Timed it 100x; the processing for these lines is ~1 sec on average
        Map<String, Set<String>> followingTags = null;
        Map<String, Set<String>> wordsWithTags = null;
        
        try {            
            followingTags = Serializer.importMap(new File("assets","followingTags.ser"));
            wordsWithTags = Serializer.importMap(new File("assets","wordsWithTags.ser"));
        } catch (IOException io) {
            io.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
        }

        
        // This map shows which boundaries can follow each other
        Map<Word.Boundary, List<Word.Boundary>> boundariesAllowed = new TreeMap<>();
        final Word.Boundary[] start = {Word.Boundary.START};
        final Word.Boundary[] nonStart = {Word.Boundary.MIDDLE, Word.Boundary.END};

        boundariesAllowed.put(Word.Boundary.START, Arrays.asList(nonStart));
        boundariesAllowed.put(Word.Boundary.MIDDLE, Arrays.asList(nonStart));
        boundariesAllowed.put(Word.Boundary.END, Arrays.asList(start));

        do {
            System.out.println();

            // Get a sentence from a user, take the words, and turn them into WordAndBound records
            String sentence = _getString("Enter a sentence to tag: ");
            String[] rawWords = sentence.split(" ");
            List<WordAndBound> wordList = new ArrayList<>();
            
            boolean startOfClause = true;
            for (String wordString : rawWords) {
                WordAndBound newWord;
                if (Utilities.hasNonAlpha(wordString)) {
                    startOfClause = true;
                    newWord = new WordAndBound(Utilities.stripNonAlpha(wordString), Word.Boundary.END);
                } else if (startOfClause) {
                    startOfClause = false;
                    newWord = new WordAndBound(wordString, Word.Boundary.START);
                } else {
                    newWord = new WordAndBound(wordString, Word.Boundary.MIDDLE);
                }
                wordList.add(newWord);
            }

            // The final word is always the end of a clause (assuming a well-formed sentence)
            WordAndBound lastWord = wordList.removeLast();
            wordList.add(new WordAndBound(lastWord.rawWord(), Word.Boundary.END));

            // Take each word in wordList in order and use it to update existing timelines
            Set<List<String>> timelines = new TreeSet<>((x, y) -> x.get(x.size() - 1).compareTo(y.get(y.size() - 1)));
            for (WordAndBound word : wordList) {
                Set<String> tags = wordsWithTags.get(word.rawWord());
                if (tags != null) {
                    timelines = _updateTimelines(word, timelines, tags, followingTags, boundariesAllowed);
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
                                        Set<String> possibleTags, Map<String, Set<String>> followingTags,
                                        Map<Word.Boundary, List<Word.Boundary>> boundariesAllowed) {

        // Force timelines to be a TreeSet so comparator() works
        var cmp = ((TreeSet<List<String>>)timelines).comparator();
        Set<List<String>> timelinesUpdated = new TreeSet<>(cmp);
        
        // Special case: no timelines added yet
        if (timelines.size() == 0) {
            for (String tag : possibleTags) {
                if (Word.getBoundary(tag) != Word.Boundary.START) {
                    continue;
                }
                List<String> newTimeline = new ArrayList<>();
                newTimeline.add(tag);
                timelinesUpdated.add(newTimeline);
            }
        } else {
            for (List<String> timeline : timelines) {
                Set<String> followables = followingTags.get(timeline.getLast());
                List<String> validTags = _getFollowers(followables, possibleTags);
                Word.Boundary lastBound = Word.getBoundary(timeline.getLast());
                for (String tag : validTags) {
                    Word.Boundary thisBound = Word.getBoundary(tag);
                    var followingBounds = boundariesAllowed.get(lastBound);
                    // Check for invalid boundaries
                    if (thisBound != word.boundary() || !followingBounds.contains(thisBound)) {
                        continue;
                    }
                    List<String> newTimeline = new ArrayList<>(timeline);
                    newTimeline.add(tag);
                    timelinesUpdated.add(newTimeline);
                }
            }
        }
        return timelinesUpdated;
    }

    private static List<String> _getFollowers(Set<String> followables, Set<String> allPossible) {
        List<String> intersection = new ArrayList<>();
        for (String follower : followables) {
            for (String possible : allPossible) {
                if (follower.equals(possible)
                || Word.getPOS(follower).equals(Word.getPOS(possible))) {
                    intersection.add(possible);
                }
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