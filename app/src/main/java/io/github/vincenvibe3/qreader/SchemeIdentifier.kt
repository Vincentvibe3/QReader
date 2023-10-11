package io.github.vincenvibe3.qreader

object SchemeIdentifier {

    enum class PayloadTypes{
        LINK, WIFI, OTP, PLAIN_TEXT
    }

    fun identify(data:String): PayloadTypes {
        return if (data.startsWith("https://")||data.startsWith("http://")){
            PayloadTypes.LINK
        } else if (data.startsWith("WIFI:")){
            PayloadTypes.WIFI
        } else if (data.startsWith("otpauth://")){
            PayloadTypes.OTP
        } else {
            PayloadTypes.PLAIN_TEXT
        }
    }

}