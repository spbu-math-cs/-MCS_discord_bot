import Utility.Channels
import Utility.Categories
import Utility.getChannel
import Utility.clearChannel
import Utility.courses
import Utility.getCategory
import Utility.getCourseCategory
import Utility.normalizeChanelName
import net.dv8tion.jda.api.entities.*
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

    private lateinit var subjectListChannel: TextChannel
    private lateinit var guild: Guild

    private fun sendMessage() {
        val messages = MessageHistory(subjectListChannel).retrievePast(100).complete()
        when (messages.size) {
            0 -> subjectListChannel.sendMessage(channelListSetup(guild)).queue()
            1 -> {
                subjectListChannel.sendMessage("delete").complete()
                clearChannel(subjectListChannel)
                subjectListChannel.sendMessage(channelListSetup(guild)).queue()
            }
            else -> {
                clearChannel(subjectListChannel)
                subjectListChannel.sendMessage(channelListSetup(guild)).queue()
            }
        }
    }


    override fun onGuildReady(event: GuildReadyEvent) {
        guild = event.guild
        guild.channels.filterIsInstance<TextChannel>().forEach { it.manager.setName(normalizeChanelName(it.name)).queue() }

        subjectListChannel = getChannel(Channels.COURSE_LIST.label, getCategory(Categories.COURSE_MANAGEMENT, guild))
        sendMessage()
    }

    private fun checkAndCorrectChannelName(channel: TextChannel): Boolean {
        val correctName = normalizeChanelName(channel.name)
        channel.manager.setName(correctName).complete()
        if (guild.channels.map { it.name }.count{ it == correctName } > 1)
            return false
        return true
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        val channel = event.channel
        if (channel !is TextChannel)
            return
        if (!checkAndCorrectChannelName(channel))
            channel.delete().complete()
        sendMessage()
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        sendMessage()
    }

    override fun onChannelUpdateName(event: ChannelUpdateNameEvent) {
        val channel = event.channel
        if (channel !is TextChannel)
            return
        if (!checkAndCorrectChannelName(channel))
            event.oldValue?.let { channel.manager.setName(it).complete() }
        sendMessage()
    }
}