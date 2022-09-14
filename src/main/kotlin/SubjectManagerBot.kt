import GlobalLogger.RED
import GlobalLogger.RESET
import GlobalLogger.globalLogger
import GlobalLogger.logButtonInteractionEnter
import GlobalLogger.logButtonInteractionLeave
import GlobalLogger.logFunctionEnter
import GlobalLogger.logFunctionLeave
import GlobalLogger.logModalInteractionEnter
import GlobalLogger.logModalInteractionLeave
import Utility.clearChannel
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import Utility.Channels
import Utility.Categories
import Utility.getChannel
import Utility.courses
import Utility.getCategory
import Utility.getCourseCategory
import Utility.normalizeChanelName
import Utility.sendMessageAndDeferReply

class SubjectManagerBot : ListenerAdapter() {
    private fun sendCreateAndJoin(): List<Button> {
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("create", "Создать"))
        buttons.add(Button.primary("join", "Присоединиться"))
        return buttons
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName,this.javaClass.name)

        val guild = event.guild
        val channelInteraction = getChannel(
            Channels.COURSE_INTERACTION,
            getCategory(Categories.COURSE_MANAGEMENT, guild)
        )

        clearChannel(channelInteraction)

        channelInteraction.sendMessage(
            "Этот чат предназначен для создания каналов для курсов " +
                    "и присоединения к уже существующим курсам."
        ).queue()
        channelInteraction.sendMessage(
            "Ознакомиться с полным списком курсов " +
                    "Вы можете в соседнем канале: " +
                    getChannel(Channels.COURSE_LIST,
                        getCategory(Categories.COURSE_MANAGEMENT, guild)
                    ).asMention
        ).queue()
        channelInteraction.sendMessage("Вы хотите:").setActionRow(sendCreateAndJoin()).queue()

        logFunctionLeave(Throwable().stackTrace[0].methodName,this.javaClass.name)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId !in listOf("create", "join"))
            return

        logButtonInteractionEnter(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.componentId,
            event.user.asTag
        )

        val courseNumber = TextInput.create("courseNumber", "Course number", TextInputStyle.SHORT)
            .setRequiredRange(1, 1)
            .setPlaceholder("1")
            .build()

        val subjectName = TextInput.create("subjectName", "Subject name", TextInputStyle.SHORT)
            .setRequiredRange(1, 100)
            .setPlaceholder("Теоретическая информатика (практика)")
            .build()

        when (event.componentId) {
            "create" -> {
                val subjectCreation = Modal.create("subject create", "Subject creation modal")
                    .addActionRows(ActionRow.of(courseNumber), ActionRow.of(subjectName))
                    .build()

                event.replyModal(subjectCreation).queue()
            }
// Если введён не тот курс в reply дописать, что названия курсов можно найти там-то, там-то
            "join" -> {
                val subjectJoin = Modal.create("subject join", "Subject join modal")
                    .addActionRows(ActionRow.of(courseNumber), ActionRow.of(subjectName))
                    .build()

                event.replyModal(subjectJoin).queue()
            }
        }
        logButtonInteractionLeave(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.componentId,
            event.user.asTag
        )
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId !in listOf("subject create", "subject join"))
            return

        logModalInteractionEnter(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.modalId,
            event.user.asTag
        )

        val member = event.member  ?: let {
            sendMessageAndDeferReply(
                event, "Wrong accept confirmation: " +
                        "there is no member in processing event.\n " +
                        "Please, tell dummy programmers about that, and they will definitely fix that."
            )
            globalLogger.error(RED + "Member was not found in event " +
                    "in ${Throwable().stackTrace[0].methodName} at ${this.javaClass.name}" + RESET)
            return@onModalInteraction
        }

        val guild = event.guild ?: return
        val courseNumber = event.getValue("courseNumber")?.asString?.toIntOrNull()
        val subjectName = normalizeChanelName(event.getValue("subjectName")?.asString
            ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong accept confirmation: " +
                            "there is no subjectName in processing event.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error(RED + "SubjectName was not found in event " +
                        "in ${Throwable().stackTrace[0].methodName} at ${this.javaClass.name}" + RESET)
                return@onModalInteraction
            }
        )

        if (courseNumber == null || courseNumber !in 1..4) {
            event.deferReply(true).queue()
            event.hook.sendMessage(
                "Hi, you have entered wrong course number.\n " +
                        "It should be a number in range 1..4.\n" +
                        "Try again, please, or contact administration for help."
            ).setEphemeral(true).complete()
            return
        }

        when (event.modalId) {
            "subject create" -> {
                val category = getCourseCategory(courseNumber, guild)
                if (category.textChannels.map { it.name }.contains(subjectName)) {
                    event.deferReply(true).queue()
                    event.hook.sendMessage("Channel with this name already exists. Join it instead of creating.")
                        .setEphemeral(true).complete()
                    return
                }
                category.createTextChannel(subjectName).addMemberPermissionOverride(
                    member.idLong, mutableListOf(
                        Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_CHANNEL
                    ), null
                ).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Subject $subjectName was created successfully!")
                    .setEphemeral(true).complete()
            }

            "subject join" -> {
                val category = getCourseCategory(courseNumber, guild)
                val channel = category.textChannels.find { it.name == subjectName }
                if (channel == null) {
                    event.deferReply(true).queue()
                    event.hook.sendMessage("Problems with subject name or course number.")
                        .setEphemeral(true).complete()
                    return
                }
                channel.manager.putMemberPermissionOverride(
                    member.idLong, mutableListOf(
                        Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_CHANNEL
                    ), null
                ).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Channel $subjectName was updated successfully!\n " +
                        "Check ${courses[courseNumber - 1]} category.").setEphemeral(true).complete()
            }
        }

        logModalInteractionLeave(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.modalId,
            event.user.asTag
        )
    }
}
