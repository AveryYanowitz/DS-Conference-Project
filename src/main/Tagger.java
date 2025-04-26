package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

import main.Word.Boundary;
import file_reader.CorpusReader.TwoMaps;
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
        TwoMaps<String, String, Set<String>, Set<String>> freqTags;
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

        Map<String, Set<String>> wordsWithTags = freqTags.map1();
        MapUtil.filterValues(wordsWithTags, tagsToKeep);

        Map<String, Set<String>> legalNextTags = freqTags.map2();
        MapUtil.filterKeys(legalNextTags, tagsToKeep);
        MapUtil.filterValues(legalNextTags, tagsToKeep);

        if (StringUtil.getYN("Parse custom sentences?")) {
            do {
                // Get a sentence from a user, take the words, and turn them into WordAndBound records
                String sentence = StringUtil.getString("Enter a sentence to tag: ");
                _parse(sentence, wordsWithTags, legalNextTags);
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
                _parse(sentence, wordsWithTags, legalNextTags);
                StringUtil.getString("Press 'Enter' to continue");
                System.out.println();
            }
            System.out.println("That's all, folks!");
        }
        
    }

    private static ParseTree _parse(String sentence, Map<String, Set<String>> wordsWithTags, Map<String, Set<String>> legalNextTags) {
        String[] rawWords = sentence.split(" ");
        List<WordAndBound> wordList = _addClauseContours(rawWords);
        
        // Take each word in wordList in order and use it to update the ParseTree
        ParseTree allParses = new ParseTree(wordsWithTags, legalNextTags);
        boolean successful = true;
        for (WordAndBound word : wordList) {
            if (!allParses.add(word)) { // returns false when word doesn't exist in corpus
                System.out.println("Unable to parse sentence " + sentence + ":\n"+word.rawWord()+" not in dictionary");
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