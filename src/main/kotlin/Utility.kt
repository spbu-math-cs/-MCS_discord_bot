import GlobalLogger.RED
import GlobalLogger.RESET
import GlobalLogger.globalLogger
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object Utility: ListenerAdapter() {
    enum class Roles(val label: String) {
        REGISTRATION("Регистрация"),
        PROFESSOR("Преподаватель"),
        PROFESSOR_CONFIRMATION("Ожидает подтверждения"),
    }

    val courses: List<String> = listOf("СП 1", "СП 2", "СП 3", "СП 4")

    fun getRole(roleEnum: Roles, guild: Guild) : Role {
        return guild.getRolesByName(roleEnum.label, false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Role '" + roleEnum.label + "' was not found! " +
                        "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    fun getCourseRole(courseNumber: Int, guild: Guild) : Role {
        return guild.getRolesByName(courses[courseNumber - 1], false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Role 'СП " + courseNumber + "' was not found! " +
                        "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    enum class Categories(val label: String){
        REGISTRATION("Регистрация"),
        ADMINISTRATION("Администрация"),
        COURSE_MANAGEMENT("Управление курсами")
    }

    fun getCategory(categoryEnum: Categories, guild: Guild): Category {
        return guild.getCategoriesByName(categoryEnum.label, false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Category '" + categoryEnum.label + "' was not found! " +
                            "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    fun getCourseCategory(courseNumber: Int, guild: Guild) : Category {
        return guild.getCategoriesByName(courses[courseNumber - 1], false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Category 'СП " + courseNumber + "' was not found! " +
                            "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    enum class Channels(val label: String) {
        REGISTRATION("регистрация"),
        PROFESSOR_CONFIRMATION("подтверждение_роли"),
        COURSE_LIST("список_курсов"),
        COURSE_INTERACTION("взаимодействие_с_курсами"),
        INFO("стойка_информации_и_полезные_ссылки"),
        CHAT("болталка")
    }

    fun getChannel(channel: Channels, category: Category): TextChannel {
        return category.textChannels.find { it.name == channel.label }
            ?: let {
                globalLogger.error(RED + "ALARM!!! Channel" + channel.label + "' was not found! " +
                            "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    //Очистка чата

    fun clearChannel(channel: TextChannel) {
        var deletingFlag = true
        val history = MessageHistory(channel)
        while (deletingFlag) {
            val messages = history.retrievePast(30).complete()
            if (messages.size > 1)
                channel.deleteMessages(messages).queue()
            else
                deletingFlag = false
        }
    }

    fun normalizeChanelName(name: String): String {
        return name.replace('-', '_').replace(' ', '_').trim()
    }

    fun sendMessageAndDeferReply(event: Event, text: String) {
        when (event) {
            is ButtonInteractionEvent -> {
                event.deferReply(true).queue()
                event.hook
            }
            is ModalInteractionEvent -> {
                event.deferReply(true).queue()
                event.hook
            }
            else -> null
        }?.sendMessage(text)?.setEphemeral(true)?.complete()
    }
}