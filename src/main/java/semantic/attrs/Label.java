package semantic.attrs;

import java.util.Objects;

public class Label {
    private static int labelCount = 0;

    private int index;

    public Label() {
        this.index = labelCount++;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label)) return false;
        Label label = (Label) o;
        return index == label.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        return "L" + index;
    }
}
