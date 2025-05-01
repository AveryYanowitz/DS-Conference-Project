package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

import file_reader.Word.Boundary;
import file_reader.Pair;
import file_reader.Serializer;

public class Tagger {
    record WordAndBound (String rawWord, Boundary boundary) { 
        public WordAndBound(String rawWord, Boundary boundary) {
            this.rawWord = StringUtil.stripNonAlpha(rawWord.toLowerCase());
            this.boundary = boundary;
        }
    }
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Pair<Map<String, Set<Pair<String, Integer>>>,
            Map<String, Set<String>>> freqTags;
        List<String> tagsToKeep;
        try {
            tagsToKeep = _getTags("./assets/all_tags.txt");
            freqTags = Serializer.importObject(new File("./assets/maps.ser"));
        } catch (IOException io) {
            io.printStackTrace();
            return;
        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
            return;
        }

        var wordsToTagFreqs = freqTags.first();
        MapUtil.filterValues(wordsToTagFreqs, (var tagPair) -> {
            String fullTag = tagPair.first();
            String shortTag = fullTag.substring(0,2);
            return !tagsToKeep.contains(shortTag);
        });

        Map<String, Set<String>> legalNextTags = freqTags.second();
        MapUtil.filterKeys(legalNextTags, tagsToKeep);
        MapUtil.filterValues(legalNextTags, (var nextTag) -> {
            String shortTag = nextTag.substring(0, 2);
            return !tagsToKeep.contains(shortTag);
        });

        if (StringUtil.getYN("Parse custom sentences?")) {
            do {
                // Get a sentence from a user, take the words, and turn them into WordAndBound records
                String sentence = StringUtil.getString("Enter a sentence to tag: ");
                _parse(sentence, wordsToTagFreqs, legalNextTags);
            } while (StringUtil.getYN("Add another sentence?"));
            System.out.println("Thanks for stopping by!");
        } else {
            Scanner fileScanner;

            try {
                fileScanner = new Scanner(new File("./assets/test_sentences.txt"));
            } catch (FileNotFoundException e) {
                System.out.println("ERROR: File not found.");
                return;
            }

            while (fileScanner.hasNext()) {
                String sentence = fileScanner.nextLine();
                System.out.println("Parsing: '"+sentence+"'");
                _parse(sentence, wordsToTagFreqs, legalNextTags);
                StringUtil.getString("Press 'Enter' to continue");
                System.out.println();
            }
            System.out.println("That's all, folks!");
        }
        
    }

    private static ParseTree _parse(String sentence, Map<String, Set<Pair<String, Integer>>> wordsToTagFreqs, Map<String, Set<String>> legalNextTags) {
        String[] rawWords = sentence.split(" ");
        List<WordAndBound> wordList = _addClauseContours(rawWords);
        
        // Take each word in wordList in order and use it to update the ParseTree
        ParseTree allParses = new ParseTree(wordsToTagFreqs, legalNextTags);
        boolean successful = true;
        for (WordAndBound word : wordList) {
            try {
                allParses.add(word);
            } catch (IllegalArgumentException error) {                
                System.out.println("Unable to parse sentence '" + sentence+"'");
                System.out.println(error.getMessage());
                successful = false;
                break;
            }
            if (word.boundary() == Boundary.END) {
                allParses.makeLeafNodesEnd();
            }
        }
        if (successful) {                
            System.out.println(allParses.toString());
        }
        System.out.println();
        return allParses;
    }

    private static List<WordAndBound> _addClauseContours(String[] rawWords) {        
        List<WordAndBound> wordList = new ArrayList<>();
        boolean startOfClause = true;
        for (String wordString : rawWords) {
            WordAndBound newWord;
            // Punctuation marks (other than apostrophes and quotes)
            // usually mean the start of a new clause.
            if (StringUtil.hasNonAlpha(wordString)) {
                startOfClause = true;
                newWord = new WordAndBound(StringUtil.stripNonAlpha(wordString), Boundary.END);
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

    private static List<String> _getTags(String pathname) throws FileNotFoundException {
        String[] tagOptions = new String[35];
        Scanner scanner = new Scanner(new File(pathname));
        int index = 0;
        while (scanner.hasNextLine()) {
            tagOptions[index] = scanner.nextLine();
            index++;
        }
        scanner.close();
        
        List<String> tagsToKeep = StringUtil.getList("Which tags do you want to include?", tagOptions);
        return tagsToKeep;
    }

}