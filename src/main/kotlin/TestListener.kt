import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class TestListener : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = event.name
        if (command == "welcome") {
            // Run the 'ping' command
            val userTag = event.user.asTag
            event.reply("Welcome to the server, **$userTag**!").queue()
        }
    }


}