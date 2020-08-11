package presto;

public class FuncProfile {
    @Deprecated
    long id;
    String sig;
    long count;

    public FuncProfile(long id, String sig, long count) {
        this.id = id;
        this.count = count;
        this.sig = sig;
    }

    public FuncProfile(String sig, long count) {
        this(-1, sig, count);
    }

    @Override
    public int hashCode() {
        return sig.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FuncProfile && sig.equals(((FuncProfile) o).sig);
    }
}
