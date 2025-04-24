package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import file_reader.CorpusReader.threeMaps;
import main.Tagger.WordAndBound;
import main.Word.Boundary;

public class ParseTree {

    private class WordNode {
        String data;
        List<WordNode> children;

        WordNode(String s) {
            children = new ArrayList<>();
            data = s;
        }

        boolean isLeaf() {
            return children.isEmpty();
        }

        void addChild(String s) {
            children.add(new WordNode(s));
        }

        void addChild(WordNode wnd) {
            children.add(wnd);
        }
        
    } 

    private WordNode _root;
    private Map<String, Set<String>> _wordsWithTags;
    private Map<String, Set<String>> _legalLastTags;
    private Map<String, Set<String>> _legalNextTags;
    public static Map<Boundary,List<Boundary>> legalBoundaryContours = Utilities.getLegalBoundaryContours();

    ParseTree(threeMaps<Word, String, String, Integer, Set<String>, Set<String>> freqTags) {
        _root = new WordNode(null);
        _wordsWithTags = Utilities.extractTags(freqTags.map1());
        _legalLastTags = freqTags.map2();
        _legalNextTags = freqTags.map3();
    }

    private class PostIter implements Iterator<WordNode> {
        Stack<WordNode> stack;

        PostIter() {
            stack = new Stack<>();
            stack.add(_root);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public WordNode next() {
            if(hasNext()){
                WordNode current = stack.pop();
                while (!current.isLeaf()) { 
                    for (WordNode child : current.children) {
                        stack.push(child);
                    }
                    current = stack.pop();
                }
                return current;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public void add(WordAndBound word) {
        PostIter iter = new PostIter();
        while (iter.hasNext()) {
            WordNode currentLeaf = iter.next();
            String lastTag = currentLeaf.data;
            // The intersection of this word's tags and all the tags that can come after current.data
            Set<String> validTags = Utilities.intersection(_legalNextTags.get(lastTag), 
                                                                    _wordsWithTags.get(word.rawWord()));
            validTags = _excludeInvalidBoundaries(lastTag, validTags, word.boundary());
            for (String tag : validTags) {
                currentLeaf.addChild(tag);
            }
        }
    }

    // Goes through the possible tags provided and determines which to consider based on local clause boundaries
    private static Set<String> _excludeInvalidBoundaries (String lastTag, Set<String> possibilities, 
                                                            Boundary wordBound) {
        Set<String> prunedPossibilities = new TreeSet<>(possibilities);
        Boundary lastBoundary = Word.getBoundary(lastTag);
        var followingBounds = legalBoundaryContours.get(lastBoundary);
        for (String tag : possibilities) {
            Boundary tagBoundary = Word.getBoundary(tag);
            if (tagBoundary != wordBound || !followingBounds.contains(tagBoundary)) {
                prunedPossibilities.remove(tag);
            }
        }
        return prunedPossibilities;
    }
}
