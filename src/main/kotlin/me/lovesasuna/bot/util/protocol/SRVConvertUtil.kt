package me.lovesasuna.bot.util.protocol

import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.TextParseException
import org.xbill.DNS.Type

object SRVConvertUtil {
    @Throws(TextParseException::class)
    fun convert(host: String): String? {
        val resultHost: String
        val resultPort: Int
        val records = Lookup("_minecraft._tcp.$host", Type.SRV).run()
        return if (records != null && records.isNotEmpty()) {
            val result = records[0] as SRVRecord
            resultHost = result.target.toString().replaceFirst(Regex("\\.$"), "")
            resultPort = result.port
            "$resultHost:$resultPort"
        } else {
            null
        }
    }
}