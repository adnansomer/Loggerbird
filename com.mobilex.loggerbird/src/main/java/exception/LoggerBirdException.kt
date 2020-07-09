package exception

import java.lang.Exception

/**
 * Custom Exception class for printing custom error messages.
 */
internal class LoggerBirdException(message: String) : Exception(message) {
}