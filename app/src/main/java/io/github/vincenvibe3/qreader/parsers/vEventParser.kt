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
        var vEventStarted = false
        lines.forEach { line ->
            if (line.startsWith(" ")){
                currentField?.value+=line.removePrefix(" ")
            } else if (line.startsWith("\t")){
                currentField?.value+=line.removePrefix("\t")
            }
            else {
                if (vEventStarted) {
                    currentField?.let {
                        fields[it.fieldName] = it
                    }
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
                val field = currentField
                if (field!=null&&field.fieldName == "BEGIN"&&field.value=="VEVENT"){
                    vEventStarted = true
                }
            }
            if (vEventStarted) {
                currentField?.let {
                    fields[it.fieldName] = it
                }
            }
            val title = fields["SUMMARY"]
            fields["LOCATION"]
            fields["DESCRIPTION"]
            fields["DTSTART"]
            fields["DTEND"]
            fields["CLASS"]
            fields["RRULE"]
            fields["TRANSPARENT"]
            fields["ATTENDEE"]

        }
        return null
    }



    fun getAttendees(fieldString:String){

    }

    fun checkAllDay(start:String, end:String):Boolean  {
        val startDay = start.substring(start.indices.last-2, start.indices.last)
        val endDay = start.substring(end.indices.last-2, end.indices.last)
        return startDay.toInt()==endDay.toInt()-1
    }

}