package api

import org.json.JSONObject

interface JsonResponseCallback : ResponseCallback<JSONObject?> {
    override fun onSuccess(response: JSONObject?)
}
