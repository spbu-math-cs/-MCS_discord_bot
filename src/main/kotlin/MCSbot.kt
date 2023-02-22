import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import java.io.File
import java.io.PrintStream


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Программист дурачок - забыл про токен.")
        return
    }

    if (args.size > 1) {
        GlobalLogger.globalLogger.setStream(PrintStream(File(args[1])))
    }

    val jda = JDABuilder.create(
        args[0],
        GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)
    )/*.setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL)
        .enableCache(CacheFlag.ONLINE_STATUS)*/.build()

    jda.presence.setStatus(OnlineStatus.ONLINE)
    jda.addEventListener(
        TestListener(),
        RegistrationListener(),
        SubjectManagementListener(),
        ChannelListManager(),
        InviteGenerator(),
        CleaningButtonListener()
    )
}