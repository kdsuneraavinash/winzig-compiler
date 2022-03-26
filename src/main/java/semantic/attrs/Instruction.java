package semantic.attrs;

import java.util.StringJoiner;

public class Instruction {
    private Label label;
    private final InstructionMnemonic instructionMnemonic;
    private final Object[] args;

    public Instruction(Label label, InstructionMnemonic instructionMnemonic, Object... args) {
        this.label = label;
        this.instructionMnemonic = instructionMnemonic;
        this.args = args;
    }

    public Instruction(InstructionMnemonic instructionMnemonic, Object... args) {
        this.label = null;
        this.instructionMnemonic = instructionMnemonic;
        this.args = args;
    }

    public Label getLabel() {
        if (this.label == null) this.label = new Label();
        return this.label;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (label != null) sj.add(label.getLabel());
        sj.add("\t");
        sj.add(instructionMnemonic.toString());
        for (Object arg : args) sj.add(arg.toString());
        return sj.toString();
    }
}
