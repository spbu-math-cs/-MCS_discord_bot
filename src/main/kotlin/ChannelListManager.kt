import Utility.BasicChannels
import Utility.BasicCategories
import Utility.channelGetter
import Utility.clearChannel
import Utility.courses
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ChannelListManager : ListenerAdapter() {

    @Synchronized
    private fun channelListSetup(guild: Guild): String {

        val channelList: StringBuilder = StringBuilder("")
        for (i in 0..3) {
            channelList.append(courses[i] + ":\n")
            val category =
                guild.getCategoriesByName(courses[i], true).first()
                    ?: return ""// Если этой категории нет, дело труба...
            category.textChannels.filter {
                it.name != BasicChannels.CHAT.channel
                        && it.name != BasicChannels.INFO.channel
                        && it.name != BasicChannels.ARCHIVE.channel
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

        channel = channelGetter(guild, BasicCategories.COURSE_MANAGEMENT.category, BasicChannels.COURSE_LIST.channel) // обработка если канал не найден
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