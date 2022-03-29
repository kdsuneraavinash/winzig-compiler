package diagnostics;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public abstract class DiagnosticCollector implements TextHighlighter {
    private final List<String> diagnostics;

    protected DiagnosticCollector() {
        this.diagnostics = new ArrayList<>();
    }

    public void addError(Highlightable highlightable, String message, Object... args) {
        String errorMessage = String.format(message, args);
        String highlighted = highlightedSegment(highlightable);
        if (highlighted.isEmpty()) {
            diagnostics.add(errorMessage);
        } else {
            diagnostics.add(String.format("%s\n%s", errorMessage, highlighted));
        }
    }

    public String collectErrors() {
        StringJoiner sj = new StringJoiner("\n");
        diagnostics.forEach(sj::add);
        return sj.toString();
    }

    public boolean hasErrors() {
        return !diagnostics.isEmpty();
    }
}
