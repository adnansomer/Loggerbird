package loggerbird.exception

import java.lang.Exception

//Custom Exception class for printing custom error messages.
class LoggerBirdException(message: String) : Exception(message) {
}