package api

interface StringResponseCallback : ResponseCallback<String?> {
    override fun onSuccess(response: String?)
}