public class Word implements Comparable<Word> {
    public static enum Boundary {START, END, NONE};
    private String _word;
    private String _tag;
    private Boundary _clauseBound;

    public Word(String wordWithAnnotation, Boundary clauseBoundary) throws IllegalArgumentException {
        if (!wordWithAnnotation.contains("_")) {
            throw new IllegalArgumentException(wordWithAnnotation + " has no annotation");
        }
        String[] arr = wordWithAnnotation.split("_");
        if (arr.length != 2) {
            throw new IllegalArgumentException("failed to split "+wordWithAnnotation);
        }
        _word = arr[0].toLowerCase();
        _tag = arr[1];
        _clauseBound = clauseBoundary;
    }

    public Word(String word, String tag, Boundary clauseBoundary) {
        _word = word;
        _tag = tag;
        _clauseBound = clauseBoundary;
    }

    public String getWord() {
        return _word;
    }
    public String getTag() {
        return _tag;
    }
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", _word, _tag, _clauseBound);
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

    public int compareTo(Word other) {
        if (other.equals(this)) {
            return 0;
        }
        if (!other._word.equals(_word)) {
            return other._word.compareTo(_word);
        }
        return other._tag.compareTo(_tag);
    }
}