package main;

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

import main.Word.Boundary;
import file_reader.Serializer;

public class Tagger {
    record WordAndBound (String rawWord, Boundary boundary) { 
        public WordAndBound(String rawWord, Boundary boundary) {
            this.rawWord = rawWord.toLowerCase();
            this.boundary = boundary;
        }
    }

    public static void main(String[] args) {
        // Timed it 100x; the processing for these lines is ~1 sec on average
        Map<String, Set<String>> precedingTagsMap = null;
        Map<String, Set<String>> followingTagsMap = null;
        Map<String, Set<String>> wordsWithTags = null;

        try {
            System.out.println(new File(".").getAbsolutePath());
            precedingTagsMap = Serializer.importMap(new File("./assets/beforeTags.ser"));
            followingTagsMap = Serializer.importMap(new File("./assets/afterTags.ser"));
            wordsWithTags = Serializer.importMap(new File("./assets/wordsWithTags.ser"));
        } catch (IOException io) {
            io.printStackTrace();
            return;
        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
            return;
        }

        // This map shows which boundaries can follow each other
        Map<Boundary, List<Boundary>> legalBoundaryContours = new TreeMap<>();
        final Boundary[] start = {Boundary.START};
        final Boundary[] nonStart = {Boundary.MIDDLE, Boundary.END};
        legalBoundaryContours.put(Boundary.START, Arrays.asList(nonStart));
        legalBoundaryContours.put(Boundary.MIDDLE, Arrays.asList(nonStart));
        legalBoundaryContours.put(Boundary.END, Arrays.asList(start));

        do {
            System.out.println();

            // Get a sentence from a user, take the words, and turn them into WordAndBound records
            String sentence = _getString("Enter a sentence to tag: ");
            String[] rawWords = sentence.split(" ");
            List<WordAndBound> wordList = _addClauseContours(rawWords);
            
            // Take each word in wordList in order and use it to update existing timelines
            Set<List<String>> timelines = new TreeSet<>((x, y) -> x.get(x.size() - 1).compareTo(y.get(y.size() - 1)));
            for (WordAndBound word : wordList) {
                Boundary wordBound = word.boundary();
                Set<String> tagsForWord = wordsWithTags.get(word.rawWord());
                if (timelines.size() > 0) {
                    for (List<String> timeline : timelines) {
                        String lastTag = timeline.getLast();
                        Set<String> intersectingTags = _intersectionOf(followingTagsMap.get(lastTag), tagsForWord);
                        Set<String> validTags = _pruneBoundaries(lastTag, intersectingTags, wordBound, legalBoundaryContours);
                        timelines.remove(timeline);
                        for (String tag : validTags) {
                            List<String> newTimeline = new ArrayList<>(timeline);
                            newTimeline.add(tag);
                            timelines.add(newTimeline);
                        }
                    }
                    // If no possible timelines are left, we're done
                    if (timelines.size() == 0) {
                        break;
                    }
                }
                // Special case: no timelines added yet
                else {
                    for (String tag : tagsForWord) {
                        if (Word.getBoundary(tag) != Boundary.START) {
                            continue;
                        }
                        List<String> newTimeline = new ArrayList<>();
                        newTimeline.add(tag);
                        timelines.add(newTimeline);
                    }        
                }
            }
            System.out.println(timelines);
        } while (_getYN("Add another sentence?"));
        System.out.println("Thanks for stopping by!");
    }

    private static List<WordAndBound> _addClauseContours(String[] rawWords) {        
        List<WordAndBound> wordList = new ArrayList<>();
        boolean startOfClause = true;
        for (String wordString : rawWords) {
            WordAndBound newWord;
            if (Utilities.hasNonAlpha(wordString)) {
                startOfClause = true;
                newWord = new WordAndBound(Utilities.stripNonAlpha(wordString), Boundary.END);
            } else if (startOfClause) {
                startOfClause = false;
                newWord = new WordAndBound(wordString, Boundary.START);
            } else {
                newWord = new WordAndBound(wordString, Boundary.MIDDLE);
            }
            wordList.add(newWord);
        }

        // The final word is always the end of a clause (assuming a well-formed sentence)
        WordAndBound lastWord = wordList.removeLast();
        wordList.add(new WordAndBound(lastWord.rawWord(), Boundary.END));
        return wordList;
    }

    // Goes through the possible tags provided and determines which to consider based on local clause boundaries
    private static Set<String> _pruneBoundaries (String lastTag, Set<String> possibilities, Boundary wordBound,
                                                Map<Boundary, List<Boundary>> legalBoundaryContours) {
        Set<String> prunedPossibilities = new TreeSet<>(possibilities);
        Boundary lastBoundary = Word.getBoundary(lastTag);
        var followingBounds = legalBoundaryContours.get(lastBoundary);
        for (String tag : possibilities) {
            Boundary tagBoundary = Word.getBoundary(tag);
            if (tagBoundary == wordBound && followingBounds.contains(tagBoundary)) {
                prunedPossibilities.remove(tag);
            }
        }
        return prunedPossibilities;
    }
    
    // Takes the intersection of two sets; if both elements are word tags, it will
    // also accept tags that share a part of speech but not a word boundary, because
    // very rare words caused whole sentences to be rejected otherwise.
    private static Set<String> _intersectionOf(Set<String> wordSet1, Set<String> wordSet2) {
        Set<String> intersection = new TreeSet<>();
        for (String elem1 : wordSet1) {
            for (String elem2 : wordSet2) {
                if (elem1.equals(elem2)
                || Word.getPOS(elem1).equals(Word.getPOS(elem2))) {
                    intersection.add(elem2);
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