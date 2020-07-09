package models.api.jira

import java.net.URI

/**
 * This class is a model for Jira Api request
 */
data class JiraLinkedIssueModel(
    var inward:String?  = null,
    var outward:String? = null,
    var name:String? = null
)

