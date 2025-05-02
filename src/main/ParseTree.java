package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import file_processing.Pair;
import file_processing.Word;
import file_processing.Word.Boundary;
import main.Tagger.WordAndBound;

public class ParseTree implements Iterable<Pair<String, Double>> {
    
    private class WordNode {
        Word word;
        WordNode parent;
        
        List<WordNode> children;
        double probability;

        WordNode(String rawWord, String thisTag, WordNode parent, double prob) {
            children = new ArrayList<>();
            word = new Word(rawWord, thisTag);
            this.parent = parent;
            if (parent == null) {
                probability = prob;
            } else {
                probability = parent.probability * prob;
            }
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

        void addChild(String rawWord, String tag, double probability) {
            children.add(new WordNode(rawWord, tag, this, probability));
        }
    } 
    
    private WordNode _root;
    private TagAtlas _tagAtlas;

    ParseTree(TagAtlas tagAtlas) throws ParseException {
        // Since the first word could have multiple parsings, the root node
        // has to be a null placeholder node instead
        _root = new WordNode(null, null,null,1);
        _tagAtlas = tagAtlas;
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
    
    // Iterates over the sentences in the tree, returns pairs that link
    // sentences to their total probability, calculated as the product of
    // each word-tag combo's probability
    public class SentenceIter implements Iterator<Pair<String, Double>> {
        private PriorityQueue<Pair<String, Double>> sentences;

        SentenceIter() {
            sentences = new PriorityQueue<>((x, y) -> y.second().compareTo(x.second()));
            sentences.addAll(getSentences());
        }

        @Override
        public boolean hasNext() {
            return sentences.size() > 0;
        }

        @Override
        public Pair<String, Double> next() {
            Pair<String, Double> rawPair = sentences.poll();
            // No real significance to 15, other than that it made the numbers the prettiest
            double normalizedFreq = _nthRoot(rawPair.second(), 30);
            return rawPair.replaceSecond(normalizedFreq);
        }

        private double _nthRoot(double base, int n) {
            return Math.pow(base, 1.0/n);
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
            Set<Pair<String, Double>> validTags;

            try {
                validTags = new TreeSet<>(_tagAtlas.getTagAndProb(word.rawWord()));
            } catch (NullPointerException e) { // word not in _wordsToTagProbs

                throw new IllegalArgumentException("No legal tags left for word '"+word.rawWord()+"'");
            }

            if (lastTag != null) {
                Set<String> possibleNext = _tagAtlas.getNextTags(lastTag);
                validTags.removeIf((Pair<String, Double> pair) -> {
                    String tag = pair.first();
                    return !possibleNext.contains(tag);
                }); 
                validTags = _excludeInvalidBoundaries(lastTag, word.boundary(), validTags);
            } else {
                // If it's the first word of the sentence, it better be the start of a clause!
                validTags.removeIf((tag) -> Word.getBoundary(tag.first()) != Boundary.START);
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
            for (Pair<String,Double> tagPair : validTags) {
                String tag = tagPair.first();
                double probability = tagPair.second();
                current.addChild(rawWord, tag, probability);
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

    public Set<Pair<String, Double>> getSentences() {
        LeafIter iter = new LeafIter();
        Set<Pair<String, Double>> allSentences = new TreeSet<>();
        while (iter.hasNext()) {
            WordNode word = iter.next();
            double probability = word.probability;
            StringBuilder sb = new StringBuilder();
            while (word.parent != null) {
                sb.insert(0, " ");
                sb.insert(0, word.getTag());
                word = word.parent;
            }
            allSentences.add(new Pair<>(sb.toString(), probability));
        }
        return allSentences;
    }

    @Override
    public String toString() {
        Set<Pair<String, Double>> sentences = getSentences();
        StringBuilder sb = new StringBuilder();
        for (Pair<String, Double> sentence : sentences) {
            sb.append(sentence.first());
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
    private Set<Pair<String, Double>> _excludeInvalidBoundaries (String lastTag, Boundary wordBound,
                                                            Set<Pair<String, Double>> possibilities) {
        Set<Pair<String, Double>> prunedPossibilities = new TreeSet<>(possibilities);
        Boundary lastBoundary = Word.getBoundary(lastTag);
        var followingBounds = _tagAtlas.getNextBoundaries(lastBoundary);
        for (Pair<String, Double> tagPair : possibilities) {
            String tag = tagPair.first();
            Boundary tagBoundary = Word.getBoundary(tag);
            if (tagBoundary != wordBound || !followingBounds.contains(tagBoundary)) {
                prunedPossibilities.remove(tagPair);
            }
        }
        return prunedPossibilities;
    }

    @Override
    public Iterator<Pair<String, Double>> iterator() {
        return new SentenceIter();
    }

}
