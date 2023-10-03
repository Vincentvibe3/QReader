package io.github.vincentvibe3.authenticator.scanner

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

class QrScanner(val onDetected: (text:String)->Unit):ImageAnalysis.Analyzer {

    private var canAnalyse = true

    private fun process(text:String){
        canAnalyse = false
        onDetected(text)
        canAnalyse = true
    }

    private val reader = QRCodeReader()

    override fun analyze(image: ImageProxy) {
        if (!canAnalyse){
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
                    image.width/4,(image.height-image.width/2)/2,image.width/2, image.width/2, false
                )
            )
        )

        val decodeResult = runCatching{
            reader.decode(binaryBitmap, mapOf(
                DecodeHintType.TRY_HARDER to true,
            ))
        }.getOrNull()
        image.close()
        if (decodeResult != null) {
            process(decodeResult.text)
            Log.i("Scanner", decodeResult.text)
        }
    }


}