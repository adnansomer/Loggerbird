package api

interface ResponseCallback<T> {

    fun onSuccess(response: T)
    fun onFailure(responseCode: Int, errorMessage: String?)
    fun onException(ex: Exception?)
}