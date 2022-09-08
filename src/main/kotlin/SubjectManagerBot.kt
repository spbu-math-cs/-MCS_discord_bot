import Utility.clearChannel
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
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

class SubjectManagerBot : ListenerAdapter() {


    private fun sendCreateAndJoin(): List<Button> {
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("create", "Создать"))
        buttons.add(Button.primary("join", "Присоединиться"))
        return buttons
    }

    private fun channelListSetup(guild: Guild){
        val channelList: StringBuilder
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        val channel = channelGetter(guild,
            BasicCategories.COURSE_MANAGEMENT.category,
            BasicChannels.COURSE_INTERACTION.channel
        )
        clearChannel(channel)

        channel.sendMessage("Этот чат предназначен для создания каналов для курсов " +
                "и присоединения к уже существующим курсам.").queue()
        channel.sendMessage("Ознакомиться с полным списком курсов " +
                "Вы можете в соседнем канале: " +
                channelGetter(guild, 
                    BasicCategories.COURSE_MANAGEMENT.category, 
                    BasicChannels.COURSE_LIST.channel
                ).asMention
        ).queue()
        channel.sendMessage("Вы хотите:").setActionRow(sendCreateAndJoin()).queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        val courseNumber = TextInput.create("courseNumber", "Course number", TextInputStyle.SHORT)
            .setPlaceholder("Course number")
            .setRequiredRange(1, 1)
            .setPlaceholder("1")
            .build()

        val courseName = TextInput.create("courseName", "Course name", TextInputStyle.SHORT)
            .setPlaceholder("Course name")
            .setRequiredRange(1, 150)
            .setPlaceholder("Теоретическая информатика (практика)")
            .build()

        when (event.componentId) {
            "create" -> {
                val courseCreation = Modal.create("course create", "Course creation modal")
                    .addActionRows(ActionRow.of(courseNumber), ActionRow.of(courseName))
                    .build()

                event.replyModal(courseCreation).queue()
            }
// Если введён не тот курс в reply дописать, что названия курсов можно найти там-то, там-то
            "join" -> {
                val courseJoin = Modal.create("course join", "Course join modal")
                    .addActionRows(ActionRow.of(courseNumber), ActionRow.of(courseName))
                    .build()

                event.replyModal(courseJoin).queue()
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val member = event.member ?: return
        val guild = event.guild ?: return

        when (event.modalId) {
            "course create" -> {
                val courseNumber = event.getValue("courseNumber")?.asString?.toIntOrNull() //логгер
                val courseName = event.getValue("courseName")?.asString ?: "Error" //логгер

                if (courseNumber == null || courseNumber !in 1..4) {
                    event.reply(
                        "Hi, you have entered wrong course number.\n " +
                                "It should be a number in range 1..4.\n" +
                                "Try again, please, or contact administration for help."
                    )
                        .setEphemeral(true).queue()
                    return
                }

                val category = guild.getCategoriesByName(courses[courseNumber-1], true).first()
                category.createTextChannel(courseName).addMemberPermissionOverride(
                    member.idLong, mutableListOf(
                        Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_CHANNEL
                    ), null
                ).queue()

                event.reply("Channel $courseName was created successfully!")
                    .setEphemeral(true).queue()
            }

            "course join" -> {
                val courseNumber = event.getValue("courseNumber")?.asString?.toIntOrNull() ?: return//логгер + отправка пользователю сообщение об ошибке ввода
                val courseName = event.getValue("courseName")?.asString ?: "Error" //логгер

                val category = guild.getCategoriesByName(courses[courseNumber-1], true).first()
                val channel = category.textChannels.find { it.name == courseName }
                if (channel == null) {
                    event.reply("Problems with course name.").setEphemeral(true).queue()
                    return
                }
                channel.manager.putMemberPermissionOverride(
                    member.idLong, mutableListOf(
                        Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_CHANNEL
                    ), null
                ).queue()

                event.reply("Channel $courseName was updated successfully!\n Check ${courses[courseNumber]} category.")
                    .setEphemeral(true).queue()
            }
        }
    }
}