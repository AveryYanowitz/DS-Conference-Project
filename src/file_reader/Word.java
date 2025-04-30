package file_reader;

import java.io.Serializable;

public class Word implements Comparable<Word>, Serializable {
    public static enum Boundary {START, END, MIDDLE};
    private String _word;
    private String _tag;

    public Word(String wordWithAnnotation, Boundary clauseBoundary) throws IllegalArgumentException {
        if (!wordWithAnnotation.contains("_")) {
            throw new IllegalArgumentException(wordWithAnnotation + " has no annotation");
        }
        String[] arr = wordWithAnnotation.split("_");
        if (arr.length != 2) {
            throw new IllegalArgumentException("failed to split "+wordWithAnnotation);
        }
        _word = arr[0].toLowerCase();
        String fullTag = arr[1];
        // Some tags have extra info after the '-', but it was utterly
        // unhelpful for this application, so I'm removing it here.
        String trimmed = fullTag.split("-")[0];
        StringBuilder sb = new StringBuilder();
        sb.append(trimmed);
        sb.append(";");
        sb.append(clauseBoundary);
        _tag = sb.toString();
    }

    public Word(String word, String tag, Boundary clauseBoundary) {
        _word = word;
        StringBuilder sb = new StringBuilder();
        sb.append(getPOS(tag)); // Remove the boundary from it if present
        sb.append(";");
        sb.append(clauseBoundary);
        _tag = sb.toString();
    }

    public Word(String word, String tagWithBoundary) {
        _word = word;
        _tag = tagWithBoundary;
    }

    public String getWord() {
        return _word;
    }
    public String getTag() {
        return _tag;
    }
    public Boundary getBoundary() {
        return getBoundary(_tag);
    }
    public String getPOS() {
        return getPOS(_tag);
    }

    @Override
    public String toString() {
        return _word + " - " + _tag;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof Word) {
            Word otherWord = (Word) other;
            if (_word.equalsIgnoreCase(otherWord._word) &&
            _tag.equalsIgnoreCase(otherWord._tag)) {
                return true;
            }    
        }
        return false;
    }

    @Override
    public int compareTo(Word other) {
        if (other.equals(this)) {
            return 0;
        }
        if (!other._word.equals(_word)) {
            return other._word.compareTo(_word);
        }
        return other._tag.compareTo(_tag);
    }

    // Returns the "word boundary" chunk of the tag
    public static Boundary getBoundary(String tag) {
        try {
            String boundaryString = tag.split(";")[1];
            if (boundaryString.equals("START")) {
                return Boundary.START;
            } else if (boundaryString.equals("MIDDLE")) {
                return Boundary.MIDDLE;
            } else {
                return Boundary.END;
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            return null; // No boundary found in string
        }
    }

    // Returns the "part of speech" chunk of the tag
    public static String getPOS(String tag) {
        return tag.split(";")[0];
    }
}