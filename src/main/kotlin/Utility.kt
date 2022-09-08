import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel

object Utility {

    enum class Roles(val roleName: String) {
        REGISTRATION("Регистрация"),
        PROFESSOR("Преподаватель")
    }

    enum class BasicChannels(val channel: String) {
        REGISTRATION("регистрация"),
        COURSE_LIST("список-курсов"),
        COURSE_INTERACTION("взаимодействие-с-курсами"),
        INFO("стойка-информации"),
        CHAT("болталка"),
        ARCHIVE("архив-курса")
    }

    enum class BasicCategories(val category:String){
        REGISTRATION("Регистрация"),
        COURSE_MANAGEMENT("Управление курсами"),
    }

    val courses: List<String> = listOf("СП 1", "СП 2", "СП 3", "СП 4")

    //Очистка чата
    fun clearChannel(channel: TextChannel) {
        var deletingFlag = true
        val history = MessageHistory(channel)
        while (deletingFlag) {
            val messages = history.retrievePast(100).complete()
            if (messages.size > 1)
                channel.deleteMessages(messages).complete()
            else
                deletingFlag = false
        }
    }

    fun channelGetter(guild: Guild, categoryName: String, channelName: String): TextChannel {
        val category = guild.getCategoriesByName(categoryName, true).firstOrNull() ?:
            guild.createCategory(categoryName).complete()//логгер
        return category.textChannels.find { it.name == channelName } ?: //привести к нормальному виду перед этим
            category.createTextChannel(channelName).complete()
    }
}