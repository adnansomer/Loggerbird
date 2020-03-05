package deneme.example.loggerbird

import java.lang.Exception
//Custom Exception class for printing custom error message if LogInit method return value is false in LogDeneme class.
class ExceptionCustom(message:String): Exception(message) {
}