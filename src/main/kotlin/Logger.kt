import org.apache.logging.log4j.Level
import org.apache.logging.log4j.message.ParameterizedMessageFactory
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream

object GlobalLogger{
    const val RESET: String = "\u001B[0m"
    const val RED: String = "\u001B[31m"
    const val GREEN: String = "\u001B[32m"
    const val YELLOW: String = "\u001B[33m"

    var logFile: String = "src/main/kotlin/log.txt"

    val globalLogger = SimpleLogger(
        "Global", Level.TRACE,
        true,
        false,
        true,
        false,
        "yyyy-MM-dd HH:mm:ss:SSS",
        ParameterizedMessageFactory.INSTANCE,
        PropertiesUtil(""),
        PrintStream(File(logFile))
    )

    fun logFunctionEnter(functionName: String, className: String) {
        globalLogger.debug(GREEN + "Entered " + YELLOW + functionName + GREEN +  " at " + YELLOW + className + RESET)
    }

    fun logFunctionLeave(functionName: String, className: String) {
        globalLogger.debug(GREEN + "Left " + YELLOW + functionName + GREEN +  " at " + YELLOW + className + RESET)
    }

    private fun logInteraction(
        typeOfLog: String,
        whatLog: String,
        functionName: String,
        className: String,
        id: String,
        memberTag: String
    ) {
        globalLogger.debug(GREEN + typeOfLog + ' ' + YELLOW + functionName + ' '
                + GREEN + whatLog + " : '" + YELLOW + id + GREEN +
                "' at " + YELLOW + className +
                GREEN + " by '" + YELLOW + memberTag + GREEN + "'" + RESET)
    }

    fun logButtonInteractionEnter(functionName: String, className: String, id: String, memberTag: String) =
        logInteraction("Entered", "button", functionName, className, id, memberTag)

    fun logButtonInteractionLeave(functionName: String, className: String, id: String, memberTag: String) =
        logInteraction("Left", "button", functionName, className, id, memberTag)


    fun logModalInteractionEnter(functionName:String, className:String, id: String, memberTag: String) =
        logInteraction("Entered", "modal", functionName, className, id, memberTag)

    fun logModalInteractionLeave(functionName:String, className:String, id: String, memberTag: String) =
        logInteraction("Left", "modal", functionName, className, id, memberTag)
}