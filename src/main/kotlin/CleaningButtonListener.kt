import GlobalLogger.logButtonInteractionEnter
import GlobalLogger.logButtonInteractionLeave
import GlobalLogger.logFunctionEnter
import GlobalLogger.logFunctionLeave
import GlobalLogger.logModalInteractionEnter
import GlobalLogger.logModalInteractionLeave
import Utility.clearChannel
import Utility.getCategory
import Utility.getChannel
import Utility.getCurrentStudyYear
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.time.LocalDateTime

class CleaningButtonListener : ListenerAdapter() {
    private val toCleanListButton = Button.primary("cleaner", "Удалить старые каналы")

    private val confirmationString = "УДАЛИТЬ"

    private fun deleteOutdatedChannels(guild: Guild) {
        val lastYearToDelete = getCurrentStudyYear(LocalDateTime.now().year - 4)
        val subjectCategory = getCategory(Utility.Categories.SUBJECTS ,guild)
        val toDelete = subjectCategory.channels.filter { getCurrentStudyYear(it.timeCreated.year) < lastYearToDelete}
        println(toDelete.size)
        toDelete.forEach { it.delete().queue() }
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        logFunctionEnter(Throwable().stackTrace[0].methodName, this.javaClass.name)

        val guild = event.guild
        val cleaningChannel = getChannel(
            Utility.Channels.CLEANING,
            getCategory(Utility.Categories.ADMINISTRATION, guild)
        )

        clearChannel(cleaningChannel)

        cleaningChannel.sendMessage(
            "После нажатия на эту кнопку все каналы созданные более 5 лет назад удалятся!"
        ).setActionRow(toCleanListButton).queue()

        logFunctionLeave(Throwable().stackTrace[0].methodName,this.javaClass.name)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != "cleaner")
            return

        logButtonInteractionEnter(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.componentId,
            event.user.asTag
        )

        val confirmation = ActionRow.of(
            TextInput.create("confirm",
                "Введите '$confirmationString' для подтверждения",
                TextInputStyle.SHORT
            ).setRequiredRange(7, 7)
                .setPlaceholder("Введите '$confirmationString'")
                .build()
        )


        val cleaningConfirmation = Modal.create(
            "cleaningConfirmation",
            "Подтверждение очистки"
        ).addActionRows(confirmation).build()

        event.replyModal(cleaningConfirmation).queue()

        logButtonInteractionLeave(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.componentId,
            event.user.asTag
        )
    }


    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId != "cleaningConfirmation")
            return

        logModalInteractionEnter(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.modalId,
            event.user.asTag
        )

        val guild = event.guild ?: return

        if (event.getValue("confirm")?.asString == confirmationString) {
            deleteOutdatedChannels(guild)

            event.deferReply(true).queue()
            event.hook.sendMessage("Вы успешно удалили всё блять. ")
                .setEphemeral(true).complete()
        } else {
            event.deferReply(true).queue()
            event.hook.sendMessage("Вы успешно передумали. ")
                .setEphemeral(true).complete()
        }

        logModalInteractionLeave(
            Throwable().stackTrace[0].methodName,
            this.javaClass.name,
            event.modalId,
            event.user.asTag
        )
        return
    }
}