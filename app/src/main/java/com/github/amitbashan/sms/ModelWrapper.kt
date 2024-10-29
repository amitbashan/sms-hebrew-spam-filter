package com.github.amitbashan.sms

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtTrainingSession
import com.github.amitbashan.sms.tokenization.WordpieceTokenizer
import org.jetbrains.kotlinx.dl.impl.util.argmax
import org.jetbrains.kotlinx.dl.onnx.inference.OnnxInferenceModel
import org.jetbrains.kotlinx.dl.onnx.inference.executionproviders.ExecutionProvider
import kotlin.math.exp

fun FloatArray.softmaxAt(i: Int): Float {
    val expSum = this.map { exp(it) }.sum()
    return exp(this[i]) / expSum
}

fun FloatArray.softmax(): FloatArray {
    return this.indices.map { this.softmaxAt(it) }.toFloatArray()
}

class ModelWrapper(modelBytes: ByteArray, vocab: Map<String, Int>) {
    companion object {
        const val MAX_INPUT_SIZE = 512
    }

    private val tokenizer = WordpieceTokenizer(vocab)
    private val env = OrtEnvironment.getEnvironment()
    private val session = OrtSession.SessionOptions().use { opts ->
        opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
        env.createSession(modelBytes, opts)
    }

    fun extractHebrew(input: String): String {
        val regex = "[\\u0590-\\u05FF\\d\\W]+".toRegex()
        val matches = regex.findAll(input)
        return matches.joinToString("") { it.value }
    }

    fun predict(input: String): Pair<Boolean, Float> {
        val message = extractHebrew(input)
        val tokenizedMessage = tokenizer.tokenizeAndPadWithAttentionMask(message, MAX_INPUT_SIZE)

        if (tokenizedMessage.first.size > MAX_INPUT_SIZE) {
            return Pair(false, 0F)
        }

        val output = session.run(
            mapOf(
                "messageInput" to OnnxTensor.createTensor(
                    env,
                    arrayOf(tokenizedMessage.first)
                ),
                "attentionMask" to OnnxTensor.createTensor(
                    env,
                    arrayOf(tokenizedMessage.second)
                ),
            )
        )
        val prediction = output.get("prediction")!!.get() as OnnxTensor
        val logits = prediction.floatBuffer.array().softmax()

        return Pair(logits.argmax() != 0, logits[1])
    }
}