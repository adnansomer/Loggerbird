package models.jira

data class JiraUserModel(
    var displayName: String? = null,
    var accountId: String? = null,
    var self: String? = null,
    var emailAddress:String? = null
)

