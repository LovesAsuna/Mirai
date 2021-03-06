package me.lovesasuna.bot.controller.qqfun

import me.lovesasuna.bot.Main
import me.lovesasuna.bot.data.BotData
import me.lovesasuna.lanzou.util.OkHttpUtil
import me.lovesasuna.bot.util.registerDefaultPermission
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object Nbnhhsh : SimpleCommand(
    owner = Main,
    primaryName = "nbnhhsh",
    description = "能不能好好说话?",
    parentPermission = registerDefaultPermission()
) {
    @Handler
    suspend fun CommandSender.handle(abbreviation: String) {
        val text = BotData.objectMapper.createObjectNode().put("text", abbreviation)
        sendMessage(
            "可能的结果: ${
                OkHttpUtil.postJson(
                    "https://lab.magiconch.com/api/nbnhhsh/guess",
                    text.toString().toRequestBody("application/json".toMediaType())
                )[0]["trans"] ?: "[]"
            }"
        )
    }
}