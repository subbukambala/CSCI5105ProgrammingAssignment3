// Code taken from : http://groups.google.com/group/comp.lang.java.help/browse_thread/thread/f8b63fc645c1b487/1d94be050cfc249b
import java.io.Serializable;

public class Pair <T, U> implements Serializable
{
    private final T first;
    private final U second;
    private transient final int hash;

    public Pair(T f, U s) {
        this.first = f;
        this.second = s;
        hash = (first == null ? 0 : first.hashCode() * 31)
                + (second == null ? 0 : second.hashCode());
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public T fst() {
        return getFirst();
    }

    public U snd() {
        return getSecond();
    }

    public int hashCode() {
        return hash;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth == null || !(getClass().isInstance(oth))) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Pair<T, U> other = getClass().cast(oth);
        return (first == null ? other.first == null : first.equals(other.first))
                && (second == null ? other.second == null : second.equals(other.second));
    }

} 