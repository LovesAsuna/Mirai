package me.lovesasuna.bot.util.pictureSearch

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import me.lovesasuna.bot.Config
import me.lovesasuna.lanzou.util.NetWorkUtil

object Saucenao : PictureSearchSource {
    private val api =
        "https://saucenao.com/search.php?db=999&output_type=2&testmode=1&api_key=${Config.SauceNaoAPI}&numres=16&url="
    private val mapper = ObjectMapper()

    override fun search(url: String): List<Result> {
        val inputStream = NetWorkUtil[api + url]?.second ?: return emptyList()
        val results = mapper.readTree(inputStream)["results"]
        val resultList = ArrayList<Result>()
        for (i in 0..results.size()) {
            val result = results[i]
            if (result != null) {
                val similarity = result["header"]["similarity"].asInt()
                if (similarity < 57.5) continue
                val extUrlsList = ArrayList<String>()
                val exeUrls: JsonNode? = result["data"]["ext_urls"]
                if (exeUrls != null) {
                    repeat(exeUrls.size()) {
                        extUrlsList.add(result["data"]["ext_urls"][it].asText())
                    }
                }
                if (!extUrlsList.parallelStream().anyMatch { it.contains("pixiv") }) continue
                val thumbnail = result["header"]["thumbnail"].asText()
                val memberName = result["data"]["member_name"].asText()
                resultList.add(Result(similarity, thumbnail, extUrlsList, memberName))
            }
        }

        return resultList
    }

}