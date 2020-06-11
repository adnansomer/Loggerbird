package models

import java.net.URI

data class JiraLinkedIssueModel(
    var inward:String?  = null,
    var outward:String? = null,
    var name:String? = null
)

