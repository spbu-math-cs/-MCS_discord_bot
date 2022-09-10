import Utility.clearChannel
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
import Utility.Roles
import Utility.Channels
import Utility.Categories
import Utility.getCategory
import Utility.getChannel

class RegistrationBot : ListenerAdapter() {
    private lateinit var registrationRole: Role
    private lateinit var professorRole: Role

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild

        registrationRole = Utility.getRole(Roles.REGISTRATION, guild)
        professorRole = Utility.getRole(Roles.PROFESSOR, guild)

        val channel = getChannel(Channels.REGISTRATION.label, getCategory(Categories.REGISTRATION, guild))

        clearChannel(channel)

        channel.sendMessage("Рады приветствовать вас на официальном " +
                "сервере программы Современное Программирование!").queue()
        channel.sendMessage("Вы:").setActionRow(sendStudentAndProfessor()).complete()
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) =
        event.guild.addRoleToMember(event.member, Utility.getRole(Roles.REGISTRATION, event.guild)).queue()

    private fun sendStudentAndProfessor(): List<Button> {
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("student", "Студент"))
        buttons.add(Button.primary("professor", "Преподаватель"))
        return buttons
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId !in listOf("student", "professor"))
            return

        val name = TextInput.create("name", "Name", TextInputStyle.SHORT)
            .setRequiredRange(1, 50)
            .setPlaceholder("Иван")
            .build()

        val surname = TextInput.create("surname", "Surname", TextInputStyle.SHORT)
            .setRequiredRange(1, 50)
            .setPlaceholder("Иванов")
            .build()

        when (event.componentId) {
            "student" -> {
                val courseNumber = TextInput.create("courseNumber", "courseNumber", TextInputStyle.SHORT)
                    .setRequiredRange(1, 1)
                    .setPlaceholder("1")
                    .build()

                val studentRegModal = Modal.create("student profile", "Setting student profile")
                    .addActionRows(ActionRow.of(surname), ActionRow.of(name), ActionRow.of(courseNumber))
                    .build()

                event.replyModal(studentRegModal).complete()
            }

            "professor" -> {
                val professorRegModal = Modal.create("professor profile", "Setting professor profile")
                    .addActionRows(ActionRow.of(surname), ActionRow.of(name))
                    .build()

                event.replyModal(professorRegModal).complete()
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId !in listOf("student profile", "professor profile"))
            return

        val member = event.member ?: return
        val guild = event.guild ?: return

        when (event.modalId) {
            "student profile" -> {
                val surname = event.getValue("surname")?.asString ?: "Error" //логгер
                val name = event.getValue("name")?.asString ?: "Error" //логгер
                val courseNumber = event.getValue("courseNumber")?.asString?.trim()?.toIntOrNull() //логгер
                println(courseNumber)
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
                guild.addRoleToMember(member, chosenRole).queue()
                guild.removeRoleFromMember(member, registrationRole).queue()
                event.deferReply(true).queue()
                event.hook.sendMessage("Hi, $surname $name!\n You have been successfully registered!")
                    .setEphemeral(true).queue()
            }

            "professor profile" -> {
                val surname = event.getValue("surname")?.asString ?: "Error"
                val name = event.getValue("name")?.asString ?: "Error"
                member.modifyNickname("$surname $name".trim()).queue()

                guild.modifyMemberRoles(member, listOf(professorRole), listOf(registrationRole)).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Hello, $surname $name!\n You have been successfully registered!")
                    .setEphemeral(true).queue()
            }
        }
    }
}