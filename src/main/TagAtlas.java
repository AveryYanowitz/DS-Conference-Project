package main;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import file_processing.Pair;
import file_processing.ReaderWriter;
import file_processing.Word.Boundary;

public class TagAtlas {
    private Map<String, Set<Pair<String, Double>>> _wordsToTagProbs;
    private Map<String, Set<String>> _legalNextTags;
    private Map <String, String> _tagAbbreviationKey;
    private Map<Boundary, List<Boundary>> _boundaryContours;
    private boolean _verboseTags;

    public TagAtlas() throws ParseException{
        try {
            _verboseTags = !(StringUtil.getYN("Use abbreviated tags?"));
            _wordsToTagProbs = ReaderWriter.importObject(new File("./assets/wordsToTagProbs.ser"));
            _legalNextTags = ReaderWriter.importObject(new File("./assets/legalNextTags.ser"));
            _tagAbbreviationKey = ReaderWriter.getAbbreviationKey("./assets/all_tags.txt", _verboseTags);
            _boundaryContours = getBoundaryContours();
        } catch (Exception e) {
            throw new ParseException(e.getMessage());
        }
        _removeUnusedTags();        
    }

    public Set<Pair<String, Double>> getTagAndProb(String word) {
        return _wordsToTagProbs.get(word);
    }

    public boolean wordExists(String word) {
        return _wordsToTagProbs.containsKey(word);
    }

    public Set<String> getNextTags(String tag) {
        return _legalNextTags.get(tag);
    }

    public boolean isVerbose() {
        return _verboseTags;
    }

    public List<Boundary> getNextBoundaries(Boundary boundary) {
        return _boundaryContours.get(boundary);
    }

    public static Map<Boundary, List<Boundary>> getBoundaryContours() {
        Map<Boundary, List<Boundary>> boundaryContours = new TreeMap<>();
        final Boundary[] start = {Boundary.START};
        final Boundary[] nonStart = {Boundary.MIDDLE, Boundary.END};
        boundaryContours.put(Boundary.START, Arrays.asList(nonStart));
        boundaryContours.put(Boundary.MIDDLE, Arrays.asList(nonStart));
        boundaryContours.put(Boundary.END, Arrays.asList(start));
        return boundaryContours;
    }

    // _tagAbbreviationKey only includes tags that the user specified,
    // so we have to remove the others from the tag-related maps
    private void _removeUnusedTags() {
        if (_verboseTags) {
            MapUtil.filterValues(_wordsToTagProbs, (var tagPair) -> {
                String fullTag = tagPair.first();
                return !_tagAbbreviationKey.containsValue(fullTag);
            });
    
            MapUtil.filterKeys(_legalNextTags, (var entry) -> {
                String fullTag = entry.getKey();
                return !_tagAbbreviationKey.containsValue(fullTag);
            });
    
            MapUtil.filterValues(_legalNextTags, (var nextTag) -> {
                String fullTag = nextTag;
                return !_tagAbbreviationKey.containsValue(fullTag);
            });
        } else {
            MapUtil.filterValues(_wordsToTagProbs, (var tagPair) -> {
                String shortTag = tagPair.first().substring(0,2);
                return !_tagAbbreviationKey.containsKey(shortTag);
            });
    
            MapUtil.filterKeys(_legalNextTags, (var entry) -> {
                String shortTag = entry.getKey().substring(0,2);
                return !_tagAbbreviationKey.containsKey(shortTag);
            });
    
            MapUtil.filterValues(_legalNextTags, (var nextTag) -> {
                String shortTag = nextTag.substring(0, 2);
                return !_tagAbbreviationKey.containsKey(shortTag);
            });
        }
    }
    
}
