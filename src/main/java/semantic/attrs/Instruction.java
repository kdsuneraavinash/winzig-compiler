package semantic.attrs;

import java.util.StringJoiner;

public class Instruction {
    private final String label;
    private final InstructionMnemonic instructionMnemonic;
    private final Object[] args;

    public Instruction(String label, InstructionMnemonic instructionMnemonic, Object... args) {
        this.label = label;
        this.instructionMnemonic = instructionMnemonic;
        this.args = args;
    }

    public Instruction(InstructionMnemonic instructionMnemonic, Object... args) {
        this.label = null;
        this.instructionMnemonic = instructionMnemonic;
        this.args = args;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (label != null) sj.add(label).add(": ");
        sj.add(instructionMnemonic.toString());
        for (Object arg : args) sj.add(arg.toString());
        return sj.toString();
    }
}
