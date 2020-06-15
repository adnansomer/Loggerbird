package models

import java.net.URI

data class JiraBasicModel(
    var self: URI,
    var id:Long?,
    var name:String,
    var description:String?
)

