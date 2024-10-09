package com.github.amitbashan.sms.tokenization

class BasicTokenizer {
    companion object {
        fun tokenize(text: String): List<String> {
            val cleanedText = cleanText(text)
            val origTokens = whitespaceTokenize(cleanedText)
            val stringBuilder = StringBuilder()

            for (token in origTokens) {
                val list = runSplitOnPunc(token)

                for (subtoken in list) {
                    stringBuilder.append(subtoken).append(" ")
                }
            }

            return whitespaceTokenize(stringBuilder.toString())
        }

        fun cleanText(text: String): String {
            val builder = StringBuilder("")

            for (i in text.indices) {
                val ch = text[i]

                if (CharChecker.isInvalid(ch) || CharChecker.isControl(ch)) {
                    continue
                }

                if (CharChecker.isWhitespace(ch)) {
                    builder.append(" ")
                } else {
                    builder.append(ch)
                }
            }

            return builder.toString()
        }

        fun whitespaceTokenize(text: String): List<String> {
            return text.split(" ")
        }

        fun runSplitOnPunc(text: String): List<String> {
            val tokens = mutableListOf<String>()
            var startNewWord = false

            for (i in text.indices) {
                val ch = text[i]
                if (CharChecker.isPunctuation(ch)) {
                    tokens += ch.toString()
                    startNewWord = true
                } else {
                    if (startNewWord) {
                        tokens += ""
                        startNewWord = false
                    }

                    tokens[tokens.size - 1] = tokens.last() + ch
                }
            }

            return tokens
        }
    }
}