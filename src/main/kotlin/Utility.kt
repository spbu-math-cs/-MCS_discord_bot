import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.hooks.ListenerAdapter

object Utility: ListenerAdapter() {
    enum class Roles(val label: String) {
        REGISTRATION("Регистрация"),
        PROFESSOR("Преподаватель")
    }

    val courses: List<String> = listOf("СП 1", "СП 2", "СП 3", "СП 4")

    fun getRole(roleEnum: Roles, guild: Guild) : Role {
        return guild.getRolesByName(roleEnum.label, false).firstOrNull()
            ?: throw Exception() //логгер
    }

    fun getCourseRole(courseNumber: Int, guild: Guild) : Role {
        return guild.getRolesByName(courses[courseNumber - 1], false).firstOrNull()
            ?: throw Exception() //логгер
    }

    enum class Categories(val label: String){
        REGISTRATION("Регистрация"),
        COURSE_MANAGEMENT("Управление курсами"),
    }

    fun getCategory(categoryEnum: Categories, guild: Guild): Category {
        return guild.getCategoriesByName(categoryEnum.label, false).firstOrNull()
            ?: throw Exception() //логгер
    }

    fun getCourseCategory(courseNumber: Int, guild: Guild) : Category {
        return guild.getCategoriesByName(courses[courseNumber - 1], false).firstOrNull()
            ?: throw Exception() //логгер
    }

    enum class Channels(val label: String) {
        REGISTRATION("регистрация"),
        COURSE_LIST("список_курсов"),
        COURSE_INTERACTION("взаимодействие_с_курсами"),
        INFO("стойка_информации_и_полезные_ссылки"),
        CHAT("болталка")
    }

    fun getChannel(channelName: String, category: Category): TextChannel {
        return category.textChannels.find { it.name == channelName }
            ?: throw Exception() //логгер
    }

    //Очистка чата

    fun clearChannel(channel: TextChannel) {
        var deletingFlag = true
        val history = MessageHistory(channel)
        while (deletingFlag) {
            val messages = history.retrievePast(100).complete()
            if (messages.size > 1)
                channel.deleteMessages(messages).queue()
            else
                deletingFlag = false
        }
    }
}