import Utility.Channels
import Utility.Categories
import Utility.clearAndSendMessage
import Utility.getChannel
import Utility.getCategory
import Utility.normalizeChanelName
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ChannelListManager : ListenerAdapter() {
    private fun getSubjectList(guild: Guild): String {
        val subjectList: StringBuilder = StringBuilder("Список курсов:\n")
        val category = getCategory(Categories.SUBJECTS, guild)
        category.textChannels.forEach { subjectList.append("\t\t${it.asMention}\n") }
        return subjectList.toString()
    }

    private lateinit var subjectListChannel: TextChannel
    private lateinit var guild: Guild

    override fun onGuildReady(event: GuildReadyEvent) {
        guild = event.guild
        guild.channels.filterIsInstance<TextChannel>().forEach {
            it.manager.setName(normalizeChanelName(it.name)).queue()
        }

        subjectListChannel = getChannel(Channels.SUBJECT_LIST, getCategory(Categories.SUBJECT_MANAGEMENT, guild))
        clearAndSendMessage(subjectListChannel, getSubjectList(guild))
    }

    private fun checkAndCorrectChannelName(channel: TextChannel): Boolean {
        val correctName = normalizeChanelName(channel.name)
        channel.manager.setName(correctName).complete()
        if ((channel.parentCategory?.channels?.map { it.name }?.count { it == correctName } ?: 2) > 1)
            return false
        return true
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        val channel = event.channel
        if (channel !is TextChannel)
            return
        if (!checkAndCorrectChannelName(channel))
            channel.delete().complete() //TODO(Добавить оповещение пользователю)
        clearAndSendMessage(subjectListChannel, getSubjectList(guild))
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        clearAndSendMessage(subjectListChannel, getSubjectList(guild))
    }

    override fun onChannelUpdateName(event: ChannelUpdateNameEvent) {
        val channel = event.channel
        if (channel !is TextChannel)
            return
        if (!checkAndCorrectChannelName(channel))
            event.oldValue?.let { channel.manager.setName(it).complete() } //TODO(Добавить оповещение пользователю)
        clearAndSendMessage(subjectListChannel, getSubjectList(guild))
    }
}