package com.github.amitbashan.sms.tokenization

class CharChecker {
    companion object {
        fun isInvalid(ch: Char): Boolean {
            return (ch == '\u0000' || ch == '\uFFFD')
        }

        fun isControl(ch: Char): Boolean {
            if (ch.isWhitespace()) {
                return false
            }
            val type = Character.getType(ch)
            return (type == Character.CONTROL.toInt() || type == Character.FORMAT.toInt())
        }

        fun isWhitespace(ch: Char): Boolean {
            if (ch.isWhitespace()) {
                return true
            }
            val type = Character.getType(ch)
            return (type == Character.SPACE_SEPARATOR.toInt() ||
                    type == Character.LINE_SEPARATOR.toInt() ||
                    type == Character.PARAGRAPH_SEPARATOR.toInt())
        }

        fun isPunctuation(ch: Char): Boolean {
            val type = Character.getType(ch)
            return (type == Character.CONNECTOR_PUNCTUATION.toInt() ||
                    type == Character.DASH_PUNCTUATION.toInt() ||
                    type == Character.START_PUNCTUATION.toInt() ||
                    type == Character.END_PUNCTUATION.toInt() ||
                    type == Character.INITIAL_QUOTE_PUNCTUATION.toInt() ||
                    type == Character.FINAL_QUOTE_PUNCTUATION.toInt() ||
                    type == Character.OTHER_PUNCTUATION.toInt())
        }
    }
}