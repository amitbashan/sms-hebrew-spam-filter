package com.github.amitbashan.sms.tokenization

fun <T> List<T>.padList(with: T, size: Int): List<T> {
    if (this.size < size) {
        val fillSize = size - this.size
        return this + List(fillSize) { with }
    } else {
        return this
    }
}

class WordpieceTokenizer(val dic: Map<String, Int>) {
    val UNKNOWN_TOKEN = "[UNK]"
    val PAD_TOKEN = "[PAD]"
    val CLS_TOKEN = "[CLS]"
    val SEP_TOKEN = "[SEP]"
    val MAX_INPUTCHARS_PER_WORD = 100

    fun tokenizeAndPadWithAttentionMask(text: String, pad: Int): Pair<LongArray, LongArray> {
        val padNum = dic[PAD_TOKEN]
        val tokens = tokenizeAsNums(text).padList(padNum, pad).map { it!!.toLong() }.toLongArray()
        val attentionMask = tokens.map {
            if (it == 0L) {
                0L
            } else {
                1L
            }
        }.toLongArray()
        return Pair(tokens, attentionMask)
    }

    fun tokenizeAsNums(text: String): List<Int> {
        val clsNum = dic[CLS_TOKEN]!!
        val sepNum = dic[SEP_TOKEN]!!
        return listOf(clsNum) + tokenize(text).map {
            dic[it]!!
        } + sepNum
    }

    fun tokenize(text: String): List<String> {
        val outputTokens = mutableListOf<String>()

        for (token in BasicTokenizer.whitespaceTokenize(text)) {
            if (token.length > MAX_INPUTCHARS_PER_WORD) {
                outputTokens += UNKNOWN_TOKEN
                continue
            }

            var isBad = false
            var start = 0
            val subTokens = mutableListOf<String>()

            while (start < token.length) {
                var curSubStr = ""
                var end = token.length

                while (start < end) {
                    val subStr = if (start == 0) {
                        token.substring(start, end)
                    } else {
                        "##" + token.substring(start, end)
                    }

                    if (dic.containsKey(subStr)) {
                        curSubStr = subStr
                        break
                    }

                    end--
                }

                if ("" == curSubStr) {
                    isBad = true
                    break
                }

                subTokens.add(curSubStr)
                start = end
            }

            if (isBad) {
                outputTokens.add(UNKNOWN_TOKEN)
            } else {
                outputTokens.addAll(subTokens)
            }
        }

        return outputTokens
    }
}