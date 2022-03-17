package semantic.attrs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeSegment {
    private final List<CodeLine> codeLines;

    private CodeSegment(List<CodeLine> codeLines) {
        this.codeLines = codeLines;
    }

    public static CodeSegment gen(CodeSegment codeSegment, Instruction instruction, Object... args) {
        return new CodeSegment(
                Stream.concat(codeSegment.codeLines.stream(), Stream.of(new CodeLine(instruction, args)))
                        .collect(Collectors.toList())
        );
    }

    public static CodeSegment empty() {
        return new CodeSegment(List.of());
    }

    private static class CodeLine {
        private final Instruction instruction;
        private final Object[] args;

        private CodeLine(Instruction instruction, Object... args) {
            this.instruction = instruction;
            this.args = args;
        }
    }
}
