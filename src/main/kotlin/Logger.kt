import org.apache.logging.log4j.Level
import org.apache.logging.log4j.message.ParameterizedMessageFactory
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream

object GlobalLogger{
    val globalLogger = SimpleLogger(
        "Global", Level.TRACE,
        true,
        false,
        true,
        false,
        "yyyy-MM-dd HH:mm:ss:SSS",
        ParameterizedMessageFactory.INSTANCE,
        PropertiesUtil(""),
        PrintStream(File("mknbot.log"))
    )

    fun logFunctionEnter(functionName: String, className: String) {
        globalLogger.debug("Entered $functionName at $className")
    }

    fun logFunctionLeave(functionName: String, className: String) {
        globalLogger.debug("Left $functionName at $className")
    }

    private fun logInteraction(
        typeOfLog: String,
        whatLog: String,
        functionName: String,
        className: String,
        id: String,
        memberTag: String
    ) {
        globalLogger.debug(typeOfLog + ' ' + functionName + ' '
                 + whatLog + " : '" + id + "' at " + className +
                " by '" + memberTag + "'")
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