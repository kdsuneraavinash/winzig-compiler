package lexer;


import diagnostics.TextHighlighter;

import java.util.Arrays;

public class CharReader implements TextHighlighter {
    private final char[] charBuffer;
    private final int charBufferLength;
    private int markStartOffset;
    private int offset;

    private CharReader(char[] buffer) {
        this.charBuffer = buffer;
        this.charBufferLength = buffer.length;
        this.markStartOffset = 0;
        this.offset = 0;
    }

    public static CharReader from(String text) {
        return new CharReader(text.toCharArray());
    }

    public char peek() {
        if (offset < charBufferLength) {
            return charBuffer[offset];
        }
        return Character.MAX_VALUE;
    }

    public char peek(int k) {
        int kOffset = offset + k;
        if (kOffset < charBufferLength) {
            return charBuffer[kOffset];
        }
        return Character.MAX_VALUE;
    }

    public void advance() {
        offset++;
    }

    public void advance(int k) {
        offset += k;
    }

    public void startMarking() {
        markStartOffset = offset;
    }

    public String getMarkedChars() {
        return new String(Arrays.copyOfRange(charBuffer, markStartOffset, offset));
    }

    public boolean isEOF() {
        return offset >= charBufferLength;
    }

    public int getMarkStartOffset() {
        return markStartOffset;
    }

    public int getOffset() {
        return offset;
    }

    public String highlightedSegment(int startOffset, int endOffset) {
        int prevCrOffset, nextCrOffset;
        for (prevCrOffset = startOffset; prevCrOffset >= 0; prevCrOffset--) {
            if (charBuffer[prevCrOffset] == '\n') break;

        }
        for (nextCrOffset = endOffset; nextCrOffset < charBufferLength; nextCrOffset++) {
            if (charBuffer[nextCrOffset] == '\n') break;
        }
        StringBuilder codeSb = new StringBuilder();
        StringBuilder highlightSb = new StringBuilder();
        for (int i = prevCrOffset + 1; i < nextCrOffset; i++) {
            codeSb.append(charBuffer[i]);
            if (i >= startOffset && i < endOffset) {
                highlightSb.append('^');
            } else {
                highlightSb.append(' ');
            }
        }
        return codeSb.append('\n').append(highlightSb).toString();
    }
}
