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
import net.dv8tion.jda.api.entities.Category
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
import Utility.getCategory
import Utility.getCourseCategory
import Utility.normalizeChanelName
import Utility.sendMessageAndDeferReply

class SubjectManagerBot : ListenerAdapter() {
    private fun sendCreateAndJoin(): List<Button> {
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("createCompulsorySubject", "Создать обязательный курс"))
        buttons.add(Button.primary("createSpecialSubject", "Создать спецкурс"))
        buttons.add(Button.primary("professor_join", "Присоединиться"))
        return buttons
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name)

        val guild = event.guild
        val channelInteraction = getChannel(
            Channels.SUBJECT_INTERACTION,
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
                    getChannel(Channels.SUBJECT_LIST,
                        getCategory(Categories.COURSE_MANAGEMENT, guild)
                    ).asMention
        ).queue()
        channelInteraction.sendMessage("Вы хотите:").setActionRow(sendCreateAndJoin()).queue()

        val channelSpecialSubjectJoin = getChannel(
            Channels.SPECIAL_SUBJECT_JOIN,
            getCategory(Categories.SPECIAL_SUBJECTS, guild)
        )

        clearChannel(channelSpecialSubjectJoin)

        channelSpecialSubjectJoin.sendMessage(
            "Этот чат предназначен для присоединения к спецкурсам. Их список вы можете посмотерть тут:" +
                    getChannel(Channels.SPECIAL_SUBJECT_LIST,
                        getCategory(Categories.SPECIAL_SUBJECTS, guild)
                    ).asMention
        ).setActionRow(Button.primary("student_join", "Присоединиться")).queue()

        logFunctionLeave(Throwable().stackTrace[0].methodName,this.javaClass.name)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId !in listOf(
                "createCompulsorySubject",
                "createSpecialSubject",
                "professor_join",
                "student_join"
            )
        ) return

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
            "createCompulsorySubject" -> {
                val compulsorySubjectCreation = Modal.create(
                    "compulsory subject create",
                    "Compulsory subject creation modal"
                ).addActionRows(ActionRow.of(courseNumber), ActionRow.of(subjectName)).build()

                event.replyModal(compulsorySubjectCreation).queue()
            }
            "createSpecialSubject" -> {
                val specialSubjectCreation = Modal.create(
                    "special subject create",
                    "Special subject creation modal"
                ).addActionRows(ActionRow.of(subjectName)).build()

                event.replyModal(specialSubjectCreation).queue()
            }
            "professor_join" -> {
                val subjectJoin = Modal.create("professor subject join", "Professor subject join modal")
                    .addActionRows(ActionRow.of(courseNumber), ActionRow.of(subjectName))
                    .build()

                event.replyModal(subjectJoin).queue()
            }
            "student_join" -> {
                val subjectJoin = Modal.create("student subject join", "Student subject join modal")
                    .addActionRows(ActionRow.of(subjectName))
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
        if (event.modalId !in listOf(
                "compulsory subject create",
                "special subject create",
                "professor subject join",
                "student subject join"
            )
        ) return

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

        fun createSubjectChannel(category: Category) {
            if (category.textChannels.map { it.name }.contains(subjectName)) {
                event.deferReply(true).queue()
                event.hook.sendMessage("Channel with this name already exists. Join it instead of creating.")
                    .setEphemeral(true).complete()
                return
            }
            category.createTextChannel(subjectName).addMemberPermissionOverride(
                member.idLong, mutableListOf(
                    Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_CHANNEL
                ),
                null
            ).queue()

            event.deferReply(true).queue()
            event.hook.sendMessage("Subject $subjectName was created successfully!")
                .setEphemeral(true).complete()
        }

        val specialSubjectsCategory = getCategory(Categories.SPECIAL_SUBJECTS, guild)
        when(event.modalId) {
            "special subject create" -> {
                createSubjectChannel(specialSubjectsCategory)
                return
            }
            "student subject join" -> {
                val channel = specialSubjectsCategory.textChannels.find { it.name == subjectName } ?: let {
                    event.deferReply(true).queue()
                    event.hook.sendMessage("Special subject with entered name is not exists.\n" +
                            "Check it and try again. You can find list of all subjects here:" +
                            getChannel(Channels.SUBJECT_LIST,
                                getCategory(Categories.COURSE_MANAGEMENT, guild)
                            ).asMention
                    ).setEphemeral(true).complete()
                    return@onModalInteraction
                }
                channel.manager.putMemberPermissionOverride(
                    member.idLong, mutableListOf(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null
                ).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Channel $subjectName was updated successfully!")
                    .setEphemeral(true).complete()
                return
            }
        }

        val courseNumber = event.getValue("courseNumber")?.asString?.toIntOrNull()
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
            "compulsory subject create" ->
                createSubjectChannel(getCourseCategory(courseNumber, guild))

            "professor subject join" -> {
                val category = getCourseCategory(courseNumber, guild)
                val channel = category.textChannels.find { it.name == subjectName } ?: let {
                    event.deferReply(true).queue()
                    event.hook.sendMessage("Problems with subject name or course number.")
                        .setEphemeral(true).complete()
                    return@onModalInteraction
                }
                channel.manager.putMemberPermissionOverride(
                    member.idLong, mutableListOf(
                        Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_CHANNEL
                    ), null
                ).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Channel $subjectName was updated successfully!\n " +
                        "Check ${getCourseCategory(courseNumber - 1, guild).asMention} " +
                        "category.").setEphemeral(true).complete()
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