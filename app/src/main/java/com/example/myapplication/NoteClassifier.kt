package com.example.myapplication

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max

class NoteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    private lateinit var classes: List<String>
    private lateinit var vocab: List<String>
    private lateinit var wordToId: Map<String, Int>

    private var padId: Int = 0
    private var unkId: Int = 1
    private var seqLen: Int = 16 // будет переопределён из модели

    companion object {
        private const val TAG = "NoteClassifier"

        // ВАЖНО: названия файлов в assets должны совпадать
        private const val MODEL_FILE = "core_model.tflite"
        private const val CLASSES_FILE = "classes.json"
        private const val VOCAB_FILE = "vocab.json"

        // Порог уверенности
        private const val MIN_CONF = 0.55f
    }

    suspend fun init() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Initializing classifier with INT32 model...")
                Log.d(TAG, "Assets: " + context.assets.list("")?.joinToString())

                val model = loadModelFile(MODEL_FILE)

                val options = Interpreter.Options().apply {
                    setNumThreads(max(1, Runtime.getRuntime().availableProcessors() - 1))
                }

                interpreter = Interpreter(model, options)

                val inTensor = interpreter!!.getInputTensor(0)
                val outTensor = interpreter!!.getOutputTensor(0)

                Log.d(TAG, "Input dtype=${inTensor.dataType()} shape=${inTensor.shape().contentToString()}")
                Log.d(TAG, "Output dtype=${outTensor.dataType()} shape=${outTensor.shape().contentToString()}")

                require(inTensor.dataType() == DataType.INT32) {
                    "Model input must be INT32 tokens, but got ${inTensor.dataType()}"
                }
                require(inTensor.shape().size == 2 && inTensor.shape()[0] == 1) {
                    "Model input shape must be [1, seqLen], but got ${inTensor.shape().contentToString()}"
                }
                seqLen = inTensor.shape()[1]

                loadClasses()
                loadVocab()

                Log.d(TAG, "Classifier initialized. seqLen=$seqLen, numClasses=${classes.size}, vocabSize=${vocab.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing classifier", e)
                interpreter?.close()
                interpreter = null
            }
        }
    }

    private fun loadModelFile(assetName: String): MappedByteBuffer {
        context.assets.openFd(assetName).use { afd ->
            FileInputStream(afd.fileDescriptor).use { fis ->
                return fis.channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
            }
        }
    }

    private fun loadAssetText(name: String): String {
        return context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun loadClasses() {
        val json = loadAssetText(CLASSES_FILE)
        classes = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
        Log.d(TAG, "Classes loaded: $classes")
    }

    private fun loadVocab() {
        val json = loadAssetText(VOCAB_FILE)
        vocab = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)

        val map = HashMap<String, Int>(vocab.size)
        vocab.forEachIndexed { index, token -> map[token] = index }
        wordToId = map

        padId = wordToId[""] ?: 0
        unkId = wordToId["[UNK]"] ?: 1

        Log.d(TAG, "Vocab loaded. padId=$padId unkId=$unkId")
    }

    private fun standardize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^\\w\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun tokenizeToIds(text: String): IntArray {
        val standardizedText = standardize(text)
        val words = if (standardizedText.isEmpty()) emptyList() else standardizedText.split(" ")

        val ids = IntArray(seqLen) { padId }
        var count = 0
        for (word in words) {
            if (word.isEmpty()) continue
            ids[count] = wordToId[word] ?: unkId
            count++
            if (count >= seqLen) break
        }
        return ids
    }

    fun classify(text: String): String {
        val localInterpreter = interpreter ?: run {
            Log.e(TAG, "Classifier not initialized.")
            return "other"
        }

        val ids = tokenizeToIds(text)
        val input = arrayOf(ids)
        val output = Array(1) { FloatArray(classes.size) }

        try {
            localInterpreter.run(input, output)
        } catch (e: Exception) {
            Log.e(TAG, "Error running model inference", e)
            return "other"
        }

        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        if (maxIndex == -1) {
            return "other"
        }

        val score = probabilities[maxIndex]
        val category = classes[maxIndex]

        if (score < MIN_CONF) {
            Log.d(TAG, "Predicted category: '$category' with score $score. CONFIDENCE TOO LOW.")
            return "other"
        }

        Log.d(TAG, "Predicted category: '$category' with score $score")
        return category
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
