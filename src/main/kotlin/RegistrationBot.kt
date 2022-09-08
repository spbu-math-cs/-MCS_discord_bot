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

class RegistrationBot : ListenerAdapter() {

    private val courseRoles: MutableList<Role> = mutableListOf()
    private lateinit var registrationRole: Role
    private lateinit var professorRole: Role

    override fun onGuildReady(event: GuildReadyEvent) {

        registrationRole = event.guild.getRolesByName("Регистрация", true).first()
        professorRole = event.guild.getRolesByName("Преподаватель", true).first()
        for (i in 1..4)
            courseRoles.add(event.guild.getRolesByName("СП $i", true).first())

        val channel = event.guild.getCategoriesByName("Регистрация", true)
            .first().textChannels.find { it.name == "регистрация" } ?: return //логгер

        clearChannel(channel)

        channel.sendMessage("Рады приветствовать вас на официальном сервере программы Современное Программирование!")
            .complete()
        channel.sendMessage("Вы:").setActionRow(sendStudentAndProfessor()).queue()

    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        event.guild.addRoleToMember(
            event.member, event.guild.getRolesByName("Регистрация", true)
                .first()
        ).complete()
    }

    private fun sendStudentAndProfessor(): List<Button> {
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("student", "Студент"))
        buttons.add(Button.primary("professor", "Преподаватель"))
        return buttons
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val member = event.member ?: return

        val name = TextInput.create("name", "Name", TextInputStyle.SHORT)
            .setPlaceholder("Name")
            .setRequiredRange(1, 50)
            .setPlaceholder("Иван")
            .build()

        val surname = TextInput.create("surname", "Surname", TextInputStyle.SHORT)
            .setPlaceholder("Surname")
            .setRequiredRange(1, 50)
            .setPlaceholder("Иванов")
            .build()

        when (event.componentId) {
            "student" -> {
                if (member.roles.contains(professorRole))
                    return
                val courseNumber = TextInput.create("courseNumber", "courseNumber", TextInputStyle.SHORT)
                    .setPlaceholder("courseNumber")
                    .setRequiredRange(1, 1)
                    .setPlaceholder("1")
                    .build()

                val studentRegModal = Modal.create("student profile", "Setting student profile")
                    .addActionRows(ActionRow.of(surname), ActionRow.of(name), ActionRow.of(courseNumber))
                    .build()

                event.replyModal(studentRegModal).queue()
            }

            "professor" -> {
                if (member.roles.any { courseRoles.contains(it) })
                    return
                val professorRegModal = Modal.create("professor profile", "Setting professor profile")
                    .addActionRows(ActionRow.of(surname), ActionRow.of(name))
                    .build()

                event.replyModal(professorRegModal).queue()
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val member = event.member ?: return
        val guild = event.guild ?: return

        when (event.modalId) {
            "student profile" -> {
                val surname = event.getValue("surname")?.asString ?: "Error" //логгер
                val name = event.getValue("name")?.asString ?: "Error" //логгер
                val course = event.getValue("course")?.asString?.toIntOrNull() //логгер

                if (course == null || course !in 1..4) {
                    event.reply(
                        "Hi, you have entered wrong course number.\n " +
                                "It should be a number in range 1..4.\n" +
                                "Try again, please, or contact administration for help."
                    )
                        .setEphemeral(true).queue()
                    return
                }

                val chosenRole = courseRoles[course - 1]

                member.modifyNickname("$surname $name".trim()).queue()
                member.roles.forEach { guild.removeRoleFromMember(member, it) }
                guild.addRoleToMember(member, chosenRole).complete()
                guild.removeRoleFromMember(member, registrationRole).complete()
                event.reply("Hi, $surname $name!\n You have been successfully registered!")
                    .setEphemeral(true).queue()
            }

            "professor profile" -> {
                val surname = event.getValue("surname")?.asString ?: "Error"
                val name = event.getValue("name")?.asString ?: "Error"
                member.modifyNickname("$surname $name".trim()).queue()

                guild.addRoleToMember(member, professorRole).queue()
                guild.removeRoleFromMember(member, registrationRole).queue()
                event.reply("Hello, $surname $name!\n You have been successfully registered!").setEphemeral(true)
                    .queue()
            }
        }
    }
}