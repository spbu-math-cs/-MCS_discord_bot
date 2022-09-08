import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent


fun main(args: Array<String>) {
    val jda = JDABuilder.create(
        args[0],
        GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)
    )/*.setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL)
        .enableCache(CacheFlag.ONLINE_STATUS)*/.build()

    jda.presence.setStatus(OnlineStatus.ONLINE)
    jda.addEventListener(TestBot(), RegistrationBot(), SubjectManagerBot())
}

/*TODO
* Тесты: 2 курса с одинаковым именем
* подтверждение роли
* много регистраций одновременно
* Ручное тестирование
*
* Задачи: действия отправлять в queue, а не в comlete
* Разделение по файлам
* Логгирование
* Список актуальных курсов
* Инструкции по взаимодействию с ботом
*
* */
