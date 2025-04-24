package main;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

import main.Word.Boundary;
import file_reader.CorpusReader.twoMaps;
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
        twoMaps<Word, String, Integer, Set<String>> freqTags;

        try {
            System.out.println(new File(".").getAbsolutePath());
            freqTags = Serializer.importObject(new File("./assets/maps.ser"));
        } catch (IOException io) {
            io.printStackTrace();
            return;
        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
            return;
        }

        do {
            // Get a sentence from a user, take the words, and turn them into WordAndBound records
            String sentence = _getString("Enter a sentence to tag: ");
            String[] rawWords = sentence.split(" ");
            List<WordAndBound> wordList = _addClauseContours(rawWords);
            
            // Take each word in wordList in order and use it to update the ParseTree
            ParseTree allParses = new ParseTree(freqTags);
            for (WordAndBound word : wordList) {
                allParses.add(word);
            }
            System.out.println(allParses);

        } while (_getYN("Add another sentence?"));
        System.out.println("Thanks for stopping by!");
    }

    private static List<WordAndBound> _addClauseContours(String[] rawWords) {        
        List<WordAndBound> wordList = new ArrayList<>();
        boolean startOfClause = true;
        for (String wordString : rawWords) {
            WordAndBound newWord;
            // Punctuation marks (other than apostrophes and quotes)
            // usually mean the start of a new clause.
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