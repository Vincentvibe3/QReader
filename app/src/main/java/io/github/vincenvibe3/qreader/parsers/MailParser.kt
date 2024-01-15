package io.github.vincenvibe3.qreader.parsers

import android.net.Uri
import java.net.URI


object MailParser {

    data class MailData(
        val address:String,
        val subject: String?=null,
        val cc:List<String>?=null,
        val bcc:List<String>?=null,
        val body:String?=null
    )

    fun parse(mailToString:String):MailData?{
        val uri = URI(mailToString)
        uri.path
        val data = uri.query.split("&").associate{
            val splitQueryItem = it.split("=")
            Pair(splitQueryItem.first(), splitQueryItem.last())
        }
        val address = data["address"]
        val subject = data["subject"]
        val cc = data["subject"]
        val bcc = data["subject"]
        val body = data["subject"]
        return if (address!=null&&subject!=null&&cc!=null&&bcc!=null&&body!=null) {
            MailData(
                address,
                subject,
                cc.split(","),
                bcc.split(","),
                body
            )
        } else {
            null
        }
    }

}