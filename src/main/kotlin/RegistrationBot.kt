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
import Utility.BasicChannels
import Utility.BasicCategories
import Utility.channelGetter
import Utility.courses

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
        val member = event.member ?: return

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
                if (member.roles.contains(professorRole))
                    return
                val courseNumber = TextInput.create("courseNumber", "courseNumber", TextInputStyle.SHORT)
                    .setRequiredRange(1, 1)
                    .setPlaceholder("1")
                    .build()

                val studentRegModal = Modal.create("student profile", "Setting student profile")
                    .addActionRows(ActionRow.of(surname), ActionRow.of(name), ActionRow.of(courseNumber))
                    .build()

                event.replyModal(studentRegModal).queue()
            }

            "professor" -> {
//                if (member.roles.any { courseRoles.contains(it) })
//                    return
//                val professorRegModal = Modal.create("professor profile", "Setting professor profile")
//                    .addActionRows(ActionRow.of(surname), ActionRow.of(name))
//                    .build()
//
//                event.replyModal(professorRegModal).queue()
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val member = event.member ?: return
        val guild = event.guild ?: return

        if (!member.roles.contains(guild.getRolesByName(Roles.REGISTRATION.label, true).first())) {
            event.deferReply(true).queue()
            event.hook.sendMessage("You have been already registered. It is impossible to do it again.")
                .setEphemeral(true).queue()
        }

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
                    )
                        .setEphemeral(true).queue()
                    return
                }

                val chosenRole = Utility.getCourseRole(courseNumber - 1, guild)

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

                guild.addRoleToMember(member, professorRole).queue()
                guild.removeRoleFromMember(member, registrationRole).queue()

                event.deferReply(true).queue()
                event.hook.sendMessage("Hello, $surname $name!\n You have been successfully registered!")
                    .setEphemeral(true).queue()
            }
        }
    }
}