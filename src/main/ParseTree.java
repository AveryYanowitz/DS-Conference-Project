package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import file_reader.CorpusReader.twoMaps;
import main.Tagger.WordAndBound;
import main.Word.Boundary;

public class ParseTree {

    private class WordNode {
        String tag;
        WordNode parent;
        List<WordNode> children;

        WordNode(String thisTag, WordNode parent) {
            children = new ArrayList<>();
            tag = thisTag;
            this.parent = parent;
        }

        boolean isLeaf() {
            return children.isEmpty();
        }

        int numChildren() {
            return children.size();
        }

        void addChild(String s) {
            children.add(new WordNode(s, this));
        }     
    } 

    private WordNode _root;
    private Map<String, Set<String>> _wordsWithTags;
    private Map<String, Set<String>> _legalNextTags;
    public static Map<Boundary,List<Boundary>> legalBoundaryContours = Utilities.getLegalBoundaryContours();

    ParseTree(twoMaps<Word, String, Integer, Set<String>> freqTags) {
        _root = new WordNode(null, null);
        _wordsWithTags = Utilities.extractTags(freqTags.map1());
        _legalNextTags = freqTags.map2();
    }

    // Iterates over the tree, only returns leaf nodes
    private class LeafIter implements Iterator<WordNode> {
        Stack<WordNode> stack;

        LeafIter() {
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
    
    // Adds the word to each current leaf node.
    public boolean add(WordAndBound word) {
        LeafIter iter = new LeafIter();
        WordNode current;
        while (iter.hasNext()) {
            // Increment at the beginning of the loop to
            // skip the first node, the null _root node.
            current = iter.next();
            String lastTag = current.tag;
            Set<String> validTags;

            try {
                validTags = new TreeSet<>(_wordsWithTags.get(word.rawWord()));
            } catch (NullPointerException e) {
                // Thrown if the word doesn't exist in _wordsWithTags
                return false;
            }

            if (lastTag != null) {
                Set<String> possibleNext = _legalNextTags.get(lastTag);
                validTags.retainAll(possibleNext); 
                validTags = _excludeInvalidBoundaries(lastTag, validTags, word.boundary());
            } else {
                // If it's the first word of the sentence, it better be the start of a clause!
                validTags.removeIf((tag) -> Word.getBoundary(tag) != Boundary.START);
            }
            
            if (validTags.size() == 0) {
                _prune(current);
                continue;
            }
            for (String tag : validTags) {
                current.addChild(tag);
            }
        }
        return true;
    }

    // Marks all leaf nodes' boundary field as Boundary.END
    public void makeLeafNodesEnd() {
        LeafIter iter = new LeafIter();
        while (iter.hasNext()) {
            WordNode current = iter.next();
            String partOfSpeech = Word.getPOS(current.tag);
            current.tag = partOfSpeech + ";" + "END";
        }
    }

    // Does not work correctly.
    private String _prune(WordNode leafToPrune) {
        if (!leafToPrune.isLeaf()) {
            throw new IllegalArgumentException("Can only prune starting from leaf node");
        }

        // We only want to remove the unique sentence containing this leaf,
        // so we traverse upwards until we find where that unique branch ends;
        // i.e. where the parent has more than one child
        WordNode current = leafToPrune;
        WordNode previous = null;
        while (current.parent != null && current.parent.numChildren() > 1) {
            previous = current;
            current = current.parent;
        }
        current.children.remove(previous);
        return leafToPrune.tag;
    }

    public Set<String> getSentences() {
        LeafIter iter = new LeafIter();
        Set<String> allSentences = new TreeSet<>();
        while (iter.hasNext()) {
            WordNode word = iter.next();
            StringBuilder sb = new StringBuilder();
            while (word.parent != null) {
                sb.insert(0, " ");
                sb.insert(0, word.tag);
                word = word.parent;
            }
            allSentences.add(sb.toString());
        }
        return allSentences;
    }

    @Override
    public String toString() {
        return getSentences().toString();
    }

    // Goes through the possible tags provided and determines which to consider, given local clause boundaries
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
