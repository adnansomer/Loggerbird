package loggerbird.models.api.jira

/**
 * This class is a model for Jira Api request
 */
internal data class JiraLinkedIssueModel(
    var inward:String?  = null,
    var outward:String? = null,
    var name:String? = null
)

