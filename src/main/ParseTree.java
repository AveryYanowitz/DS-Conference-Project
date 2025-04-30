package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import file_reader.Word;
import file_reader.Word.Boundary;
import main.Tagger.WordAndBound;

public class ParseTree {

    private class WordNode {
        Word word;
        WordNode parent;
        List<WordNode> children;

        WordNode(String rawWord, String thisTag, WordNode parent) {
            children = new ArrayList<>();
            word = new Word(rawWord, thisTag);
            this.parent = parent;
        }

        String getWord() {
            return word.getWord();
        }

        String getTag() {
            return word.getTag();
        }

        void setTag(String newTag) {
            word = new Word(word.getWord(), newTag);
        }

        boolean isLeaf() {
            return children.isEmpty();
        }

        int numChildren() {
            return children.size();
        }

        void addChild(String rawWord, String tag) {
            children.add(new WordNode(rawWord, tag, this));
        }     
    } 

    private WordNode _root;
    private Map<String, Set<String>> _wordsWithTags;
    private Map<String, Set<String>> _legalNextTags;
    public static Map<Boundary,List<Boundary>> legalBoundaryContours = MapUtil.getLegalBoundaryContours();

    ParseTree(Map<String, Set<String>> wordsWithTags, Map<String, Set<String>> legalNextTags) {
        // Since the first word could have multiple parsings, the root node
        // has to be a null placeholder node instead
        _root = new WordNode(null, null,null);
        _wordsWithTags = wordsWithTags;
        _legalNextTags = legalNextTags;
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
    public void add(WordAndBound word) throws IllegalArgumentException {
        LeafIter iter = new LeafIter();
        WordNode current;
        while (iter.hasNext()) {
            // Increment at the beginning of the loop to
            // skip the first node, the null _root node.
            current = iter.next();
            String lastTag = current.getTag();
            Set<String> validTags;

            try {
                validTags = new TreeSet<>(_wordsWithTags.get(word.rawWord()));
            } catch (NullPointerException e) {
                // Thrown if the word doesn't exist in _wordsWithTags
                throw new IllegalArgumentException("No legal tags left for word "+word.rawWord());
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
                try {
                    _prune(current.parent, current);
                } catch (NullPointerException e) {
                    throw new IllegalArgumentException("No legal sentences possible");
                }
                continue;
            }

            String rawWord = word.rawWord();
            for (String tag : validTags) {
                current.addChild(rawWord, tag);
            }
        }
    }

    // Marks all leaf nodes' boundary field as Boundary.END
    public void makeLeafNodesEnd() {
        LeafIter iter = new LeafIter();
        while (iter.hasNext()) {
            WordNode current = iter.next();
            String partOfSpeech = Word.getPOS(current.getTag());
            current.setTag(partOfSpeech + ";" + "END");
        }
    }

    public Set<String> getSentences() {
        LeafIter iter = new LeafIter();
        Set<String> allSentences = new TreeSet<>();
        while (iter.hasNext()) {
            WordNode word = iter.next();
            StringBuilder sb = new StringBuilder();
            while (word.parent != null) {
                sb.insert(0, " ");
                sb.insert(0, word.getTag());
                word = word.parent;
            }
            allSentences.add(sb.toString());
        }
        return allSentences;
    }

    @Override
    public String toString() {
        Set<String> sentences = getSentences();
        StringBuilder sb = new StringBuilder();
        for (String sentence : sentences) {
            sb.append(sentence);
            sb.append("\n");
        }
        sb.append(_getWords());
        return sb.toString();
    }

    // Does not work correctly.
    private void _prune(WordNode evilParent, WordNode problemChild) {
        // We only want to remove the unique sentence containing this leaf,
        // so we traverse upwards until we find where that unique branch ends;
        // i.e. where the parent has more than one child, or where its parent
        // is the placeholder node _root
        if (evilParent.numChildren() > 1 || evilParent.parent == _root) {
            evilParent.children.remove(problemChild);
        } else {
            _prune(evilParent.parent, evilParent);
        }

    }

    private String _getWords() {
        StringBuilder sb = new StringBuilder();
        WordNode current = _root;
        while (!current.isLeaf()) {
            current = current.children.get(0); // Do this first because _root isn't printable
            sb.append(current.getWord());
            sb.append(" ");
        }
        return sb.toString();
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
