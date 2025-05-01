package main;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;

import file_processing.Pair;
import file_processing.Word.Boundary;

import java.util.Scanner;

public class Tagger {
    record WordAndBound (String rawWord, Boundary boundary) { 
        public WordAndBound(String rawWord, Boundary boundary) {
            this.rawWord = StringUtil.stripNonAlpha(rawWord.toLowerCase());
            this.boundary = boundary;
        }
    }
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        if (StringUtil.getYN("Parse custom sentences?")) {
            do {
                // Get a sentence from a user, take the words, and turn them into WordAndBound records
                String sentence = StringUtil.getString("Enter a sentence to tag: ");
                _parse(sentence);
            } while (StringUtil.getYN("Add another sentence?"));
            System.out.println("Thanks for stopping by!");
        } else {
            Scanner fileScanner;
            try {
                fileScanner = new Scanner(new File("./assets/test_sentences.txt"));
            } catch (FileNotFoundException e) {
                System.out.println("ERROR: File containing test sentences not found.");
                return;
            }

            double minProb = StringUtil.getDouble("Input minimum probability for sentences to be included.", 0, 1);
            int numToPrint = StringUtil.getInt("Max number to print?");
            while (fileScanner.hasNext()) {
                String sentence = fileScanner.nextLine();
                System.out.println("Parsing: '"+sentence+"'");
                ParseTree allParses = _parse(sentence);
                if (allParses != null) {
                    boolean printed = false;
                    int numPrinted = 0;
                    for (Pair<String, Double> parse : allParses) {
                        if (parse.second() > minProb && numPrinted < numToPrint) {
                            if (!printed) {
                                printed = true;
                                System.out.println("Sentences in order of probability:");
                            }
                            System.out.println(StringUtil.formatParse(parse));
                            numPrinted++;
                        } else {
                            break;
                        }
                    }
                    if (!printed) {
                        System.out.println("No sufficiently probable sentences found.");
                    }
                }
                StringUtil.getString("Press 'Enter' to continue");
                System.out.println();
            }
            System.out.println("That's all, folks!");
        }
        
    }

    private static ParseTree _parse(String sentence, TagAtlas tagAtlas) {
        String[] rawWords = sentence.split(" ");
        List<WordAndBound> wordList = _addClauseContours(rawWords);
        
        // Take each word in wordList in order and use it to update the ParseTree
        ParseTree allParses;
        try {
            allParses = new ParseTree(tagAtlas);
        } catch (ParseException p) {
            System.out.println(p.getMessage());
            return null;
        }

        boolean successful = true;
        for (WordAndBound word : wordList) {
            try {
                allParses.add(word);
            } catch (IllegalArgumentException error) {                
                System.out.println("Unable to parse sentence '" + sentence+"'");
                System.out.println(" "+error.getMessage());
                successful = false;
                break;
            }
            if (word.boundary() == Boundary.END) {
                allParses.makeLeafNodesEnd();
            }
        }
        if (successful) {
            return allParses;
        } else {
            return null;
        }
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

}