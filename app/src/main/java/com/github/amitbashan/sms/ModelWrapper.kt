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

class ModelWrapper(val modelBytes: ByteArray, vocab: Map<String, Int>) {
    companion object {
        const val MAX_INPUT_SIZE = 512
    }

    private val tokenizer = WordpieceTokenizer(vocab)
    private val env = OrtEnvironment.getEnvironment()
    private val session = OrtSession.SessionOptions().use { opts ->
        opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
        env.createSession(modelBytes, opts)
    }

    fun predict(message: String): Boolean {
        val tokenizedMessage = tokenizer.tokenizeAndPadWithAttentionMask(message, MAX_INPUT_SIZE)

        if (tokenizedMessage.first.size > MAX_INPUT_SIZE) {
            return false
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

        return logits.argmax() != 0
    }
}