import GlobalLogger.logFunctionEnter
import GlobalLogger.logFunctionLeave
import Utility.getChannel
import Utility.Channels
import Utility.Categories
import Utility.clearChannel
import Utility.getCategory
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button

class InviteGenerator : ListenerAdapter() {
    lateinit var guild: Guild
    lateinit var inviteGeneratorChannel: TextChannel
    lateinit var registrationChannel: TextChannel

    private fun sendInvite() {
        inviteGeneratorChannel.sendMessage("Ссылка на актуальное одноразовое приглашение. " +
                "Не забудьте обновить её, нажав на кнопку 'Обновить приглашение' повторно, " +
                "после того как отправили это кому-нибудь:\n" +
                registrationChannel.createInvite().setMaxUses(1).setMaxAge(7 * 24 * 60 * 60).setUnique(true).complete().url
        ).queue()
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name)

        guild = event.guild
        inviteGeneratorChannel = getChannel(Channels.INVITE_GENERATOR, getCategory(Categories.ADMINISTRATION, guild))
        registrationChannel = getChannel(Channels.REGISTRATION, getCategory(Categories.REGISTRATION, guild))

        clearChannel(inviteGeneratorChannel)
        inviteGeneratorChannel.sendMessage("Нажмите на кнопку, чтобы обновиить приглашение для одного человека:")
            .setActionRow(Button.primary("update_invite", "Обновить приглашение")).queue()

        sendInvite()

        logFunctionLeave(Throwable().stackTrace[0].methodName, this.javaClass.name)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name)

        if (event.componentId != "update_invite")
            return

        val messages = MessageHistory(inviteGeneratorChannel).retrievePast(1).complete()
        messages[0].delete().queue()

        sendInvite()
        event.deferReply(true).queue()
        event.hook.sendMessage(
            "Ссылка обновлена успешно."
        ).setEphemeral(true).complete()

        logFunctionLeave(Throwable().stackTrace[0].methodName, this.javaClass.name)
    }
}