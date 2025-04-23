package ds_conference;

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
        StringBuilder sb = new StringBuilder();
        sb.append(arr[1]);
        sb.append(";");
        sb.append(clauseBoundary);
        _tag = sb.toString();
    }

    public Word(String word, String tag, Boundary clauseBoundary) {
        _word = word;
        StringBuilder sb = new StringBuilder();
        sb.append(getPOS(tag)); // Remove the boundary from it
        sb.append(";");
        sb.append(clauseBoundary);
        _tag = sb.toString();
    }

    public String getWord() {
        return _word;
    }
    public String getTag() {
        return _tag;
    }
    public Word.Boundary getBoundary() {
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
    public static Word.Boundary getBoundary(String tag) {
        try {
            String boundaryString = tag.split(";")[1];
            if (boundaryString.equals("START")) {
                return Word.Boundary.START;
            } else if (boundaryString.equals("MIDDLE")) {
                return Word.Boundary.MIDDLE;
            } else {
                return Word.Boundary.END;
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