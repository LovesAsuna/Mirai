package me.lovesasuna.bot.listener

import me.lovesasuna.bot.Main
import me.lovesasuna.bot.controller.FunctionListener
import me.lovesasuna.bot.file.Config
import me.lovesasuna.bot.util.interfaces.EventListener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.Image

object FriendMessageListener : EventListener {
    private val listeners: MutableList<FunctionListener> = ArrayList()

    init {
        val listenersClass = arrayOf<Class<*>>(

        )

        listenersClass.forEach { c -> listeners.add(c.getConstructor().newInstance() as FunctionListener) }
    }

    override fun onAction() {
        Main.bot.subscribeAlways(FriendMessageEvent::class) {
            val senderID = sender.id
            if (Config.data.Admin.contains(senderID)) {
                return@subscribeAlways
            }
            listeners.forEach {
                Main.scheduler.asyncTask {
                    it.execute(this, this.message.contentToString(), this.message[Image], this.message[Face])
                }
            }
        }
    }
}