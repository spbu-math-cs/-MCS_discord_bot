import GlobalLogger.RED
import GlobalLogger.RESET
import GlobalLogger.globalLogger
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.LocalDateTime
import java.time.Month


object Utility: ListenerAdapter() {
    fun Boolean.toInt() = if (this) 1 else 0

    enum class GuildRole(val label: String) {
        REGISTRATION("Регистрация"),
        PROFESSOR("Преподаватель"),
        PROFESSOR_CONFIRMATION("Ожидает подтверждения"),
    }

    enum class StudyDirection(val label: String) {
        MATHEMATICS("М"),
        DATA_SCIENCE("НОД"),
        MODERN_PROGRAMMING("СП");
        companion object {
            private val map = values().associateBy { it.label }
            operator fun get(label: String) = map[label]
        }
    }

    fun getCurrentStudyYear(year: Int) =
        year - (LocalDateTime.now().month < Month.AUGUST).toInt()

    private fun getNumberedCourseName(studyDirection: StudyDirection, courseNumber: Int) : String {
        val studyYearOfAcceptance = getCurrentStudyYear(LocalDateTime.now().year - courseNumber)
        return "${studyDirection.label} $studyYearOfAcceptance-" +
                "${studyYearOfAcceptance + 1}"
    }

    fun getRole(studyDirection: StudyDirection, courseNumber: Int, guild: Guild) : Role {
        return guild.getRolesByName(getNumberedCourseName(studyDirection, courseNumber), false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Role '${getNumberedCourseName(studyDirection, courseNumber)}' " +
                        "was not found! Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    fun getRole(roleEnum: GuildRole, guild: Guild) : Role {
        return guild.getRolesByName(roleEnum.label, false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Role '" + roleEnum.label + "' was not found! " +
                        "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    enum class Categories(val label: String){
        REGISTRATION("Регистрация"),
        ADMINISTRATION("Администрация"),
        SUBJECT_MANAGEMENT("Управление курсами"),
        SUBJECTS("Учебные курсы"),
    }

    fun getCategory(categoryEnum: Categories, guild: Guild): Category {
        return guild.getCategoriesByName(categoryEnum.label, false).firstOrNull()
            ?: let {
                globalLogger.error(RED + "ALARM!!! Category '" + categoryEnum.label + "' was not found! " +
                        "Fix this immediately, or everything will fall down!" + RESET)
                throw Exception()
            }
    }

    enum class Channels(val label: String) {
        REGISTRATION("регистрация"),
        PROFESSOR_CONFIRMATION("подтверждение_роли"),
        CLEANING("удаление_старых_каналов"),
        SUBJECT_LIST("список_курсов"),
        SUBJECT_JOINING("присоединение_к_курсам"),
        SUBJECT_CREATION("создание_курсов"),
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

    fun clearAndSendMessages(channel: TextChannel, messages: List<String>) {
        clearChannel(channel)
        messages.forEach { channel.sendMessage(it).complete() }
    }

    fun normalizeChanelName(name: String) =
        name.replace('-', '_').replace(' ', '_').trim()

    fun expandChannelName(subjectName: String, semesterNumber: Int?) =
        normalizeChanelName(subjectName) + '_' +
                getCurrentStudyYear(LocalDateTime.now().year) + '_' +
                getCurrentStudyYear(LocalDateTime.now().year + 1) +
                when (semesterNumber) {
                    1 -> "_осень"
                    2 -> "_весна"
                    else -> ""
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