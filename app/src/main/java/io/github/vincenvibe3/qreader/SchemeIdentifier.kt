package io.github.vincenvibe3.qreader

object SchemeIdentifier {

    enum class PayloadTypes{
        LINK, WIFI, OTP, PLAIN_TEXT, MAIL
    }

    fun identify(data:String): PayloadTypes {
        return if (data.startsWith("https://")||data.startsWith("http://")){
            PayloadTypes.LINK
        } else if (data.startsWith("WIFI:")){
            PayloadTypes.WIFI
        } else if (data.startsWith("otpauth://")){
            PayloadTypes.OTP
        } else if (data.startsWith("mailto:")) {
            PayloadTypes.MAIL
        } else {
            PayloadTypes.PLAIN_TEXT
        }
    }

}