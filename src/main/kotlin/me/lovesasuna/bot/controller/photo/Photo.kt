package me.lovesasuna.bot.controller.photo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.lovesasuna.bot.Main
import me.lovesasuna.bot.controller.FunctionListener
import me.lovesasuna.bot.controller.photo.source.PhotoSource
import me.lovesasuna.bot.controller.photo.source.Pixiv
import me.lovesasuna.bot.controller.photo.source.Random
import me.lovesasuna.bot.file.Config
import me.lovesasuna.lanzou.util.NetWorkUtil
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText

class Photo : FunctionListener {
    lateinit var photoSource: PhotoSource
    var random = true
    var pixiv = true
    override suspend fun execute(event: MessageEvent, message: String, image: Image?, face: Face?): Boolean {
        val bannotice = { Main.scheduler.asyncTask { event.reply("该图源已被禁用！") } }
        if (message.startsWith("/色图")) {
            when (message.split(" ")[1]) {
                "pixiv" -> {
                    if (pixiv) {
                        photoSource = Pixiv()
                        val data = photoSource.fetchData()
                        val quota = data?.split("|")!![1]
                        if (quota == "0") {
                            event.reply("达到每日调用额度限制")
                        } else {
                            val url = data.split("|")[0]
                            event.reply(event.uploadImage(NetWorkUtil[url]!!.second) + PlainText("\n剩余次数: $quota"))
                        }
                    } else {
                        bannotice.invoke()
                    }
                }
                "random" -> {
                    if (random) {
                        photoSource = Random()
                        event.reply(event.uploadImage(photoSource.fetchData()?.let { NetWorkUtil[it] }!!.second))
                    } else {
                        bannotice.invoke()
                    }
                }
                "switch" -> {
                    changeBanStatus(event, message)
                }
            }
        }
        return true
    }

    private fun changeBanStatus(event: MessageEvent, message: String) {
        if (Config.data.Admin.contains(event.sender.id)) {
            GlobalScope.async {
                when (message.split(" ")[2]) {
                    "pixiv" -> {
                        event.reply("已${if (pixiv) "禁用" else "解禁"}pixiv图源")
                        pixiv = !pixiv
                    }
                    "random" -> {
                        event.reply("已${if (random) "禁用" else "解禁"}random图源")
                        random = !random
                    }
                }
            }

        }
    }

}