import GlobalLogger.GREEN
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
import Utility.Roles
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

class RegistrationBot : ListenerAdapter() {
    private lateinit var registrationRole: Role
    private lateinit var professorRole: Role
    private lateinit var professorConfirmationRole: Role

    private val nameTextInput = TextInput.create("name", "Name", TextInputStyle.SHORT)
        .setRequiredRange(1, 15)
        .setPlaceholder("Иван")
        .build()
    private val surnameTextInput = TextInput.create("surname", "Surname", TextInputStyle.SHORT)
        .setRequiredRange(1, 15)
        .setPlaceholder("Иванов")
        .build()

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild

        registrationRole = getRole(Roles.REGISTRATION, guild)
        professorRole = getRole(Roles.PROFESSOR, guild)
        professorConfirmationRole = getRole(Roles.PROFESSOR_CONFIRMATION, guild)

        val channel = getChannel(Channels.REGISTRATION.label, getCategory(Categories.REGISTRATION, guild))

        clearChannel(channel)

        channel.sendMessage(
            "Рады приветствовать вас на официальном " +
                    "сервере программы Современное Программирование!"
        ).queue()
        channel.sendMessage("Вы:").setActionRow(sendStudentAndProfessor()).complete()
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name)

        event.guild.addRoleToMember(event.member, getRole(Roles.REGISTRATION, event.guild)).queue()

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

        logButtonInteractionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name, event.componentId)

        fun acceptOrDeny(accept: Boolean) {

            val guild = event.guild ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong accept confirmation: " +
                            "there is no guild in processing event.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error("Guild was not found in event in ${Throwable().stackTrace[0].methodName} " +
                        "at ${this.javaClass.name}")
                return@acceptOrDeny
            }

            val embed = event.message.embeds.firstOrNull() ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong message format.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error("Some troubles with confirmation message in ${Throwable().stackTrace[0].methodName} " +
                        "at ${this.javaClass.name} (embed was not found)")
                return@acceptOrDeny
            }

            val registeredMember = guild.getMemberByTag(embed.description.toString()) ?: let {
                sendMessageAndDeferReply(
                    event, "Wrong message format.\n " +
                            "Please, tell dummy programmers about that, and they will definitely fix that."
                )
                globalLogger.error("Some troubles with confirmation message in ${Throwable().stackTrace[0].methodName} " +
                        "at ${this.javaClass.name} (member was not found in embed)")
                return@acceptOrDeny
            }

            event.message.delete().queue()

            if (accept) {
                guild.modifyMemberRoles(
                    registeredMember,
                    listOf(getRole(Roles.PROFESSOR, guild)),
                    listOf(getRole(Roles.PROFESSOR_CONFIRMATION, guild))
                ).queue()

                sendMessageAndDeferReply(
                    event, "Accepted successfully!\n" +
                            "Member ${registeredMember.asMention} is professor now."
                )
            } else {
                guild.modifyMemberRoles(
                    registeredMember,
                    listOf(getRole(Roles.REGISTRATION, guild)),
                    listOf(getRole(Roles.PROFESSOR_CONFIRMATION, guild))
                ).queue()

                sendMessageAndDeferReply(
                    event, "Denied successfully!\n" +
                            "Member ${registeredMember.asMention} is pushed back into registration.\n" +
                            "You can ban or kick him/her, if he/she continues doing this."
                )
            }
        }

        when (event.componentId) {
            "student" -> {
                val courseNumber = TextInput.create("courseNumber", "courseNumber", TextInputStyle.SHORT)
                    .setRequiredRange(1, 1)
                    .setPlaceholder("1")
                    .build()

                val studentRegModal = Modal.create("student profile", "Setting student profile")
                    .addActionRows(
                        ActionRow.of(surnameTextInput),
                        ActionRow.of(nameTextInput),
                        ActionRow.of(courseNumber)
                    )
                    .build()

                event.replyModal(studentRegModal).complete()
            }

            "professor" -> {
                val professorRegModal = Modal.create("professor profile", "Setting professor profile")
                    .addActionRows(ActionRow.of(surnameTextInput), ActionRow.of(nameTextInput))
                    .build()

                event.replyModal(professorRegModal).complete()
            }

            "accept" -> acceptOrDeny(true)

            "deny" -> acceptOrDeny(false)
        }

        logButtonInteractionLeave(Throwable().stackTrace[0].methodName, this.javaClass.name, event.componentId)
    }

    private fun createConfirmationMessage(member: Member, name: String, surname: String): MessageEmbed {
        val messageForConfirmation = EmbedBuilder()
        messageForConfirmation.setDescription(member.user.asTag)
        messageForConfirmation.setTitle("Professor role confirmation")
        messageForConfirmation.addField("Discord profile", member.asMention, false)
        messageForConfirmation.addField("Specified name", name, false)
        messageForConfirmation.addField("Specified surname", surname, false)
        messageForConfirmation.setColor(Color.CYAN)

        return messageForConfirmation.build()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId !in listOf("student profile", "professor profile"))
            return

        logModalInteractionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name, event.modalId)

        val member = event.member ?: return
        val guild = event.guild ?: return

        when (event.modalId) {
            "student profile" -> {
                val surname = event.getValue("surname")?.asString ?: let {
                    globalLogger.error("$GREEN Some troubles with entered surname in $YELLOW ${Throwable().stackTrace[0].methodName} $GREEN " +
                            "at $YELLOW ${this.javaClass.name} $RESET")
                    "Error"
                }
                val name = event.getValue("name")?.asString ?: let {
                    globalLogger.error("$GREEN Some troubles with entered name in $YELLOW ${Throwable().stackTrace[0].methodName} $GREEN " +
                            "at $YELLOW ${this.javaClass.name} $RESET")
                    "Error"
                }
                val courseNumber = event.getValue("courseNumber")?.asString?.trim()?.toIntOrNull() //логгер
                if (courseNumber == null || courseNumber !in 1..4) {
                    event.reply(
                        "Hi, you have entered wrong course number.\n " +
                                "It should be a number in range 1..4.\n" +
                                "Try again, please, or contact administration for help."
                    ).setEphemeral(true).complete()
                    return
                }

                val chosenRole = Utility.getCourseRole(courseNumber, guild)

                member.modifyNickname("$surname $name".trim()).queue()
                member.roles.forEach { guild.removeRoleFromMember(member, it) }
                guild.modifyMemberRoles(member, listOf(chosenRole), listOf(registrationRole)).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Hi, $surname $name!\n You have been successfully registered!")
                    .setEphemeral(true).complete()
            }

            "professor profile" -> {
                val surname = event.getValue("surname")?.asString ?: let {
                    globalLogger.error("$GREEN Some troubles with entered surname in $YELLOW ${Throwable().stackTrace[0].methodName} $GREEN " +
                            "at $YELLOW ${this.javaClass.name} $RESET")
                    "Error"
                }
                val name = event.getValue("name")?.asString ?: let {
                    globalLogger.error("$GREEN Some troubles with entered name in $YELLOW ${Throwable().stackTrace[0].methodName} $GREEN " +
                            "at $YELLOW ${this.javaClass.name} $RESET")
                    "Error"
                }
                member.modifyNickname("$surname $name".trim()).queue()

                guild.modifyMemberRoles(member, listOf(professorConfirmationRole), listOf(registrationRole)).queue()

                val channelConfirmation = getChannel(
                    Channels.PROFESSOR_CONFIRMATION.label,
                    getCategory(Categories.ADMINISTRATION, guild)
                )

                channelConfirmation.sendMessageEmbeds(
                    createConfirmationMessage(member, name, surname)
                ).setActionRow(
                    Button.primary("accept", "Accept"),
                    Button.primary("deny", "Deny")
                ).queue()
                event.deferReply(true).queue()
                event.hook.sendMessage(
                    "Hello, $surname $name!\n Wait until " +
                            "administration confirm your profile."
                ).setEphemeral(true).complete()
            }
        }

        logModalInteractionLeave(Throwable().stackTrace[0].methodName, this.javaClass.name, event.modalId)
    }
}