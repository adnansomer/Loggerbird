package models.api.jira

/**
 * This class is a model for Jira Api request
 */
internal data class JiraUserModel(
    var displayName: String? = null,
    var accountId: String? = null,
    var self: String? = null,
    var emailAddress:String? = null
)

