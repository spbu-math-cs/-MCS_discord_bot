import Utility.Channels
import Utility.Categories
import Utility.clearAndSendMessages
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
    private val starterMessage = "В этом канале в сообщениях ниже поддерживается корректный список курсов. " +
            "С помощью него, вы можете скопировать название курса, для присоединения к нему " +
            "или проверить существование курса, который Вы планируете создать."

    private fun getSubjectList(guild: Guild): List<String> {
        val category = getCategory(Categories.SUBJECTS, guild)

        return listOf("Список курсов:") + category.textChannels.windowed(20, 20, true) {
                window -> window.joinToString("", "```", "```") { "\t${it.name}\n" }
        }
    }

    private lateinit var subjectListChannel: TextChannel
    private lateinit var guild: Guild

    private val resetSubjectList = { clearAndSendMessages(
        subjectListChannel,
        listOf(starterMessage) + getSubjectList(guild)
    ) }

    override fun onGuildReady(event: GuildReadyEvent) {
        guild = event.guild
        guild.channels.filterIsInstance<TextChannel>().forEach {
            it.manager.setName(normalizeChanelName(it.name)).queue()
        }

        subjectListChannel = getChannel(Channels.SUBJECT_LIST, getCategory(Categories.SUBJECT_MANAGEMENT, guild))
        resetSubjectList()
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
        resetSubjectList()
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) = resetSubjectList()

    override fun onChannelUpdateName(event: ChannelUpdateNameEvent) {
        val channel = event.channel
        if (channel !is TextChannel)
            return
        if (!checkAndCorrectChannelName(channel))
            event.oldValue?.let { channel.manager.setName(it).complete() } //TODO(Добавить оповещение пользователю)
        resetSubjectList()
    }
}