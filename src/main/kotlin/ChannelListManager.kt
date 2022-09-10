import Utility.Channels
import Utility.Categories
import Utility.getChannel
import Utility.clearChannel
import Utility.courses
import Utility.getCategory
import Utility.getCourseCategory
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ChannelListManager : ListenerAdapter() {
    private fun channelListSetup(guild: Guild): String {
        val channelList: StringBuilder = StringBuilder("")
        courses.forEachIndexed { i, label ->
            channelList.append("$label:\n")
            val category = getCourseCategory(i + 1, guild)
            category.textChannels.filter {
                it.name != Channels.CHAT.label && it.name != Channels.INFO.label
            }.forEach { channelList.append("\t\t${it.asMention}\n") }
        }

        return channelList.toString()
    }

    private lateinit var channel: TextChannel
    private lateinit var guild: Guild

    private fun sendMessage() {
        val messages = MessageHistory(channel).retrievePast(100).complete()
        when (messages.size) {
            0 -> channel.sendMessage(channelListSetup(guild)).queue()
            1 -> channel.editMessageById(channel.latestMessageId, channelListSetup(guild)).queue()
            else -> {
                clearChannel(channel)
                channel.sendMessage(channelListSetup(guild)).queue()
            }
        }
    }


    override fun onGuildReady(event: GuildReadyEvent) {
        guild = event.guild
        guild.channels.forEach { it.manager.setName(it.name.replace('-','_').trim()).queue() }

        channel = getChannel(Channels.COURSE_LIST.label, getCategory(Categories.COURSE_MANAGEMENT, guild))
        sendMessage()
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        sendMessage()
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        sendMessage()
    }

    override fun onChannelUpdateName(event: ChannelUpdateNameEvent) {
        sendMessage()
    }
}