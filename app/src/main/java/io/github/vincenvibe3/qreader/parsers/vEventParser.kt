package io.github.vincenvibe3.qreader.parsers

object vEventParser {

    private data class Field(val fieldName:String, val params:String, var value:String)

    data class Event(
        val title:String,
        val startTime:String,
        val endTime:String,
        val allDay:Boolean,
        val location:String,
        val description:String,
        val extraEmail:String,
        val rrule:String,
        val private:Boolean,
        val busy:Boolean
    )

    fun parse(data:String):Event?{
        val fields = HashMap<String, Field>()
        val lines = data.split("\n")
        var currentField:Field? = null
        lines.forEach { line ->
            if (line.startsWith(" ")){
                currentField?.value+=line.removePrefix(" ")
            } else if (line.startsWith("\t")){
                currentField?.value+=line.removePrefix("\t")
            }
            else {
                currentField?.let {
                    fields[it.fieldName] = it
                }
                val paramDelimiter = line.indexOfFirst { it==';' }
                val valueDelimiter = line.indexOfFirst { it==':' }
                val hasParams = paramDelimiter<valueDelimiter
                currentField = if (hasParams){
                    Field(
                        line.substring(0, paramDelimiter),
                        line.substring(paramDelimiter, valueDelimiter),
                        line.substring(valueDelimiter)
                    )
                } else {
                    Field(
                        line.substring(0, valueDelimiter),
                        "",
                        line.substring(valueDelimiter)
                    )
                }
            }
            currentField?.let {
                fields[it.fieldName] = it
            }

        }

    }

}