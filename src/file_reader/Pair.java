package file_reader;

import java.io.Serializable;

public record Pair<A, B>(A first, B second) implements Serializable, Comparable<Pair<A, B>> { 
    public Pair<A, B> replaceFirst(A newVal) {
        return new Pair<>(newVal, second);
    }
    public Pair<A, B> replaceSecond(B newVal) {
        return new Pair<>(first, newVal);
    }
    @Override
    public int compareTo(Pair<A, B> other) {
        if (this == other) {
            return 0;
        }
        String thisFirstString = this.first.toString();
        String otherFirstString = other.first.toString();
        if (thisFirstString.equals(otherFirstString)) {
            String thisSecondString = this.second.toString();
            String otherSecondString = other.second.toString();
            return thisSecondString.compareTo(otherSecondString);
        } else {
            return thisFirstString.compareTo(otherFirstString);
        }
    }
    
}