package presto;

public class Func {
    public String sig;

    public Func(String sig) {
        this.sig = sig;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Func && sig.equals(((Func) o).sig);
    }

    @Override
    public int hashCode() {
        return sig.hashCode();
    }
}
