package io.github.vincenvibe3.qreader.parsers

object WifiParser {

    data class WifiData(
        val ssid:String?,
        val auth:String?,
        val password:String?,
        val hidden:Boolean?,
        val EAP:String?,
        val anon:String?,
        val identity:String?,
        val phase2Method:String?
    )

    fun parse(data:String): WifiData {
        // Parse for sections of payload
        val payload = data.removePrefix("WIFI:")
        val sections = arrayListOf<String>()
        var current = ""
        var escaped = false
        for (c in payload){
            if (!escaped && c == ';'){
                sections.add(current)
                current = ""
                continue
            }
            if (c == '\\' && !escaped){
                escaped = true
            } else {
                current+=c
                escaped = false
            }
        }
        //Add last section that may not be closed by a semicolon
        sections.add(current)

        //Separate sections by type
        val prefixes = listOf("T", "S", "P", "H", "E", "A", "I", "PH2")
        val fieldMap = mutableMapOf<String, String>()
        sections.forEach{
            val prefix = prefixes.firstOrNull { prefix -> it.startsWith(prefix) }
            if (prefix!=null){
                val content = it.removePrefix("$prefix:")
                if ((content.lowercase() != "true" || content.lowercase() != "false") && prefix == "H"){
                    // Set as Phase 2 method
                    fieldMap["PH2"] = content
                } else {
                    fieldMap[prefix] = content
                }
            }
        }

        return WifiData(
            fieldMap.getOrDefault("S", null),
            fieldMap.getOrDefault("T", null),
            fieldMap.getOrDefault("P", null),
            fieldMap.getOrDefault("H", "false").lowercase() != "false",
            fieldMap.getOrDefault("E", null),
            fieldMap.getOrDefault("A", null),
            fieldMap.getOrDefault("I", null),
            fieldMap.getOrDefault("PH2", null),
        )

    }
}