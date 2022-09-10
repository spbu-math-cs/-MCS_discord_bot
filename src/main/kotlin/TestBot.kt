import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class TestBot : ListenerAdapter() {

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