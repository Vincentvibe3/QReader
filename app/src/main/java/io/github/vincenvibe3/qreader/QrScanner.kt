package io.github.vincenvibe3.qreader

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class QrScanner(val onDetected: (text:String, formatName:String)->Unit):ImageAnalysis.Analyzer {

    private var canAnalyse = true

    private fun process(text:String, format:BarcodeFormat){
        canAnalyse = false
        val formatName = when (format.name){
            "QR_CODE" -> "QR Code"
            "AZTEC" -> "Aztec"
            "CODABAR" -> "Codabar"
            "CODE_128" -> "Code 128"
            "CODE_39" -> "Code 39"
            "CODE_93" -> "Code 93"
            "DATA_MATRIX" -> "Data Matrix"
            "EAN_13" -> "EAN-13"
            "EAN_8" -> "EAN-8"
            "ITF" -> "ITF"
            "MAXICODE" -> "MaxiCode"
            "PDF_417" -> "PDF417"
            "RSS_14" -> "RSS 14"
            "RSS_EXPANDED" -> "RSS Expanded"
            "UPC_A" -> "UPC-A"
            "UPC_E" -> "UPC-E"
            "UPC_EAN_EXTENSION" -> "UPC/EAN"
            else -> "Unknown Format"
        }
        onDetected(text, formatName)
        canAnalyse = true
    }

    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        if (!canAnalyse) {
            image.close()
            return
        }
        val bytes = ByteArray(image.planes[0].buffer.remaining())
        image.planes[0].buffer.get(bytes)
        val binaryBitmap = BinaryBitmap(
            HybridBinarizer(
                PlanarYUVLuminanceSource(
                    bytes,
                    image.width,
                    image.height,
                    image.width / 4,
                    (image.height - image.width / 2) / 2,
                    image.width / 2,
                    image.width / 2,
                    false
                )
            )
        )
        val decodeResult = runCatching {
            reader.decode(
                binaryBitmap, mapOf(
                    DecodeHintType.TRY_HARDER to true,
                    DecodeHintType.ALSO_INVERTED to true
                )
            )
        }.getOrNull()
        image.close()
        if (decodeResult != null) {
            process(decodeResult.text, decodeResult.barcodeFormat)
            Log.i("Scanner", decodeResult.text)
        }
    }

}