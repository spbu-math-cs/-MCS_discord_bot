import org.apache.logging.log4j.Level
import org.apache.logging.log4j.message.MessageFactory
import org.apache.logging.log4j.message.ParameterizedMessageFactory
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory
import org.apache.logging.log4j.util.PropertiesUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.impl.SimpleLoggerFactory


object GlobalLogger{
    const val RESET: String = "\u001B[0m"
    const val RED: String = "\u001B[31m"
    const val GREEN: String = "\u001B[32m"
    const val YELLOW: String = "\u001B[33m"
    const val BLUE: String = "\u001B[34m"
    const val SEPARATOR: String = "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"

    val globalLogger = SimpleLogger("Global", Level.TRACE,true,false,true,false, "yyyy-MM-dd HH:mm:ss:SSS", ParameterizedMessageFactory.INSTANCE, PropertiesUtil(""),System.out);

    fun logFunctionEnter(functionName: String, className: String) {
        globalLogger.debug(GREEN + "Entered " + YELLOW + functionName + GREEN +  " at " + YELLOW + className + RESET)
    }

    fun logFunctionLeave(functionName: String, className: String) {
        globalLogger.debug(GREEN + "Left " + YELLOW + functionName + GREEN +  " at " + YELLOW + className + RESET)
    }

    fun logButtonInteractionEnter(functionName:String, className:String, id: String) {
        globalLogger.debug(GREEN + "Entered " + YELLOW + functionName + GREEN + " button: '" + YELLOW + id + GREEN +
                "' at " + YELLOW + className + RESET)
    }

    fun logButtonInteractionLeave(functionName:String, className:String, id: String) {
        globalLogger.debug(GREEN + "Left " + YELLOW + functionName + GREEN + " button: '" + YELLOW + id + GREEN +
                "' at " + YELLOW + className + RESET)
    }

    fun logModalInteractionEnter(functionName:String, className:String, id: String) {
        globalLogger.debug(GREEN + "Entered " + YELLOW + functionName + GREEN + " modal: '" + YELLOW + id +GREEN +
                "' at " + YELLOW + className + RESET)
    }

    fun logModalInteractionLeave(functionName:String, className:String, id: String) {
        globalLogger.debug(GREEN + "Entered " + YELLOW + functionName + GREEN + " modal: '" + YELLOW + id +GREEN +
                "' at " + YELLOW + className + RESET)
    }
}