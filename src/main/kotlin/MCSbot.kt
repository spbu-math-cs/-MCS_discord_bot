import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent

fun main(args: Array<String>) {
    val jda = JDABuilder.create(args[0],
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.MESSAGE_CONTENT,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.GUILD_MESSAGE_TYPING
    ). build()

    jda.presence.setStatus(OnlineStatus.ONLINE)
    jda.addEventListener(NewMemberJoin(), Bot())
}

class Bot : ListenerAdapter() {
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
        event.guild.addRoleToMember(event.member, event.guild.getRolesByName("Администрация", true)
            .first()).complete()
    }
}