package com.acadexia.security;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.Toolkit;
import java.util.function.Consumer;

/**
 * Native Swing DocumentFilter that blocks SQL injection characters,
 * dangerous SQL comment syntax, semicolons, and enforces length limits in real time.
 */
public class SqlSanitizerFilter extends DocumentFilter {

    private final int maxLength;
    private final boolean allowSpaces;
    private final Consumer<String> onBlockedAttempt;

    public SqlSanitizerFilter(int maxLength, boolean allowSpaces, Consumer<String> onBlockedAttempt) {
        this.maxLength = maxLength;
        this.allowSpaces = allowSpaces;
        this.onBlockedAttempt = onBlockedAttempt;
    }

    public SqlSanitizerFilter(int maxLength, boolean allowSpaces) {
        this(maxLength, allowSpaces, null);
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        }

        if (isValidInput(fb.getDocument().getText(0, fb.getDocument().getLength()), string, offset, 0)) {
            super.insertString(fb, offset, string, attr);
        } else {
            notifyBlocked(string);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) {
            super.replace(fb, offset, length, null, attrs);
            return;
        }

        if (isValidInput(fb.getDocument().getText(0, fb.getDocument().getLength()), text, offset, length)) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            notifyBlocked(text);
        }
    }

    private boolean isValidInput(String currentText, String incomingText, int offset, int replaceLength) {
        int newLength = currentText.length() - replaceLength + incomingText.length();
        if (maxLength > 0 && newLength > maxLength) {
            return false;
        }

        // Construct prospective full string
        StringBuilder prospective = new StringBuilder(currentText);
        prospective.replace(offset, offset + replaceLength, incomingText);
        String fullString = prospective.toString();

        // Check for disallowed characters: quotes, semicolons, backticks, null bytes
        for (char c : incomingText.toCharArray()) {
            if (c == '\'' || c == '\"' || c == ';' || c == '`' || c == '\0' || c == '\\') {
                return false;
            }
            if (!allowSpaces && Character.isWhitespace(c)) {
                return false;
            }
        }

        // Check for SQL comment tokens or injection keywords
        if (fullString.contains("--") || fullString.contains("/*") || fullString.contains("*/")) {
            return false;
        }

        return !SecurityUtils.containsSqlInjection(fullString);
    }

    private void notifyBlocked(String rejectedSnippet) {
        Toolkit.getDefaultToolkit().beep();
        if (onBlockedAttempt != null) {
            onBlockedAttempt.accept(rejectedSnippet);
        }
    }
}
