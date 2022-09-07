import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.internal.wait


fun main(args: Array<String>) {
    val jda = JDABuilder.create(
        args[0],
        GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)
    ).setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL).enableCache(CacheFlag.ONLINE_STATUS).build()

    jda.presence.setStatus(OnlineStatus.ONLINE)
    jda.addEventListener(NewMemberJoin(), IsBotAlive(), CommandManager(), HelloBot())
}

class IsBotAlive : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message
        if (msg.contentRaw == "!ping") {
            val channel: MessageChannel = event.channel
            val time = System.currentTimeMillis()
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                .queue { response: Message ->
                    response.editMessageFormat(
                        "Pong: %d ms",
                        System.currentTimeMillis() - time
                    ).queue()
                }
        }
    }
}

class NewMemberJoin : ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {

        event.guild.getCategoriesByName("РЕГИСТРАЦИЯ", true)
            .first().textChannels.find { it.name == "регистрация" }
            ?.sendMessage("XXX")?.complete()
            ?: throw Exception()

        // Add role
        event.guild.addRoleToMember(
            event.member, event.guild.getRolesByName("Регистрация", true)
                .first()
        ).complete()
    }
}


 class CommandManager: ListenerAdapter() {

    /**
     * Listens for slash commands and responds accordingly
     */

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = event.name
        if (command == "welcome") {
            // Run the 'ping' command
            val userTag = event.user.asTag
            event.reply("Welcome to the server, **$userTag**!").queue()
        } else if (command == "roles") {
            // run the 'roles' command
            event.deferReply().queue()
            var response = ""
            for (role in event.guild!!.roles) {
                response += """
                ${role.asMention}
                
                """.trimIndent()
            }
            event.hook.sendMessage(response).queue()
        }
    }


}

class HelloBot: ListenerAdapter() {

    private val roles: MutableList<Role> = mutableListOf()

    override fun onGuildReady(event: GuildReadyEvent) {

        roles.add( event.guild.getRolesByName("СП 1", true).first())
        roles.add( event.guild.getRolesByName("СП 2", true).first())
        roles.add( event.guild.getRolesByName("СП 3", true).first())
        roles.add( event.guild.getRolesByName("СП 4", true).first())

        val channel = event.guild.getCategoriesByName("РЕГИСТРАЦИЯ", true)
            .first().textChannels.find { it.name == "регистрация" }

        //Очистка чата, нужно придумать рабочую
       /* var currentId: String? = channel?.latestMessageId;
        while (currentId != null) {
            currentId = channel?.latestMessageId
            channel?.deleteMessageById(currentId!!)
        }*/
        val commandData: MutableList<CommandData> = ArrayList()
        commandData.add(Commands.slash("welcome", "Get welcomed by the bot"))
        commandData.add(Commands.slash("roles", "Display all roles on the server"))
        event.guild.updateCommands().addCommands(commandData).queue()
        channel?.sendMessage("Nice to meet you!")?.setActionRow(sendButtons())?.queue()
    }

    private fun sendButtons() : List<Button>{
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("hello","Click me :)"))
        buttons.add(Button.primary("registration","Setting profile"))
        return buttons
    }

    /*override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "hello") {
            event.reply("Click the button to say hello")
                .addActionRow(
                    Button.primary("hello", "Click Me"),  // Button with only a label
                    Button.success("emoji", Emoji.fromFormatted("<:minn:245267426227388416>"))) // Button with only an emoji
                .queue()
        } else if (event.name == "info") {
            event.reply("Click the buttons for more info")
                .addActionRow( // link buttons don't send events, they just open a link in the browser when clicked
                    Button.link("https://github.com/DV8FromTheWorld/JDA", "GitHub")
                        .withEmoji(Emoji.fromFormatted("<:github:849286315580719104>")),  // Link Button with label and emoji
                    Button.link("https://ci.dv8tion.net/job/JDA/javadoc/", "Javadocs")) // Link Button with only a label
                .queue()
        }
    }*/

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.componentId) {
            "hello" -> event.reply("Hello :)").queue() // send a message in the channel
            "registration" -> {
                val subject = TextInput.create("subject", "Name", TextInputStyle.SHORT)
                    .setPlaceholder("Name")
                    .setRequiredRange(0,50)
                    .setPlaceholder("Иван")
                    .build()

                val body = TextInput.create("body", "Surname", TextInputStyle.SHORT)
                    .setPlaceholder("Surname")
                    .setRequiredRange(0,50)
                    .setPlaceholder("Иванов")
                    .build()

                val course = TextInput.create("course", "Course", TextInputStyle.SHORT)
                    .setPlaceholder("Course")
                    .setRequiredRange(1,1)
                    .setPlaceholder("1")
                    .build()

                val modal = Modal.create("modmail", "Setting profile")
                    .addActionRows(ActionRow.of(subject), ActionRow.of(body),  ActionRow.of(course))
                    .build()

                event.replyModal(modal).queue()
            }
        }
    }
        override fun onModalInteraction(event: ModalInteractionEvent) {
            if (event.modalId == "modmail") {
                val surname = event.getValue("subject")?.asString ?: "Error"
                val name = event.getValue("body")?.asString ?: "Error"
                val course = event.getValue("course")?.asString?.toInt() ?: 0
                event.member?.modifyNickname("$surname $name".trim())?.queue()
                val role: Role? = when (course) {
                    1 -> event.guild?.getRolesByName("СП 1", true)?.first()
                    2 -> event.guild?.getRolesByName("СП 2", true)?.first()
                    3 -> event.guild?.getRolesByName("СП 3", true)?.first()
                    4 -> event.guild?.getRolesByName("СП 4", true)?.first()
                    else -> null
                }
                if (role != null) {
                    val member = event.member!!
                    event.guild?.removeRoleFromMember(member, roles[0])?.complete()
                    event.guild?.removeRoleFromMember(member, roles[1])?.complete()
                    event.guild?.removeRoleFromMember(member, roles[2])?.complete()
                    event.guild?.removeRoleFromMember(member, roles[3])?.complete()
                    event.guild?.addRoleToMember(event.member!!, role)?.complete()
                }
                event.reply("Hi, $surname $name!").setEphemeral(true).queue()
            }
        }
    }


