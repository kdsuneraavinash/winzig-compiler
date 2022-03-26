package semantic.attrs;

public class Label {
    private static int labelCount = 0;

    private final int index;

    public Label() {
        this.index = labelCount++;
    }

    public String getLabel() {
        return "L" + index;
    }
}
