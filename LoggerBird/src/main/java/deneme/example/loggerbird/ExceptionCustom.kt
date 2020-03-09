package deneme.example.loggerbird

import java.lang.Exception
//Custom Exception class for printing custom error messages.
class ExceptionCustom(message:String): Exception(message) {
}