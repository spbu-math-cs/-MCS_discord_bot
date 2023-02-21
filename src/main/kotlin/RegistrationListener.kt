import GlobalLogger.GREEN
import GlobalLogger.RED
import GlobalLogger.RESET
import GlobalLogger.YELLOW
import GlobalLogger.globalLogger
import GlobalLogger.logButtonInteractionEnter
import GlobalLogger.logButtonInteractionLeave
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import GlobalLogger.logFunctionEnter
import GlobalLogger.logFunctionLeave
import GlobalLogger.logModalInteractionEnter
import GlobalLogger.logModalInteractionLeave
import Utility.clearChannel
import Utility.StudyDirection
import Utility.GuildRole
import Utility.Channels
import Utility.Categories
import Utility.getCategory
import Utility.getChannel
import Utility.getRole
import Utility.sendMessageAndDeferReply
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

class RegistrationListener : ListenerAdapter() {
    private lateinit var registrationRole: Role
    private lateinit var professorRole: Role
    private lateinit var professorConfirmationRole: Role

    private val nameTextInput = TextInput.create("name", "Имя", TextInputStyle.SHORT)
        .setRequiredRange(1, 15)
        .setPlaceholder("Иван")
        .build()
    private val surnameTextInput = TextInput.create("surname", "Фамилия", TextInputStyle.SHORT)
        .setRequiredRange(1, 15)
        .setPlaceholder("Иванов")
        .build()

    private val welcomeMessage = "Перед тем, как пользоваться всем функционалом, Вам необходимо зарегистрироваться. " +
            "Укажите, являетесь преподавателем или студентом, нажав на соответствующую кнопку. " +
            "Далее укажите ваши данные: имя, фамилию и курс. Пожалуйста, вводите достоверную информацию, " +
            "так всем будет проще. Если Вы регистрируетесь, как преподаватель, " +
            "то необходимо будет дождаться подтверждения Вашего профиля от Администрации."

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild

        registrationRole = getRole(GuildRole.REGISTRATION, guild)
        professorRole = getRole(GuildRole.PROFESSOR, guild)
        professorConfirmationRole = getRole(GuildRole.PROFESSOR_CONFIRMATION, guild)

        val channel = getChannel(
            Channels.REGISTRATION,
            getCategory(Categories.REGISTRATION, guild)
        )

        clearChannel(channel)

        channel.sendMessage(
            "Рады приветствовать вас на официальном " +
                    "сервере факультета Математики и Компьютерных Наук!"
        ).queue()
        channel.sendMessage(welcomeMessage)
        channel.sendMessage("Вы:").setActionRow(sendStudentAndProfessor()).complete()
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name)

        event.guild.addRoleToMember(
            event.member,
            getRole(GuildRole.REGISTRATION, event.guild)
        ).queue()

        logFunctionLeave(Throwable().stackTrace[0].methodName, this.javaClass.name)
    }

    private fun sendStudentAndProfessor(): List<Button> {
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("student", "Студент"))
        buttons.add(Button.primary("professor", "Преподаватель"))
        return buttons
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId !in listOf("student", "professor", "accept", "deny"))
            return

        logButtonInteractionEnter(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.componentId,
            event.user.asTag
        )

        fun acceptOrDeny(accept: Boolean) {
            val guild = event.guild ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong accept confirmation: " +
                            "there is no guild in processing event.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error(RED + "Guild was not found in event " +
                        "in ${Throwable().stackTrace[0].methodName} " +
                        "at ${this.javaClass.name}" + RESET)
                return@acceptOrDeny
            }
            val embed = event.message.embeds.firstOrNull() ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong message format.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error(RED + "Some troubles with confirmation message + " +
                        "in ${Throwable().stackTrace[0].methodName} " +
                        "at ${this.javaClass.name} (embed was not found)" + RESET)
                return@acceptOrDeny
            }

            val registeredMember = guild.getMemberByTag(embed.description.toString()) ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong message format.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error("Some troubles with confirmation message " +
                        "in ${Throwable().stackTrace[0].methodName} " +
                        "at ${this.javaClass.name} (member was not found in embed)")
                return@acceptOrDeny
            }

            event.message.delete().queue()

            if (accept) {
                guild.modifyMemberRoles(
                    registeredMember,
                    listOf(getRole(GuildRole.PROFESSOR, guild)),
                    listOf(getRole(GuildRole.PROFESSOR_CONFIRMATION, guild))
                ).queue()

                sendMessageAndDeferReply(
                    event, "Запрос принят!\n" +
                            "Пользователь ${registeredMember.asMention} получил роль преподавателя."
                )
            } else {
                guild.modifyMemberRoles(
                    registeredMember,
                    listOf(getRole(GuildRole.REGISTRATION, guild)),
                    listOf(getRole(GuildRole.PROFESSOR_CONFIRMATION, guild))
                ).queue()

                sendMessageAndDeferReply(
                    event, "Запрос отклонён!\n" +
                            "Пользователь ${registeredMember.asMention} обратно направлен на регистрацию.\n" +
                            "Вы можете забанить или прогнать его/её, если он/она продалжает посылать запросы."
                )
            }
        }

        when (event.componentId) {
            "student" -> {
                val courseNumber = TextInput.create(
                    "courseNumber",
                    "Номер курса",
                    TextInputStyle.SHORT
                ).setRequiredRange(1, 1).setPlaceholder("1").build()

                val studyDirection = TextInput.create(
                    "studyDirection",
                    "Название направления",
                    TextInputStyle.SHORT
                ).setRequiredRange(1, 3)
                    .setPlaceholder("М, НОД или СП (в любом регистре)")
                    .build()

                val studentRegModal = Modal.create(
                    "student profile",
                    "Настрока профиля студента"
                ).addActionRows(
                    ActionRow.of(surnameTextInput),
                    ActionRow.of(nameTextInput),
                    ActionRow.of(studyDirection),
                    ActionRow.of(courseNumber)
                ).build()

                event.replyModal(studentRegModal).complete()
            }

            "professor" -> {
                val professorRegModal = Modal.create(
                    "professor profile",
                    "Настройка профиля преподавателя"
                ).addActionRows(ActionRow.of(surnameTextInput), ActionRow.of(nameTextInput)).build()

                event.replyModal(professorRegModal).complete()
            }

            "accept" -> acceptOrDeny(true)

            "deny" -> acceptOrDeny(false)
        }

        logButtonInteractionLeave(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.componentId,
            event.user.asTag
        )
    }

    private fun createConfirmationMessage(member: Member, name: String, surname: String): MessageEmbed {
        val messageForConfirmation = EmbedBuilder()
        messageForConfirmation.setDescription(member.user.asTag)
        messageForConfirmation.setTitle("Подтверждение роли преподавателя")
        messageForConfirmation.addField("Discord профиль", member.asMention, false)
        messageForConfirmation.addField("Указанное имя", name, false)
        messageForConfirmation.addField("Указанная фамилия", surname, false)
        messageForConfirmation.setColor(Color.CYAN)

        return messageForConfirmation.build()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId !in listOf("student profile", "professor profile"))
            return

        logModalInteractionEnter(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.modalId,
            event.user.asTag
        )

        val member = event.member ?: return
        val guild = event.guild ?: return
        val surname = event.getValue("surname")?.asString
        val name = event.getValue("name")?.asString

        if (name == null || surname == null) {
            globalLogger.error("$GREEN Some troubles with entered name/surname " +
                    "in $YELLOW ${Throwable().stackTrace[0].methodName} $GREEN " +
                    "at $YELLOW ${this.javaClass.name} $RESET")
            event.deferReply(true).queue()
            event.hook.sendMessage(
                "Wrong registration processing. There is no name/surname in event.\n" +
                        "Please, tell dummy programmers about that, and they will definitely fix that."
            ).setEphemeral(true).complete()
            return
        }

        when (event.modalId) {

            "student profile" -> {
                val courseNumber = event.getValue("courseNumber")?.asString?.trim()?.toIntOrNull() ?: 0
                if (courseNumber !in 1..4) {
                    event.deferReply(true).queue()
                    event.hook.sendMessage(
                        "Вы ввели некорректный номер курса.\n " +
                                "Это должно быть число в диапазоне 1..4.\n" +
                                "Попробуйте, пожалуйста, ещё раз или свяжитесь с администрацией для помощи."
                    ).setEphemeral(true).complete()
                    return
                }

                val studyDirection = StudyDirection[event.getValue("studyDirection")?.asString?.trim()?.uppercase() ?: ""]
                    ?: let {
                    event.deferReply(true).queue()
                    event.hook.sendMessage(
                        "Вы ввели некорректное название направления.\n " +
                                "Оно должно быть одним из перечисленных: СП, НОД или М.\n" +
                                "Попробуйте, пожалуйста, ещё раз или свяжитесь с администрацией для помощи."
                    ).setEphemeral(true).complete()
                    return@onModalInteraction
                }

                val chosenRole = getRole(studyDirection, courseNumber, guild)

                member.modifyNickname("$surname $name".trim()).queue()
                member.roles.forEach { guild.removeRoleFromMember(member, it) }
                guild.modifyMemberRoles(member, listOf(chosenRole), listOf(registrationRole)).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Вы успешно зарегистрированы, как $surname $name!")
                    .setEphemeral(true).complete()
            }

            "professor profile" -> {
                member.modifyNickname("$surname $name".trim()).queue()

                guild.modifyMemberRoles(member, listOf(professorConfirmationRole), listOf(registrationRole)).queue()

                val channelConfirmation = getChannel(
                    Channels.PROFESSOR_CONFIRMATION,
                    getCategory(Categories.ADMINISTRATION, guild)
                )

                channelConfirmation.sendMessageEmbeds(
                    createConfirmationMessage(member, name, surname)
                ).setActionRow(
                    Button.primary("accept", "Принять"),
                    Button.primary("deny", "Отклонить")
                ).queue()
                event.deferReply(true).queue()
                event.hook.sendMessage(
                    "Первый этап регистрации заверешён. Теперь остолось дождаться, " +
                            "пока администрация подтвердит Ваш профиль."
                ).setEphemeral(true).complete()
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