import GlobalLogger.RED
import GlobalLogger.RESET
import GlobalLogger.globalLogger
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


object Utility: ListenerAdapter() {
    enum class GuildRole(val label: String) {
        REGISTRATION("Регистрация"),
        PROFESSOR("Преподаватель"),
        PROFESSOR_CONFIRMATION("Ожидает подтверждения"),
    }

    enum class StudyDirection(val label: String) {
        MATHEMATICS("Математика"),
        DATA_SCIENCE("НОД"),
        MODERN_PROGRAMMING("СП")
    }

    fun getRole(roleEnum: GuildRole, guild: Guild) : Role {
        return guild.getRolesByName(roleEnum.label, false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Role '" + roleEnum.label + "' was not found! " +
                        "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    private fun getNumberedCourseName(studyDirection: StudyDirection, courseNumber: Int) : String =
        studyDirection.label + ' ' + courseNumber.toString()

    fun getCourseRole(studyDirection: StudyDirection, courseNumber: Int, guild: Guild) : Role {
        return guild.getRolesByName(getNumberedCourseName(studyDirection, courseNumber), false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Role '${getNumberedCourseName(studyDirection, courseNumber)}' " +
                        "was not found! Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    enum class Categories(val label: String){
        REGISTRATION("Регистрация"),
        ADMINISTRATION("Администрация"),
        COURSE_MANAGEMENT("Управление курсами"),
        SPECIAL_SUBJECTS("Спецкурсы")
    }

    fun getCategory(categoryEnum: Categories, guild: Guild): Category {
        return guild.getCategoriesByName(categoryEnum.label, false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Category '" + categoryEnum.label + "' was not found! " +
                        "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    fun getCourseCategory(studyDirection: StudyDirection, courseNumber: Int, guild: Guild) : Category {
        return guild.getCategoriesByName(getNumberedCourseName(studyDirection, courseNumber), false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Category '${getNumberedCourseName(studyDirection, courseNumber)}' " +
                        "was not found! Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    enum class Channels(val label: String) {
        REGISTRATION("регистрация"),
        PROFESSOR_CONFIRMATION("подтверждение_роли"),
        SUBJECT_LIST("список_курсов"),
        SUBJECT_INTERACTION("взаимодействие_с_курсами"),
        INFO("стойка_информации_и_полезные_ссылки"),
        CHAT("болталка"),
        SUBJECT_JOIN("присоединение_к_курсам"),
        SPECIAL_SUBJECT_LIST("список_спецкурсов"),
        INVITE_GENERATOR("генератор_ссылок")
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
            when (messages.size) {
                0 -> deletingFlag = false
                1 -> {
                    deletingFlag = false
                    messages[0]?.delete()?.complete()
                }
                else -> channel.deleteMessages(messages).queue()
            }
        }
    }

    fun clearAndSendMessage(channel: TextChannel, message: String) {
        clearChannel(channel)
        channel.sendMessage(message).queue()
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