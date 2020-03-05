package deneme.example.loggerbird
//dummy model class for retrofit tests will be deleted in the future.
object RetroFitModel {
    data class Result(val query: Query)
    data class Query(val searchinfo: SearchInfo)
    data class SearchInfo(val totalhits: Int)
}