package me.lovesasuna.bot

import me.lovesasuna.bot.file.Config
import me.lovesasuna.bot.listener.EventListener
import me.lovesasuna.bot.listener.FriendMessageListener
import me.lovesasuna.bot.listener.GroupMessageListener
import me.lovesasuna.bot.listener.MemberLeaveListener
import me.lovesasuna.bot.util.BasicUtil
import me.lovesasuna.bot.util.plugin.Logger
import me.lovesasuna.bot.util.plugin.PluginScheduler
import net.mamoe.mirai.Bot
import net.mamoe.mirai.join
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger.getLogger

/**
 * @author LovesAsuna
 */


suspend fun main() {
    getLogger("").level = Level.OFF
    Main.printSystemInfo()
    Main.botConfig = BotConfiguration.Default.also {
        it.randomDeviceInfo()
    }
    Config.writeDefault()
    Logger.log("登陆协议: ${Main.botConfig.protocol}", Logger.LogLevel.CONSOLE)
    Main.bot = Bot(
        Config.data.Account,
        Config.data.Password,
        Main.botConfig
    ).also {
        it.login()
        Main.logger = it.logger
    }

    Main.initListener()

    Runtime.getRuntime().addShutdownHook(Thread {
        Logger.log(Logger.Messages.BOT_SHUTDOWN, Logger.LogLevel.CONSOLE)
    })
    Main.bot.join()
}

object Main {
    lateinit var bot: Bot
    lateinit var botConfig: BotConfiguration
    var logger: MiraiLogger? = null
    val scheduler = PluginScheduler()
    val dataFolder = File("${BasicUtil.getLocation(Main.javaClass).path}${File.separator}Bot")
        .also { if (!it.exists()) Files.createDirectories(Paths.get(it.toURI())) }

    fun initListener() {
        val listenerList = listOf(
            GroupMessageListener, FriendMessageListener, MemberLeaveListener
        )
        listenerList.forEach(EventListener::onAction)
    }

    fun printSystemInfo() {
        val runtimeMX = ManagementFactory.getRuntimeMXBean()
        val osMX = ManagementFactory.getOperatingSystemMXBean()
        if (runtimeMX != null && osMX != null) {
            val javaInfo = "Java " + runtimeMX.specVersion + " (" + runtimeMX.vmName + " " + runtimeMX.vmVersion + ")"
            val osInfo = "Host: " + osMX.name + " " + osMX.version + " (" + osMX.arch + ")"
            println("System Info: $javaInfo $osInfo")
        } else {
            println("Unable to read system info")
        }
    }
}