package lexer;


import java.util.Arrays;

public class CharReader {
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
}
