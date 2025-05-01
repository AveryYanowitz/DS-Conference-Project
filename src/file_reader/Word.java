// A mostly-immutable object that has tracks its word, 

package file_reader;
import java.io.Serializable;

public class Word implements Comparable<Word>, Serializable {
    public static enum Boundary {START, END, MIDDLE};
    private final String _WORD;
    private final String _TAG;
    private int _count;

    public Word(String wordWithAnnotation, Boundary clauseBoundary) throws IllegalArgumentException {
        _count = 1;

        if (!wordWithAnnotation.contains("_")) {
            throw new IllegalArgumentException(wordWithAnnotation + " has no annotation");
        }
        String[] arr = wordWithAnnotation.split("_");
        if (arr.length != 2) {
            throw new IllegalArgumentException("failed to split "+wordWithAnnotation);
        }
        _WORD = arr[0].toLowerCase();
        String fullTag = arr[1];
        // Some tags have extra info after the '-', but it was utterly
        // unhelpful for this application, so I'm removing it here.
        String trimmed = fullTag.split("-")[0];
        StringBuilder sb = new StringBuilder();
        sb.append(trimmed);
        sb.append(";");
        sb.append(clauseBoundary);
        _TAG = sb.toString();
    }

    public Word(String word, String tag, Boundary clauseBoundary) {
        _WORD = word;
        _count = 1;
        StringBuilder sb = new StringBuilder();
        sb.append(getPOS(tag)); // Remove the boundary from it if present
        sb.append(";");
        sb.append(clauseBoundary);
        _TAG = sb.toString();
    }

    public Word(String word, String tagWithBoundary) {
        _WORD = word;
        _TAG = tagWithBoundary;
        _count = 1;
    }

    public String getWord() {
        return _WORD;
    }
    public String getTag() {
        return _TAG;
    }
    public Boundary getBoundary() {
        return getBoundary(_TAG);
    }
    public String getPOS() {
        return getPOS(_TAG);
    }
    public int getCount() {
        return _count;
    }

    public void incrementCount() {
        _count++;
    }
    public void decrementCount() {
        _count--;
    }

    @Override
    public String toString() {
        return _WORD + " - " + _TAG;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof Word) {
            Word otherWord = (Word) other;
            if (_WORD.equalsIgnoreCase(otherWord._WORD) &&
            _TAG.equalsIgnoreCase(otherWord._TAG)) {
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
        if (!other._WORD.equals(_WORD)) {
            return other._WORD.compareTo(_WORD);
        }
        return other._TAG.compareTo(_TAG);
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