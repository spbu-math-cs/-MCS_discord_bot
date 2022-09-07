import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag


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

    override fun onGuildReady(event: GuildReadyEvent) {
        val commandData: MutableList<CommandData> = ArrayList()
        commandData.add(Commands.slash("welcome", "Get welcomed by the bot"))
        commandData.add(Commands.slash("roles", "Display all roles on the server"))
        event.guild.updateCommands().addCommands(commandData).queue()
    }
}

class HelloBot: ListenerAdapter() {

    override fun onGuildReady(event: GuildReadyEvent) {
        event.guild.getCategoriesByName("РЕГИСТРАЦИЯ", true)
            .first().textChannels.find { it.name == "регистрация" }?.sendMessage("Nice to meet you!")?.setActionRow(sendButtons())?.queue()
    }

    private fun sendButtons() : List<Button>{
        val buttons: MutableList<Button> = mutableListOf()
        buttons.add(Button.primary("hello","Click me :)"))
        buttons.add(Button.primary("fake","Do not click me!!!"))
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
            "fake" -> event.editMessage("That button didn't say click me").queue() // update the message
        }
    }
}

