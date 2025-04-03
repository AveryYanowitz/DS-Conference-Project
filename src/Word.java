public class Word implements Comparable<Word> {
    private String _word;
    private String _tag;
    public Word(String wordWithAnnotation) throws IllegalArgumentException {
        if (!wordWithAnnotation.contains("_")) {
            throw new IllegalArgumentException(wordWithAnnotation + " has no annotation");
        }
        String[] arr = wordWithAnnotation.split("_");
        if (arr.length != 2) {
            throw new IllegalArgumentException("failed to split "+wordWithAnnotation);
        }
        _word = arr[0].toLowerCase();
        _tag = arr[1];
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
        // If they're the same, return zero
        if (other.equals(this)) {
            return 0;
        }
        // If the words are the same, return by tag
        if (other._word.equals(_word)) {
            return other._tag.compareTo(_tag);
        }
        // Otherwise, return by word
        return other._word.compareTo(_word);
    }
}