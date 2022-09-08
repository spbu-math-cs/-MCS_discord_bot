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
* много регистраций одновременно
* Ручное тестирование
*
* Задачи:
* queue - обещает выполнить задачу позже, complete блокирует поток до выполнения задачи, выбирайте более подходящий
* Разделение по файлам
* Логгирование
* подтверждение роли
* Список актуальных курсов
* Инструкции по взаимодействию с ботом
* Создать заметки для администрации, запрещающие менять имена каналов: болталка, стойка информации и тд (тех, которые были созданы вручную) + придумать механизм защиты от таких изменений
* Например: бот будет менять названия обратно или в принципе блокировать подобное действие
* Сортировка каналов
* По всем ошибкам в вводах пользователя давать фидбек и максимально безболезненно обрабатывать
* Если канал или роль не находится, но мы уверены, что она должна быть, добавляем
* */
