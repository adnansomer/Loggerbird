package models.api.jira

import java.net.URI

/**
 * This class is a model for Jira Api request
 */
internal data class JiraBasicModel(
    var self: URI,
    var id:Long?,
    var name:String,
    var description:String?
)

