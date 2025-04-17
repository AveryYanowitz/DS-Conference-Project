public class Word implements Comparable<Word> {
    public static enum Boundary {START, END, NONE};
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
        sb.append(tag);
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