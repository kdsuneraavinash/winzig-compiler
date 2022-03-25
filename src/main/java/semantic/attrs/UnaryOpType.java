package semantic.attrs;

public enum UnaryOpType {
    UNOT("UNOT"), // not(X)
    UNEG("UNEG"), // -(X)
    USUCC("USUCC"), // X+1
    UPRED("UPRED"); // X-1

    private final String name;

    UnaryOpType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
