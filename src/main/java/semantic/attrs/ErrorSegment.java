package semantic.attrs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ErrorSegment {
    private final List<String> errorMessages;

    private ErrorSegment(List<String> codeLines) {
        this.errorMessages = codeLines;
    }

    public static ErrorSegment gen(ErrorSegment errorSegment, String message) {
        return new ErrorSegment(
                Stream.concat(errorSegment.errorMessages.stream(), Stream.of(message))
                        .collect(Collectors.toList())
        );
    }

    public static ErrorSegment empty() {
        return new ErrorSegment(List.of());
    }
}
