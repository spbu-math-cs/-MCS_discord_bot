import org.apache.logging.log4j.Level
import org.apache.logging.log4j.message.MessageFactory
import org.apache.logging.log4j.message.ParameterizedMessageFactory
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory
import org.apache.logging.log4j.util.PropertiesUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.impl.SimpleLoggerFactory

val globalLogger = SimpleLogger("Global", Level.TRACE,true,false,true,false, "yyyy-MM-dd HH:mm:ss:SSS", ParameterizedMessageFactory.INSTANCE, PropertiesUtil(""),System.out);



