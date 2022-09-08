import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel

object Utility {
    //Очистка чата
    fun clearChannel(channel: TextChannel) {
        var deletingFlag = true
        val history = MessageHistory(channel)
        while (deletingFlag) {
            val messages = history.retrievePast(100).complete()
            if (messages.size > 1)
                channel.deleteMessages(messages).queue()
            else
                deletingFlag = false
        }
    }


}