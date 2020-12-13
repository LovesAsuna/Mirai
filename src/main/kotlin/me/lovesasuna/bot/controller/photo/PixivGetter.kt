package me.lovesasuna.bot.controller.photo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.lovesasuna.bot.Main
import me.lovesasuna.bot.controller.FunctionListener
import me.lovesasuna.bot.data.BotData
import me.lovesasuna.bot.util.BasicUtil
import me.lovesasuna.lanzou.util.NetWorkUtil
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.Image
import java.io.InputStream

class PixivGetter : FunctionListener {


    @ExperimentalCoroutinesApi
    override suspend fun execute(event: MessageEvent, message: String, image: Image?, face: Face?): Boolean {
        when {
            message.startsWith("/pixiv work ") -> {
                val ID = BasicUtil.extractInt(message.split(" ")[2])
                var reader = NetWorkUtil["https://api.imjad.cn/pixiv/v1/?type=illust&id=$ID"]!!.second.bufferedReader()
                var root = BotData.objectMapper.readTree(reader.readLine())
                val status = root["status"].asText()
                if (BotData.debug) event.reply("R级检测响应: $status")
                var count = 1
                if (status == "failure") {
                    event.reply("查询图片信息失败，跳过R级检测...")
                } else {
                    val tags = root["response"][0]["tags"].toString()
                    count = root["response"][0]["page_count"].asInt()
                    if (BotData.debug) event.reply(tags)
                    if (tags.contains(Regex("R-[1-9]+"))) {
                        event.reply("图片含有R18内容,禁止显示！")
                        return false
                    }
                }

                event.reply("获取中,请稍后..")
                reader = NetWorkUtil.post(
                    "https://api.pixiv.cat/v1/generate", "p=$ID".toByteArray(),
                    arrayOf("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                )!!.second.bufferedReader()
                root = BotData.objectMapper.readTree(reader.readLine())
                val list = root.get("original_url") ?: root.get("original_urls")
                if (list == null) {
                    event.reply("该作品不存在或已被删除!")
                    return false
                }
                val size = list.size()
                var originInputStream: InputStream?
                if (size == 0) {
                    if (BotData.debug) event.reply("尝试复制IO流")
                    Main.scheduler.withTimeOut(suspend {
                        originInputStream =
                            NetWorkUtil["https://api.kuku.me/pixiv/picbyurl?url=${list.asText()}"]!!.second
                        val uploadImage = event.uploadImage(originInputStream!!)
                        event.reply(uploadImage)
                        event.reply("获取完成!")
                    }, 60 * 1000) {
                        event.reply("图片获取失败,大概率是服务器宽带问题或图片过大，请捐赠支持作者")
                    }
                } else {
                    event.reply("该作品共有${count}张图片")
                    repeat(size) {
                        originInputStream =
                            NetWorkUtil["https://api.kuku.me/pixiv/picbyurl?url=${list[it].asText()}"]!!.second
                        event.reply(event.uploadImage(originInputStream!!))
                    }
                    event.reply("获取完成!")
                }
            }
            message.contains("i.pximg.net") -> {
                event.reply(event.uploadImage(NetWorkUtil[message.replace("i.pximg.net", "/i.pixiv.cat")]!!.second))
            }
        }
        return true
    }


}